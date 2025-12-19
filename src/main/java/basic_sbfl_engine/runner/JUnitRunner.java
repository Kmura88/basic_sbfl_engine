package basic_sbfl_engine.runner;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
    private final MemoryClassLoader memoryClassLoader;
    private final JaCoCoRunner jacocoRunner;
    private final ExecutionDataAnalyzer executionDataAnalyzer;
    private final Map<String, byte[]> classDefinitions; // <fqcn,バイトコード> 

    /**
     * @param timeout  テスト1件あたりのタイムアウト(ms)
     * @param jacocoRunner カバレッジ計測用ランナー
     * @param ClassDefinitions 解析対象クラスのバイトコード
     */
    public JUnitRunner(Map<String, byte[]> classDefinitions, long timeout) {
        this.timeout = timeout;
        this.classDefinitions = classDefinitions;
        this.jacocoRunner = new JaCoCoRunner();
        this.executionDataAnalyzer = new ExecutionDataAnalyzer();
        this.memoryClassLoader = new MemoryClassLoader();
    }
    
    private void initialize() throws IOException {
    	
        for (Map.Entry<String, byte[]> entry : classDefinitions.entrySet()) {
            String fqcn = entry.getKey();
            byte[] originalBytes = entry.getValue();

            // A. 解析用: 元のバイトコードをAnalyzerに登録
            executionDataAnalyzer.addSubject(fqcn, originalBytes);

            // B. 実行用: JaCoCoでバイトコードをInstrument化(書き換え)
            byte[] instrumentedBytes = jacocoRunner.getInstrumenter().instrument(originalBytes, fqcn);
            
            // C. 実行用: 書き換えたバイトコードをローダーに登録
            memoryClassLoader.addDefinition(fqcn, instrumentedBytes);
        }
    }

    /**
     * テストの準備と実行を行うメインメソッド
     * @param testClassName 実行するテストのfqcn (例: "com.example.MyTest")
     * @return TestResultのリスト
     */
    public List<TestResult> runTests(String testClassName) {
        List<TestResult> results = new ArrayList<>();
        
        try {
            // 1. 初期化 (Instrument化 と Analyzerへの登録)
            initialize();
            
            // JaCoCoランタイム起動
            jacocoRunner.startup();

            // 2. MemoryClassLoaderを使ってテストクラスをロード
            Class<?> testClass = memoryClassLoader.loadClass(testClassName);

            // 3. クラス内の全メソッドを走査
            for (Method method : testClass.getDeclaredMethods()) {
                if (method.isAnnotationPresent(Test.class)) {
                     TestResult tr = runSingleTest(testClass, method.getName());
                     results.add(tr);
                }
            }
            
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
            throw new RuntimeException("クラスのロードまたはInstrumentに失敗しました: " + testClassName, e);
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
    public TestResult runSingleTest(Class<?> testClass, String methodName) {
        // 1. カバレッジ情報のリセット
        jacocoRunner.reset();

        boolean passed = false;
        ExecutorService executor = Executors.newSingleThreadExecutor();

        try {
            // 2. JUnit実行タスクの作成
            Request request = Request.method(testClass, methodName);
            Callable<Result> task = () -> new JUnitCore().run(request);
            
            // 3. タイムアウト付きで実行
            Future<Result> future = executor.submit(task);
            Result result = future.get(timeout, TimeUnit.MILLISECONDS);
            
            passed = result.wasSuccessful();

        } catch (TimeoutException e) {
            System.err.println("Test timed out: " + methodName);
            passed = false;
            // タイムアウト時はスレッドが残らないようにshutdownNowする等の処理が必要な場合あり
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            passed = false;
        } finally {
            executor.shutdownNow();
        }

        // 4. カバレッジデータの収集・解析
        ExecutionDataStore eds = jacocoRunner.collect();
        CoverageBuilder cb = executionDataAnalyzer.analyze(eds);
        
        // 5. TestResultの生成
        return new TestResult(methodName, cb, passed);
    }

}