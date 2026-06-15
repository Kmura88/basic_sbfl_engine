package basic_sbfl_engine.runner;

import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.data.SessionInfoStore;
import org.jacoco.core.instr.Instrumenter;
import org.jacoco.core.runtime.IRuntime;
import org.jacoco.core.runtime.LoggerRuntime;
import org.jacoco.core.runtime.RuntimeData;

/**
 * Jacocoを利用してカバレッジ計測を行うクラス <br>
 * startup() → collect() → shutdown()が一連の流れ <br>
 * shutdown()後の 再startup()は不可 <br>
 * 計測対象は事前にInstrumentしておく必要がある
 */
public class JaCoCoRunner {
    private final RuntimeData runtimedata;
    private final Instrumenter instrumenter;
    private final IRuntime runtime;

    /**
     * コンストラクタ
     */
    public JaCoCoRunner() {
    	this.runtime = new LoggerRuntime();
        this.runtimedata = new RuntimeData();
        this.instrumenter = new Instrumenter(runtime);
    }

    /**
     * byte配列のinstrument化にはこのインスタンスを利用
     * @return Instrumenterインスタンス
     */
    public Instrumenter getInstrumenter() {
        return instrumenter;
    }

    /**
     * 計測済みデータからExecutionDataStoreを返却するメソッド
     * @return ExecutionDataStore
     */
    public ExecutionDataStore collect() {
    	final ExecutionDataStore executionData = new ExecutionDataStore();
    	runtimedata.collect(executionData, new SessionInfoStore(), false);
    	return executionData;
    }

    /**
     * カバレッジ情報の収集を開始させるメソッド
     */
    public void startup() {
        try {
            runtime.startup(runtimedata);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * 取得したカバレッジ情報をリセットするメソッド
     */
    public void reset() {
        runtimedata.reset();
    }
    
    /**
     * カバレッジ情報の収集を終了するメソッド
     */
    public void shutdown() {
    	runtime.shutdown();
    }
}