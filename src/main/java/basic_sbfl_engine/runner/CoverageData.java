package basic_sbfl_engine.runner;

/**
 * JaCoCo の実行データをラップするだけのクラス。
 * 実際には ExecutionDataStore / SessionInfoStore を渡す設計にしてもよい。
 */

//TODO 後で消す。
public final class CoverageData {
    public final byte[] execData;

    public CoverageData(byte[] execData) {
        this.execData = execData;
    }
}