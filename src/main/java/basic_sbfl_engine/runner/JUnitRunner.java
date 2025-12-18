package basic_sbfl_engine.runner;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
/*
 * TODO 後で消すメモ
 * junit coreを使っているので@beforeや@afterも実行される
 */

public class JUnitRunner {

    private final long timeout;
    private final TimeUnit timeUnit;

    /**
     * @param timeout  テスト1件あたりのタイムアウト
     * @param timeUnit タイムアウト単位
     */
    public JUnitRunner(long timeout, TimeUnit timeUnit) {
        this.timeout = timeout;
        this.timeUnit = timeUnit;
    }

    /**
     * MemoryClassLoader を使って 1 テストメソッドを実行する
     *
     * @param classBytesMap  クラス名 → バイト配列 のマップ ほかで用意
     * @param testClassName  テストクラスの FQCN
     * @param methodName     テストメソッド名
     */
    public TestWithCoverageResult runSingleTest(
            Map<String, byte[]> classBytesMap,
            String testClassName,
            String methodName) {

        ExecutorService executor = Executors.newSingleThreadExecutor();

        Callable<TestWithCoverageResult> task = () -> {

            // MemoryClassLoader をテストごとに作成
            MemoryClassLoader loader = new MemoryClassLoader();

            // テスト対象のクラスをすべて登録
            for (Map.Entry<String, byte[]> e : classBytesMap.entrySet()) {
                loader.addDefinition(e.getKey(), e.getValue());
            }

            // このスレッドのコンテキストローダーを差し替え
            Thread.currentThread().setContextClassLoader(loader);

            try {
                // JaCoCo をリセット
                JaCoCoController.resetExecutionData();

                // テストクラスを MemoryClassLoader でロード
                Class<?> testClass = loader.loadClass(testClassName);

                // JUnit 実行
                JUnitCore core = new JUnitCore();
                Request request = Request.method(testClass, methodName);
                Result result = core.run(request);

                // 成否判定
                SbflTestResult testResult;
                if (result.getFailureCount() == 0 && result.getIgnoreCount() == 0) {
                    testResult = SbflTestResult.success();
                } else {
                    Throwable t = null;
                    if (!result.getFailures().isEmpty()) {
                        Failure f = result.getFailures().get(0);
                        t = f.getException();
                    }
                    testResult = SbflTestResult.failure(t);
                }

                // カバレッジ取得
                CoverageData coverage = JaCoCoController.dumpExecutionData();

                return new TestWithCoverageResult(testResult, coverage);

            } finally {
                Thread.currentThread().setContextClassLoader(null);
            }
        };

        Future<TestWithCoverageResult> future = executor.submit(task);

        try {
            return future.get(timeout, timeUnit);

        } catch (TimeoutException e) {
            future.cancel(true);
            return new TestWithCoverageResult(SbflTestResult.timeout(), new CoverageData(new byte[0]));

        } catch (Exception e) {
            return new TestWithCoverageResult(SbflTestResult.failure(e), new CoverageData(new byte[0]));

        } finally {
            executor.shutdownNow();
        }
    }

    // ======== 結果 DTO ========

    public static final class TestWithCoverageResult {
        public final SbflTestResult testResult;
        public final CoverageData coverage;

        public TestWithCoverageResult(SbflTestResult testResult, CoverageData coverage) {
            this.testResult = testResult;
            this.coverage = coverage;
        }
    }
}