package basic_sbfl_engine.runner;

import java.util.HashMap;
import java.util.Map;

/**
 * バイト配列をJVMに読み込んで実行可能にするクラス<br>
 * findClassをオーバーライドしており、
 * addDefinition()で登録したクラスは実行中に自動で読み込む
 */
public class MemoryClassLoader extends ClassLoader {
    private final Map<String, byte[]> definitions = new HashMap<>();

    /**
     * クラス名とバイト配列を結びつけて管理する
     * @param fqcn クラス名(ex: com.example.Main)
     * @param bytes バイト配列
     */
    public void addDefinition(String fqcn, byte[] bytes) {
        definitions.put(fqcn, bytes);
    }
    
    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        final byte[] bytes = definitions.get(name);
        if (bytes != null) {
            return defineClass(name, bytes, 0, bytes.length);
        }
        return super.findClass(name); // 定義がない場合は親ローダー(標準ライブラリ等)に委譲
    }
}
