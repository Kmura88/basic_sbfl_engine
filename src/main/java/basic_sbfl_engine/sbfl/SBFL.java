package basic_sbfl_engine.sbfl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import basic_sbfl_engine.data.Suspiciousness;
import basic_sbfl_engine.data.TestResult;
import basic_sbfl_engine.io.ClassPathScanner;
import basic_sbfl_engine.runner.JUnitRunner;

/**
 * TestResult配列からef, nf, ep, npを計算し、Suspiciousnessを生成する
 */
public abstract class SBFL {

    private List<Suspiciousness> susList;     // 結果保持用
    private Map<String, int[][]> spectrumData;// Map<ClassName, int[行番号][0=ef, 1=ep]>
    
    // 全体のテスト数
    private int totalFailed;
    private int totalPassed;

    /**
     * フォルダー内のclassファイルを再帰的に全探索してSBFLを行う
     * @param FolderPath classファイルが有るフォルダーへのパス
     * @param testClasses 実行したいtestクラス名 (nullなら全testになる)
     * @param targetClassNames test結果が欲しいクラス名 (nullなら全クラスになる)
     * @param timeout タイムアウト時間
     * @throws IOException
     */
    public void compute(String FolderPath, List<String> testClassNames,
    					Set<String> targetClassNames, long timeout) throws IOException {
    	JUnitRunner junitRunner = new JUnitRunner(ClassPathScanner.scan(FolderPath), timeout);
    	junitRunner.setTargetClassNames(targetClassNames);
    	compute(junitRunner.runTests(testClassNames));
    }
    
    /**
     * 計算を実行する
     * @param testResults テスト結果のリスト
     */
    public void compute(List<TestResult> testResults) {
        this.susList = new ArrayList<>();
        this.totalFailed = 0;
        this.totalPassed = 0;

        // 1. 配列のサイズ決定と初期化
        initializeSpectrumData(testResults);

        // 2. 実行回数(ef, ep)のカウント
        countExecutions(testResults);

        // 3. 疑惑値の計算
        calculateSuspiciousness();
    }

    /**
     * 全クラスの最大行数を走査し、spectrumDataマップを初期化する
     */
    private void initializeSpectrumData(List<TestResult> testResults) {
        Map<String, Integer> classMaxLines = new HashMap<>();
        
        for (TestResult tr : testResults) {
            for (String className : tr.getClassName()) {
                int length = tr.getLength(className);
                classMaxLines.merge(className, length, Math::max);
            }
        }

        this.spectrumData = new HashMap<>();
        for (Map.Entry<String, Integer> entry : classMaxLines.entrySet()) {
            // [行数+1][2] の配列を確保 (0:ef, 1:ep)
            this.spectrumData.put(entry.getKey(), new int[entry.getValue() + 1][2]);
        }
    }

    /**
     * テスト結果を走査して ef, ep, totalFailed, totalPassed を集計する
     */
    private void countExecutions(List<TestResult> testResults) {
        for (TestResult tr : testResults) {
            boolean isPassed = tr.isPassed();
            
            if (isPassed) this.totalPassed++;
            else this.totalFailed++;

            for (String className : tr.getClassName()) {
                if (!spectrumData.containsKey(className)) continue;

                int[][] lines = spectrumData.get(className);
                int length = tr.getLength(className);

                // 行ごとのカバレッジを確認
                for (int line = 1; line < length; line++) {
                    if (tr.getCoverage(className, line)) {
                        if (isPassed) lines[line][1]++; // ep++
                        else lines[line][0]++; // ef++
                    }
                }
            }
        }
    }

    /**
     * 集計データとformulaを用いて疑惑値を計算する
     */
    private void calculateSuspiciousness() {
        for (Map.Entry<String, int[][]> entry : spectrumData.entrySet()) {
            String className = entry.getKey();
            int[][] lines = entry.getValue();
            
            double[] score = new double[lines.length + 1];//1スタートのため+1
            
            for (int line = 1; line < lines.length; line++) {
                double ef = lines[line][0];
                double ep = lines[line][1];
                double nf = this.totalFailed - ef;
                double np = this.totalPassed - ep;
                
                score[line] = formula(ef, nf, ep, np);
            }
            susList.add(new Suspiciousness(className, score));
        }
    }
    
    /**
     * @param className クラス名
     * @param line 行数
     * @return 指定されたクラス・行の ef
     */
    public double getEf(String className, int line) {
        if (isValid(className, line)) {
            return spectrumData.get(className)[line][0];
        }
        return 0;
    }
    
    /**
     * @param className クラス名
     * @param line 行数
     * @return 指定されたクラス・行の ep
     */
    public double getEp(String className, int line) {
        if (isValid(className, line)) {
            return spectrumData.get(className)[line][1];
        }
        return 0;
    }

    /**
     * @param className クラス名
     * @param line 行数
     * @return 指定されたクラス・行の nf
     */
    public double getNf(String className, int line) {
        if (isValid(className, line)) {
            double ef = spectrumData.get(className)[line][0];
            return this.totalFailed - ef;
        }
        return 0;
    }
    
    /**
     * @param className クラス名
     * @param line 行数
     * @return 指定されたクラス・行の np
     */
    public double getNp(String className, int line) {
        if (isValid(className, line)) {
            double ep = spectrumData.get(className)[line][1];
            return this.totalPassed - ep;
        }
        return 0;
    }
    
    /**
     * @return 落ちたテスト数
     */
    public int getTotalFailed() {
    	return this.totalFailed;
    }
    
    /**
     * @return 通ったテスト数
     */
    public int getTotalPassed() {
    	return this.totalPassed;
    }
    
    /**
     * 計算結果のリストを取得
     */
    public List<Suspiciousness> getSusList() {
        return this.susList;
    }
    
    /**
     * クラス名と行番号が有効範囲内かチェックする内部メソッド
     */
    private boolean isValid(String className, int line) {
        return spectrumData != null 
            && spectrumData.containsKey(className) 
            && line > 0 
            && line < spectrumData.get(className).length;
    }
    
    /**
     * SBFLの計算式 (サブクラスで実装)
     */
    protected abstract double formula(double ef, double nf, double ep, double np);
}