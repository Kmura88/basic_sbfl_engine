package basic_sbfl_engine;

import basic_sbfl_engine.config.SBFLConfig;
import basic_sbfl_engine.coverage.CoverageMatrix;
import basic_sbfl_engine.io.CSVWriter;
import basic_sbfl_engine.runner.JUnitRunner;
import basic_sbfl_engine.runner.JaCoCoRunner;
import basic_sbfl_engine.sbfl.Ochiai;

public class Main {

    public static void main(String[] args) throws Exception {
    	// 1. 引数の処理
        SBFLConfig config = SBFLConfig.fromArgs(args);

        JUnitRunner junit = new JUnitRunner(config);
        junit.runTests();

        
        JaCoCoRunner jacoco = new JaCoCoRunner(config);
        CoverageMatrix matrix = jacoco.collectCoverage();

        Ochiai ochiai = new Ochiai();
        var suspiciousness = ochiai.compute(matrix);

        CSVWriter.write(config.getOutputPath(), suspiciousness);
    }
}
