package basic_sbfl_engine.runner;

import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.data.SessionInfoStore;
import org.jacoco.core.instr.Instrumenter;
import org.jacoco.core.runtime.IRuntime;
import org.jacoco.core.runtime.LoggerRuntime;
import org.jacoco.core.runtime.RuntimeData;


public class JaCoCoRunner {
    private final RuntimeData runtimedata;
    private final Instrumenter instrumenter;
    private final IRuntime runtime;

    public JaCoCoRunner() {
    	// カバレッジ計測の準備
    	this.runtime = new LoggerRuntime();
        this.runtimedata = new RuntimeData();
        this.instrumenter = new Instrumenter(runtime);
        try {
            runtime.startup(runtimedata);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * バイト配列のinstrument化にはこのインスタンスを利用
     * @return Instrumenter
     */
    public Instrumenter getInstrumenter() {
        return instrumenter;
    }

    /**
     * 計測データの確定とExecutionDataStoreの返却
     * @return クラスごとの命令実行フラグのデータ
     */
    public ExecutionDataStore collect() {
    	// 
    	final ExecutionDataStore executionData = new ExecutionDataStore();
    	final SessionInfoStore sessionInfos    = new SessionInfoStore();
    	runtimedata.collect(executionData, sessionInfos, false);
    	return executionData;
    }

    /**
     * 取得したカバレッジ情報をリセットする
     */
    public void reset() {
        runtimedata.reset();
    }
    
    /**
     * カバレッジ情報の収集を終了する
     */
    public void shutdown() {
    	runtime.shutdown();
    }
}