package basic_sbfl_engine.config;

import java.nio.file.Path;
import java.util.List;

// TODO picocli使う
public class SBFLConfig {

    private Path projectPath;
    private List<String> testClasses;
    private int timeoutSec;
    private Path outputPath;

    public static SBFLConfig fromArgs(String[] args) {
        // 簡易実装（実用では picocli 推奨）
        // --project, --tests, --timeout, --out
        return new SBFLConfig();
    }

    // getter省略
    
    public Path getOutputPath() {
    	return outputPath;
    }
}