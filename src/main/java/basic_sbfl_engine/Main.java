package basic_sbfl_engine;

import java.util.Map;

import basic_sbfl_engine.io.CSVWriter;
import basic_sbfl_engine.io.ClassPathScanner;
import basic_sbfl_engine.runner.JUnitRunner;
import basic_sbfl_engine.sbfl.Ochiai;
import basic_sbfl_engine.sbfl.SBFL;

public class Main {
	
    public static void main(String[] args) throws Exception {
    	// byte [] 読み込み
    	Map<String, byte[]> AAAA = ClassPathScanner.scan("example/sample1");
    	System.out.println(AAAA );
    	
    	JUnitRunner junitRunner = new JUnitRunner(AAAA, 3000);
    	SBFL sbfl = new Ochiai();
    	sbfl.compute(junitRunner.runTests("TriangleTest"));
    	CSVWriter.write(sbfl.getSusList(), "result.csv");
    }
}
