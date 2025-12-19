package basic_sbfl_engine.data;

/**
 * 単一クラスの疑惑値を保存するクラス
 */
public class Suspiciousness {
	private final String className;
	private double[] values; // 1スタート

	/**
	 * サイズで初期化するコンストラクタ
	 * @param className クラス名
	 * @param length 疑惑値配列の初期化サイズ
	 */
    public Suspiciousness(String className, int length) {
        this.className = className;
        this.values = new double[length+1];
    }

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
     * @return 疑惑値配列のサイズ
     */
    public int getLength() {
    	return values.length;
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

	/**
	 * 疑惑地を更新するメソッド
	 * @param line 行数
	 * @param value 疑惑値
	 */
    public void set(int line, double value) {
    	if(0<line||line<=values.length)values[line]=value;
    }

}