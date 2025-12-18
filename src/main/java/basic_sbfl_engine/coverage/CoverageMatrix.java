package basic_sbfl_engine.coverage;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

//TODO 消す？？
public class CoverageMatrix {

    // Map<クラス名, Map<行番号, テストごとの実行有無>>
    private final Map<String, Map<Integer, LineCoverage>> matrix;

    public static CoverageMatrix fromJaCoCo(Object jacocoData) {
        // JaCoCo API から
        return new CoverageMatrix(new HashMap<>());
    }

    public Set<String> getClasses() {
        return matrix.keySet();
    }

    public LineCoverage getLine(String className, int line) {
        return matrix.get(className).get(line);
    }
}