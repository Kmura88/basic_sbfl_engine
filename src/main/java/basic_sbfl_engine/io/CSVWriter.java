package basic_sbfl_engine.io;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import basic_sbfl_engine.data.Suspiciousness;


public class CSVWriter {
	
	/**
	 * 各クラスのSuspiciousnessをCSV出力する
	 * @param list Suspiciousness配列
	 * @param path 出力先
	 */
	public static void write(List<Suspiciousness> list, String path) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(path))) {
            bw.write("className,line,suspiciousness");
            bw.newLine();

            for (Suspiciousness sus : list) {
                String className = sus.getClassName();
                int len = sus.getLength();

                // 1スタートで書き出す
                for (int line = 1; line < len; line++) {
                    double value = sus.get(line);
                    bw.write(className + "," + line + "," + value);
                    bw.newLine();
                }
            }
        }
    }
}