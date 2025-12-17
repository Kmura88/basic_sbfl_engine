package basic_sbfl_engine.runner;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;

import basic_sbfl_engine.config.SBFLConfig;

public class JUnitRunner {

    private final SBFLConfig config;

    public JUnitRunner(SBFLConfig config) {
        this.config = config;
    }

    public void runTests() throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();

        Future<Result> future = executor.submit(() -> {
            JUnitCore junit = new JUnitCore();
            Class<?>[] tests = loadTestClasses();
            return junit.run(tests);
        });

        try {
            future.get(config.getTimeoutSec(), TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            future.cancel(true);
            throw new RuntimeException("Test execution timeout");
        } finally {
            executor.shutdownNow();
        }
    }

    private Class<?>[] loadTestClasses() throws ClassNotFoundException {
        return config.getTestClasses().stream()
                .map(name -> {
                    try {
                        return Class.forName(name);
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                })
                .toArray(Class<?>[]::new);
    }
}
