package basic_sbfl_engine.runner;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.data.ExecutionDataStore;

/**
 * ExecutionDataStoreを解析するクラス <br>
 * addsubject()で解析対象を追加し、 analyze()で解析する
 */
public class ExecutionDataAnalyzer {

	private final Map<String, byte[]> subjects;
	
	/**
	 * コンストラクタ
	 */
	public ExecutionDataAnalyzer(){
		this.subjects = new HashMap<>(); 
	}
	
	/**
	 * 解析対象の追加
	 * @param fqcn クラス名(ex: com.example.Main)
	 * @param bytes instrunmentされてない バイト配列
	 */
	public void addSubject(String fqcn, byte[] bytes) {
		this.subjects.put(fqcn, bytes);
	}
	

	/**
	 * addSubject()で追加された解析対象に対して解析
	 * @param executionData
	 * @return CoverageBuilder
	 */
	public CoverageBuilder analyze(ExecutionDataStore executionData) {
		final CoverageBuilder coverageBuilder = new CoverageBuilder();
		final Analyzer analyzer = new Analyzer(executionData, coverageBuilder);
		this.subjects.forEach((fqcn, bytes) -> {
			try {
				analyzer.analyzeClass(bytes, fqcn);
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
		return coverageBuilder;
	}
}