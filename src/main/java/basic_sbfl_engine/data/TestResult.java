package basic_sbfl_engine.data;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.ICounter;

/**
 * CoverageBuilderをboolean配列に変換して保存する
 * 各テスト、各クラス、各行のカバレッジの管理に対して責任を持つ
 */
public class TestResult {
    private final String testName;
    private final boolean passed; // 成功TestならTrue
    private final Map<String, boolean[]> coverages; // fqcn -> 各行の実行TF

    /**
     * TestResultコンストラクタ
     * @param testName testメソッド名
     * @param coverageBuilder 測定済みcoverageBuilder
     * @param passed 成功テストならtrue
     */
    public TestResult(String testName, CoverageBuilder coverageBuilder, boolean passed) {
        this.testName = testName;
        this.passed = passed;
        this.coverages = convertCoverage(coverageBuilder);
    }
    
    /**
     * @return testメソッド名
     */
    public String getTestName() {
        return testName;
    }

    /**
     * @return 成功テストならtrue
     */
    public boolean isPassed() {
        return passed;
    }
    
    /**
     * @return 保存しているclass名の集合
     */
    public Set<String> getClassName() {
        return coverages.keySet();
    }
    
    /**
     * @param fqcn クラス名
     * @return 引数として与えられたクラスの各行のカバレッジ
     */
    public boolean[] getCoverage(String fqcn) {
    	if(coverages.containsKey(fqcn)) {
    		return coverages.get(fqcn);
    	}else {
    		return new boolean[0];
    	}
    }
    
	/**
	 * CoverageBuilder内のクラス毎のカバレッジ情報をBoolean配列に変換する内部メソッド
	 * @param 計測済みのCoverageBuilder
	 * @return Map<クラス名, 各行が通ったか否か>
	 */
	private static Map<String, boolean[]> convertCoverage(CoverageBuilder coverageBuilder) {
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
            ret.put(cc.getName(), covered);
        }
        return ret;
    }
}


