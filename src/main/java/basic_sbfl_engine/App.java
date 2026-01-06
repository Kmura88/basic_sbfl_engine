package basic_sbfl_engine;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import basic_sbfl_engine.io.CSVWriter;
import basic_sbfl_engine.io.ClassPathScanner;
import basic_sbfl_engine.runner.JUnitRunner;
import basic_sbfl_engine.sbfl.Ochiai;
import basic_sbfl_engine.sbfl.SBFL;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name="basic_sbfl_engine", mixinStandardHelpOptions = true, version="1.2")
public class App implements Runnable{
	
	@Option(names = {"-p","--path"}, description="Path to Folder which contains class files", required = true)
	private String classFolderPath;
	
	@Option(names = {"-o","--output"}, description="Define the output CSV file")
	private String CSVPath;
	
	@Option(names = {"-t","--test"}, description="Run only selected test classes", split = ",")
	private List<String> testClassNames;
	
	@Option(names = {"-c","--class"}, description="Specify the class used to compute SBFL scores", split = ",")
	private Set<String> targetClassNames;
	
	@Option(names = "--timeout", description="Set the timeout(ms) for each test method.", defaultValue = "2000")
	private long timeout;
	
	@Option(names = {"-v", "--verbose"}, description = "Show test execution logs (std out/err).")
    private boolean verbose;
	
    public static void main(String[] args){
    	System.exit(new CommandLine(new App()).execute(args));
    }
    
    @Override
    public void run() {
        try {
        	
        	// 0. 初期化・設定
            SBFL sbfl = new Ochiai();
            JUnitRunner junitRunner = new JUnitRunner(ClassPathScanner.scan(classFolderPath), timeout);
            junitRunner.setSuppressOutput(!this.verbose);
            junitRunner.setTargetClassNames(targetClassNames);
            
            // 1. 計算
            System.out.println("Running SBFL...");
            sbfl.compute(junitRunner.runTests(testClassNames));
            
            // 2. 結果の出力
            if(CSVPath !=null) {
            	System.out.println("Writing results to: " + CSVPath);
            	CSVWriter.write(sbfl.getSusList(), CSVPath);
            }else {
            	System.out.println("TotalPassed : " + sbfl.getTotalPassed());
            	System.out.println("TotalFailed : " + sbfl.getTotalFailed());
            	System.out.println("[Info] : Use the -o option to get more detailed output.");
            }
            System.out.println("Done.");

        } catch (IOException e) {
            System.err.println("IO Error: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
