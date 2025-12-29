package basic_sbfl_engine.runner;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.data.ExecutionDataStore;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;

import basic_sbfl_engine.data.TestResult;

/**
 * Junitを用いてテストメソッドを実行するクラス<br>
 * junit coreを使っているので@beforeや@afterも実行される
 */
public class JUnitRunner {

    private final long timeout;
    private MemoryClassLoader memoryClassLoader;
    private JaCoCoRunner jacocoRunner;
    private ExecutionDataAnalyzer executionDataAnalyzer;
    private final Map<String, byte[]> classDefinitions; // <fqcn,バイトコード> 
    private Set<String> targetClassNames;
    private boolean suppressOutput = true; //出力を抑制するか

    /**
     * @param ClassDefinitions 解析対象クラスのバイトコード
     * @param timeout  テストメソッド1件あたりのタイムアウト(ms)
     */
    public JUnitRunner(Map<String, byte[]> classDefinitions, long timeout) {
        this.timeout = timeout;
        this.classDefinitions = classDefinitions;
    }
    
    
    /**
     * テスト実行中の標準出力・エラー出力を抑制するか設定する
     * @param suppressOutput trueなら出力削除、falseなら表示
     */
    public void setSuppressOutput(boolean suppressOutput) {
        this.suppressOutput = suppressOutput;
    }
    
    
    /**
     * TestResultの収集対象のクラスを指定できる<br>
     * 指定しなかった場合は実行した全クラスが対象になる
     * @param targetClassNames
     */
    public void setTargetClassNames(Set<String> targetClassNames) {
    	this.targetClassNames = targetClassNames;
    }
    
    private void init() throws IOException {
    	
    	this.jacocoRunner = new JaCoCoRunner();
    	this.executionDataAnalyzer = new ExecutionDataAnalyzer();
    	this.memoryClassLoader = new MemoryClassLoader();
    	
        for (Map.Entry<String, byte[]> entry : classDefinitions.entrySet()) {
            String fqcn = entry.getKey();
            byte[] originalBytes = entry.getValue();
            
            if(targetClassNames==null||targetClassNames.contains(fqcn)) {
            	
                // A. 解析用: 元のバイトコードをAnalyzerに登録
                executionDataAnalyzer.addSubject(fqcn, originalBytes);

                // B. 実行用: JaCoCoでバイトコードをInstrument化(書き換え)
                byte[] instrumentedBytes = jacocoRunner.getInstrumenter().instrument(originalBytes, fqcn);
                
                // C. 実行用: 書き換えたバイトコードをローダーに登録
                memoryClassLoader.addDefinition(fqcn, instrumentedBytes);
                
            }else {
            	memoryClassLoader.addDefinition(fqcn, originalBytes);
            }
        }
    }

    /**
     * 全testクラスを実行する
     * @return TestResult配列
     */
    public List<TestResult> runAllTests(){
    	return runTests(null);
    }
    
    /**
     * テストの実行を行うメインメソッド<br>
     * 引数がnullの場合は全testクラスを実行する
     * @param testClassNames 実行するtestクラスのfqcn (例: "com.example.MyTest")
     * @return TestResult配列
     */
    public List<TestResult> runTests(List<String> testClassNames) {
    	
    	List<String> resolvedTestClassNames = resolveTestClassNames(testClassNames);
    	
        List<TestResult> results = new ArrayList<>();
        
        try {
            // 1. 初期化 (Instrument化 と Analyzerへの登録)
            init();
            
            // JaCoCoランタイム起動
            jacocoRunner.startup();
            
            for (String testClassName: resolvedTestClassNames) {
            	
            	// 2. MemoryClassLoaderを使ってテストクラスをロード
                Class<?> testClass = memoryClassLoader.loadClass(testClassName);

                // 3. @testアノテーションの付いたメソッドを実行
                for (Method method : testClass.getDeclaredMethods()) {
                    if (method.isAnnotationPresent(Test.class)) {
                         TestResult tr = runSingleMethod(testClass, method.getName());
                         results.add(tr);
                    }
                }
            }
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to load the class.", e);
        } finally {
            jacocoRunner.shutdown();
        }
        return results;
    }
    
    /**
     * 単一のテストメソッドを実行し、カバレッジ結果を返す
     * @param testClass テストクラス
     * @param methodName メソッド名
     * @return TestResult
     */
    public TestResult runSingleMethod(Class<?> testClass, String methodName) {
        // 1. カバレッジ情報のリセット
        jacocoRunner.reset();

        boolean passed = false;
        ExecutorService executor = Executors.newSingleThreadExecutor();
        
        // 2. 元のストリームを退避
        PrintStream originalOut = System.out;
        PrintStream originalErr = System.err;

        try {
   
        	if (this.suppressOutput) {
        		// 3. システム出力を無効化ストリームにセット
                PrintStream nullStream = getNullPrintStream();
                System.setOut(nullStream);
                System.setErr(nullStream);
            }
        	
            // 4. JUnit実行タスクの作成
            Request request = Request.method(testClass, methodName);
            Callable<Result> task = () -> new JUnitCore().run(request);
            
            // 5. タイムアウト付きで実行
            Future<Result> future = executor.submit(task);
            Result result = future.get(timeout, TimeUnit.MILLISECONDS);
            
            passed = result.wasSuccessful();

        } catch (TimeoutException e) {
            System.err.println("Test timed out: " + methodName);
            passed = false;
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            passed = false;
        } finally {
        	// 6. 変更していた場合のみ元のストリームに戻す
        	if (this.suppressOutput) {
                System.setOut(originalOut);
                System.setErr(originalErr);
            }
            executor.shutdownNow();
        }

        // 4. カバレッジデータの収集・解析
        ExecutionDataStore eds = jacocoRunner.collect();
        CoverageBuilder cb = executionDataAnalyzer.analyze(eds);
        
        // 5. TestResultの生成
        return new TestResult(methodName, cb, passed);
    }
    
    private List<String> resolveTestClassNames(List<String> testClassNames) {
        if (testClassNames == null) {
        	// 引数がnullの場合、全テストクラスを取得する
        	List<String> resolvedTestClassNames = new ArrayList<>();
        	for (String fqcn : classDefinitions.keySet()) {
        		if(fqcn.endsWith("Test")){
        			// クラス名の末尾がTest ならば テストクラス
        			resolvedTestClassNames.add(fqcn);
        		}
        	}
            return resolvedTestClassNames;
        }
        return testClassNames;
    }
    
    /**
     * データを捨てるPrintStreamを作成するメソッド
     * @return 出力能力のないPrintStream
     */
    private PrintStream getNullPrintStream() {
        return new PrintStream(new OutputStream() {
            @Override
            public void write(int b) {/* 何もしない (データを捨てる)*/}
            @Override
            public void write(byte[] b, int off, int len) {/* 何もしない (データを捨てる)*/}
        });
    }
}