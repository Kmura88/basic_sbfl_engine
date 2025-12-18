package basic_sbfl_engine.io;

import java.io.FileWriter;
import java.util.List;

import basic_sbfl_engine.data.Suspiciousness;

public class CSVWriter {

    public static void write(String path, List<Suspiciousness> list) throws Exception {
        try (FileWriter fw = new FileWriter(path)) {
            fw.write("class,line,score\n");
            for (Suspiciousness s : list) {
                fw.write(String.format(
                    "%s,%d,%f\n",
                    s.getClassName(),
                    s.getLine(),
                    s.getValue()
                ));
            }
        }
    }
}