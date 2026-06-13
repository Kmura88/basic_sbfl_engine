package basic_sbfl_engine.runner;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import basic_sbfl_engine.data.TestResult;
import basic_sbfl_engine.io.ClassPathScanner;

/**
 * テスト実行だけ担うクラス
 * static メソッドのrunを実装
 */

public class TestRunner {
	
    /**
     * フォルダー内のclassファイルを再帰的に全探索してテストの実行を行う
     * @param FolderPath classファイルが有るフォルダーへのパス
     * @param testClasses 実行したいtestクラス名 (nullなら全testになる)
     * @param targetClassNames test結果が欲しいクラス名 (nullなら全クラスになる)
     * @param timeout タイムアウト時間
     * @throws IOException
     */
    public static List<TestResult> run(String folderPath, List<String> testClassNames,
    									Set<String> targetClassNames, long timeout) throws IOException {
        JUnitRunner junitRunner = new JUnitRunner(ClassPathScanner.scan(folderPath), timeout);
        junitRunner.setTargetClassNames(targetClassNames);
        return junitRunner.runTests(testClassNames);
    }
}
