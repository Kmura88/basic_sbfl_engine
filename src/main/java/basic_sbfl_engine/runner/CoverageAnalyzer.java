package basic_sbfl_engine.runner;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.ICounter;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.data.SessionInfoStore;
import org.jacoco.core.instr.Instrumenter;
import org.jacoco.core.runtime.IRuntime;
import org.jacoco.core.runtime.LoggerRuntime;
import org.jacoco.core.runtime.RuntimeData;

// TODO 後で消すクラス処理作成用

public class CoverageAnalyzer {
	
	// テスト結果を保存するフィールド
	CoverageReport coverageReport;
	
	/*
	 * コンストラクタ
	 */
	public CoverageAnalyzer (){
		//coverageReport = new CoverageReport();
	}
	
	public CoverageReport getCoverageReport() {
		return this.coverageReport;
	}
	
	public void run() throws Exception {
		 // 計測対象のクラス名（完全修飾名）
        final String targetClassName = "example.Calculator";

        // 1. JaCoCoランタイムの準備
        final IRuntime runtime = new LoggerRuntime();
        final RuntimeData data = new RuntimeData();
        runtime.startup(data);

        // 2. Instrumenterの準備
        final Instrumenter instrumenter = new Instrumenter(runtime);

        // 3. 対象クラスのバイトコードを読み込み、計測コードを埋め込む
        
        //final String targetClassResource = "/" + targetClassName.replace('.', '/') + ".class";
        final String targetClassResource = "Calculator.class";
        final InputStream originalInputStream = CoverageAnalyzer.class.getResourceAsStream(targetClassResource);
        if (originalInputStream == null) {
            System.err.println("Target class not found: " + targetClassResource);
            return;
        }
        final byte[] instrumentedBytecode = instrumenter.instrument(originalInputStream, targetClassResource);
        originalInputStream.close();

        // 4. 計測されたクラスをロードするためのカスタムクラスローダー
        final MemoryClassLoader memoryClassLoader = new MemoryClassLoader();
        memoryClassLoader.addDefinition(targetClassName, instrumentedBytecode);
        final Class<?> targetClass = memoryClassLoader.loadClass(targetClassName);

        // 5. テストシナリオの実行（計測されたクラスのメソッドを呼び出す）
        // この部分が実際のテストコードに相当します
        Object instance = targetClass.getDeclaredConstructor().newInstance();

        // addメソッドをテスト
        targetClass.getMethod("add", int.class, int.class).invoke(instance, 5, 3);
        
        // subtractメソッドをテスト
        //targetClass.getMethod("subtract", int.class, int.class).invoke(instance, 10, 4);

        // checkPositiveメソッドをテスト（分岐の一方のみ通過）
        //targetClass.getMethod("checkPositive", int.class).invoke(instance, 10);
        //targetClass.getMethod("checkPositive", int.class).invoke(instance, -1);//add


        // 6. カバレッジデータの収集
        final ExecutionDataStore executionData = new ExecutionDataStore();
        final SessionInfoStore sessionInfos = new SessionInfoStore();
        data.collect(executionData, sessionInfos, false);
        runtime.shutdown();

        // 7. カバレッジ分析の実行
        final CoverageBuilder coverageBuilder = new CoverageBuilder();// カバレッジ計測をする部分？
        final Analyzer analyzer = new Analyzer(executionData, coverageBuilder);

        // 分析には「計測されていない」元のバイトコードが必要
        final InputStream originalForAnalysis = CoverageAnalyzer.class.getResourceAsStream(targetClassResource);
        analyzer.analyzeClass(originalForAnalysis, targetClassResource);
        originalForAnalysis.close();
        
        // 8. 結果の表示
        printCoverage(coverageBuilder);
        
        // 9. boolean配列を構築し結果を出力(OK)
        
        boolean[] arr = convertCoverage(coverageBuilder).get("example/Calculator");
        
        for(int i = 0;i<arr.length;i++) {
        	System.out.printf("%d ",i);
        	System.out.println(arr[i]);
        }
        
	}
	
	private static void printCoverage(CoverageBuilder coverageBuilder) {
        System.out.println("--- JaCoCo Coverage Analysis ---");
        for (final IClassCoverage cc : coverageBuilder.getClasses()) {
            System.out.printf("Coverage for class \"%s\"%n", cc.getName());

            printCounter("Instructions", cc.getInstructionCounter());
            printCounter("Branches", cc.getBranchCounter());
            printCounter("Lines", cc.getLineCounter());
            printCounter("Methods", cc.getMethodCounter());
            printCounter("Complexity", cc.getComplexityCounter());
        }
    }

    private static void printCounter(final String unit, final ICounter counter) {
        final int missed = counter.getMissedCount();
        final int covered = counter.getCoveredCount();
        final int total = counter.getTotalCount();
        final double coveredRatio = counter.getCoveredRatio();//(double) covered / (missed + covered);
        System.out.printf("  %s: %d missed, %d covered (%.2f%%)%n", unit, missed, covered, coveredRatio * 100);
        System.out.printf(" total: %d\n",total);
    }
	
	public static class MemoryClassLoader extends ClassLoader { // ClassLoaderの子クラス
        private final java.util.Map<String, byte[]> definitions = new java.util.HashMap<>();

        public void addDefinition(final String name, final byte[] bytes) {
            definitions.put(name, bytes);
        }

        @Override
        protected Class<?> loadClass(final String name, final boolean resolve) throws ClassNotFoundException {
            final byte[] bytes = definitions.get(name);
            if (bytes != null) {
                return defineClass(name, bytes, 0, bytes.length);
            }
            return super.loadClass(name, resolve);
        }
    }
	
	/**
	 * CoverageBuilder内のクラス毎のカバレッジ情報をBoolean配列に変換する
	 * 配列内の
	 * @param 計測済みのCoverageBuilder
	 * @return Map<クラス名,各行が通ったか否か>
	 */
	public Map<String, boolean[]> convertCoverage(CoverageBuilder coverageBuilder) {
		Map<String, boolean[]> ret = new HashMap<>();
        for (IClassCoverage cc : coverageBuilder.getClasses()) {
            int lastLine = cc.getLastLine();
            boolean[] covered = new boolean[lastLine + 1];
            for (int line = 0; line <= lastLine; line++) {
                int status = cc.getLine(line).getStatus();
                if (status == ICounter.FULLY_COVERED || status == ICounter.PARTLY_COVERED) {
                    covered[line] = true;
                } else {
                	covered[line] = false;
                }
            }
            System.out.println(cc.getName()); // TODO 後で消す
            ret.put(cc.getName(), covered);
        }
        return ret;
    }
	
}