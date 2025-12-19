package basic_sbfl_engine.data;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.ICounter;

/**
 * CoverageBuilderをboolean配列に変換して保存する
 * 単体テストの、各クラスのカバレッジの管理に対して責任を持つ
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
     * カバレッジ配列のlengthを取得するメソッド
     * @param fqcn クラス名
     * @return カバレッジの行数
     */
    public int getLength(String fqcn) {
    	if(coverages.containsKey(fqcn)) {
    		return coverages.get(fqcn).length;
    	}else {
    		return -1;
    	}
    }
    
    /**
     * クラス名と行数から通ったか否かを判定する
     * @param fqcn クラス名
     * @param line 行数(1スタート)
     * @return 通ったか否か
     */
    public boolean getCoverage(String fqcn,int line) {
    	if(coverages.containsKey(fqcn)) {
    		if(0<line && line<getLength(fqcn)) {
    	   		return coverages.get(fqcn)[line];
    		}else {
    	   		return false;
    		}
    	}else {
    		return false;
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


