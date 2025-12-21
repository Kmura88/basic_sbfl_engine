# basic_sbfl_engine

JAVAプログラムに対して基本的なSBFLを行う。
classファイルを与えることで動作する。


## Requirements
- JDK17+
- junit4

## How To Use

classファイルは用意しておく必要がある。

### example - CUI

./gradlewは必須

```console
$ ./gradlew shadowJar
```

```console
$ java -jar basic_sbfl_engine.jar -h
```

```console
$ java -jar basic_sbfl_engine.jar -p ./example -o result.csv -t example.TriangleTest -c example.Triangle
```

### example - In java project

```java
SBFL sbfl = new Ochiai();
sbfl.compute("./example",null,null,3000);
List<Suspiciousness> list = sbfl.getSusList();
```

## spec
- classファイルはフォルダを再帰的に全探索するため、階層構造に縛りがない
- 複数クラスで動くプロジェクトに対してもSBFLを行うことができる
- testが無限ループしたときの対策にタイムアウト処理がある
- 実行するtestクラスの指定が可能
- 計測対象のクラスの指定が可能

