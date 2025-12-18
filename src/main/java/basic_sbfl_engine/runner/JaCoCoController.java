package basic_sbfl_engine.runner;


//TODO 後で最適化して消すクラス IAgentが見つからない
public final class JaCoCoController {

    private JaCoCoController() {}

    /**
     * テスト実行前に実行データをリセット。
     */
    public static void resetExecutionData() {
    	/*
        IAgent agent = RT.getAgent();
        // true で reset しつつデータ取得だが、ここでは捨てる
        agent.getExecutionData(true);
        */
    }

    /**
     * テスト実行後に実行データを取得（reset なし）。
     */
    public static CoverageData dumpExecutionData() {
    	/*
    		IAgent agent = RT.getAgent();
	        byte[] data = agent.getExecutionData(false);
	        return new CoverageData(data);
    	 */
    	return null;
    }
}