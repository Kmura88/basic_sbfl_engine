package basic_sbfl_engine.io;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import basic_sbfl_engine.data.Suspiciousness;
import basic_sbfl_engine.sbfl.SBFL;


public class CSVWriter {
	
	/**
	 * 各クラスのSuspiciousnessをCSV出力する
	 * @param list Suspiciousness配列
	 * @param path 出力先
	 */
	public static void write(SBFL sbfl, String path) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(path))) {
            bw.write("className,line,ef,nf,ep,np,suspiciousness");
            bw.newLine();

            List<Suspiciousness> list = sbfl.getSusList();
            
            for (Suspiciousness sus : list) {
                String className = sus.getClassName();
                int len = sus.getMaxLine();

                // 1スタートで書き出す
                for (int line = 1; line <= len; line++) {
                    double value = sus.get(line);
                    bw.write(className + "," + line
                    		+ "," + sbfl.getEf(className, line) + "," + sbfl.getNf(className, line) 
                    		+ "," + sbfl.getEp(className, line) + "," + sbfl.getNp(className, line)
                    		+ "," + value);
                    bw.newLine();
                }
            }
        }
    }
}