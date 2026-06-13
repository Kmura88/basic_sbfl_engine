package basic_sbfl_engine.data;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
    private final String testClassName;
    private final String testMethodName;
    private final long testId;    // クラス名とメソッド名から作られる単体テスト固有のID
    private final boolean passed; // 成功TestならTrue
    private final Map<String, boolean[]> coverages; // fqcn -> 各行の実行TF

    /**
     * TestResultコンストラクタ
     * @param testClassName testメソッド名
     * @param testMethodName testメソッド名
     * @param coverageBuilder 測定済みcoverageBuilder
     * @param passed 成功テストならtrue
     */
    public TestResult(String testClassName, String testMethodName, CoverageBuilder coverageBuilder, boolean passed) {
        this.testClassName   = testClassName;
        this.testMethodName  = testMethodName;
        this.testId    = computeId(testClassName + "#" + testMethodName);
        this.passed    = passed;
        this.coverages = convertCoverage(coverageBuilder);
    }
    
    /**
     * @return testクラス名
     */
    public String getTestClassName() {
        return testClassName;
    }

    /**
     * 
     * @return testメソッド名
     */
    public String getTestMethodName() {
        return testMethodName;
    }
    
    /**
     * クラス名とメソッド名から作られる単体テスト固有のID
     * @return testID
     */
    public long getTestId() {
        return testId;
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
     * テスト名からlong IDを計算する
     * SHA-256の先頭8バイトをビッグエンディアンのlongとして解釈する
     * @param testName テスト名
     * @return ハッシュ値
     */
    private static long computeId(String testName) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(testName.getBytes(StandardCharsets.UTF_8));
            return ByteBuffer.wrap(hash, 0, 8).getLong();
        } catch (NoSuchAlgorithmException e) {
            // SHA-256はJava標準保証なので到達しない
            throw new AssertionError("SHA-256 unavailable", e);
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


