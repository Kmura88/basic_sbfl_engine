package basic_sbfl_engine.data;

/**
 * 単一クラスの疑惑値を保存するクラス
 */
public class Suspiciousness {
	private final String className;
	private double[] values; // 1スタート

    /**
     * 配列で初期化するコンストラクタ
     * @param className クラス名
     * @param values 疑惑値配列
     */
    public Suspiciousness(String className, double[] values) {
        this.className = className;
        this.values = values;
    }
    
    /**
     * @return クラス名
     */
    public String getClassName() {
        return className;
    }
    
    /**
     * @return 保存している行数の最大値
     */
    public int getMaxLine() {
    	return values.length-1;// 1スタートなので1減らしておく
    }

    /**
     * 疑惑値を取得するメソッド
     * @param line 行数
     * @return 疑惑値
     */
    public double get(int line) {
    	if(line<0||values.length<=line)return 0;
    	return values[line];
    }

}