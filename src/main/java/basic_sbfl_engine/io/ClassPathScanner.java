package basic_sbfl_engine.io;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.objectweb.asm.ClassReader;

/**
 * 指定されたディレクトリを再帰的に探索し、クラスファイルを集めるクラス
 * ASMを使用してバイトコードから直接クラス名を特定する
 */
public class ClassPathScanner {

    /**
     * 指定されたルートパス以下の.classファイルを全て読み込む
     * @param rootPathStr 探索開始ディレクトリのパス (例: "target/classes")
     * @return Map<完全修飾クラス名, バイト配列>
     * @throws IOException ファイル読み込みエラー時
     */
    public static Map<String, byte[]> scan(String rootPathStr) throws IOException {
        Path rootPath = Paths.get(rootPathStr);
        if (!Files.exists(rootPath) || !Files.isDirectory(rootPath)) {
            throw new IllegalArgumentException("指定されたパスが存在しないか、ディレクトリではありません: " + rootPathStr);
        }

        Map<String, byte[]> classMap = new HashMap<>();

        try (Stream<Path> walk = Files.walk(rootPath)) {
            walk.filter(path -> !Files.isDirectory(path))
                .filter(path -> path.toString().endsWith(".class"))
                .forEach(path -> {
                    try {
                        byte[] bytes = Files.readAllBytes(path);
                        // ここでバイトコードを解析してクラス名を取得
                        String className = readClassName(bytes);
                        
                        // module-info.class など、無名パッケージや特殊なクラスを除外したい場合はここでcheck
                        if (className != null && !className.equals("module-info")) {
                            classMap.put(className, bytes);
                        }
                    } catch (Exception e) {
                        System.err.println("クラスファイルの読み込みまたは解析に失敗しました: " + path);
                        e.printStackTrace();
                    }
                });
        }

        return classMap;
    }

    /**
     * ASMを使用してバイト配列からクラス名を抽出する
     * @param bytes .classファイルのバイト列
     * @return 完全修飾クラス名 (例: "com.example.Main")
     */
    private static String readClassName(byte[] bytes) {
        ClassReader reader = new ClassReader(bytes);
        return reader.getClassName().replace('/', '.');
    }
}