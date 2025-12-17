package basic_sbfl_engine.runner;

import org.jacoco.core.tools.ExecFileLoader;

import basic_sbfl_engine.config.SBFLConfig;
import basic_sbfl_engine.coverage.CoverageMatrix;

public class JaCoCoRunner {

    private final SBFLConfig config;

    public JaCoCoRunner(SBFLConfig config) {
        this.config = config;
    }

    public CoverageMatrix collectCoverage() throws Exception {
        ExecFileLoader loader = new ExecFileLoader();
        loader.load(new java.io.File("jacoco.exec"));

        return CoverageMatrix.fromJaCoCo(loader);
    }
}