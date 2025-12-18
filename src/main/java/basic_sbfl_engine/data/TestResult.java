package basic_sbfl_engine.data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

//TODO staticなmakeTestResultメソッドを作る return TestResult

/**
 * CoverageBuilderを各クラスの各行の実行データに変換して保存する
 */

public class TestResult {

    private final String testName;
    private final boolean passed;

    // Map<クラス名, 実行された行番号集合>
    private final Map<String, Set<Integer>> coveredLines = new HashMap<>();

    public TestResult(String testName, boolean passed) {
        this.testName = testName;
        this.passed = passed;
    }

    /* =====================
     * coverage 操作
     * ===================== */

    public void addCoveredLine(String className, int line) {
        coveredLines
            .computeIfAbsent(className, k -> new HashSet<>())
            .add(line);
    }

    public boolean isLineCovered(String className, int line) {
        return coveredLines.containsKey(className)
            && coveredLines.get(className).contains(line);
    }

    /* =====================
     * getter
     * ===================== */

    public String getTestName() {
        return testName;
    }

    public boolean isPassed() {
        return passed;
    }

    public boolean isFailed() {
        return !passed;
    }

    public Map<String, Set<Integer>> getCoveredLines() {
        return coveredLines;
    }
}
