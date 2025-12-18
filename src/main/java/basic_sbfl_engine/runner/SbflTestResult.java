package basic_sbfl_engine.runner;

//TODO 後で消す

public final class SbflTestResult {
    public final boolean success;
    public final boolean timeout;
    public final Throwable failure; // 失敗理由（失敗 or エラー）

    public SbflTestResult(boolean success, boolean timeout, Throwable failure) {
        this.success = success;
        this.timeout = timeout;
        this.failure = failure;
    }

    public static SbflTestResult success() {
        return new SbflTestResult(true, false, null);
    }

    public static SbflTestResult timeout() {
        return new SbflTestResult(false, true, null);
    }

    public static SbflTestResult failure(Throwable t) {
        return new SbflTestResult(false, false, t);
    }
}
