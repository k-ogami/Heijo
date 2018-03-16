# プロファイリングの始め方
1. 可視化プログラムの実行  
RocatMonitorVisualizerを実行します（Windows版とMac版がビルド済み）。  
2. 計測プログラムとアプリケーションの実行
 * Javaアプリケーションの場合  
以下のようなコマンドを実行します（適宜ファイルパスを指定）。  
可視化プログラムとの接続が成功すれば、プロファイリングが開始します。
```
 java -javaagent:RocatMonitorAgent.jar -jar TargetApp.jar
```

 * Androidアプリケーションの場合
root化の作業とXposed FrameworkがインストールされたAndroid端末が必要で、準備がそれなりに大変かつ危険です。  
Android仮想マシンであるNoxを使用する場合、ワンクリックでroot化のon-offが可能なので非常に楽です。  
これらの準備ができたら、AndroidにAndrocatMonitorXP.apkをインストールして、Xposed Installerの画面から
AndroidInstallerの画面からAndrocatMonitorXPのモジュールを有効にして、一度Androidを再起動します。  
その後、ホーム画面からAndrocatMonitorXPを起動させれば、プロファイリング開始のための設定画面が開きます。

# 可視化画面での操作方法
* カメラ操作
現在、マウス操作にのみ対応しています。  
回転：右ボタンを押したままドラッグ  
移動：ホイールを押したままドラッグ  
ズーム：ホイールを回転
* ブロックの高さの調整  
画面下のスライダを動かして、ブロックの高さに定数倍の補正をかけられます。
* 可視化レベルの変更  
ブロックを左ボタンでダブルクリックすると、ブロックを個別に折り畳んで非表示にすることができます。  
画面右下のボタンを押して、パッケージ・クラス・メソッドのレベルで一括で折り畳むこともできます。  
プロファイリング開始時にはパッケージレベルで表示されます。

# その他の操作
RocatMonitorAgent.jarの中に含まれているCONFIG.propertiesを編集して、パラメータの変更を行うことができます。  
編集のたびに解凍と圧縮を繰り返すのは面倒なので、7-Zipなどを使用すると便利です（jarはzipと同様の圧縮ファイル）。
```
# 送信先のホスト
HOST = localhost
# 送信先のポート
PORT = 8000
# サンプリング間隔[ms]
SAMPLE_INTERVAL = 2
# 更新間隔[ms]
UPDATE_INTERVAL = 100
# サンプリング対象から除外するパッケージ名
# IGNORE_PACKAGE = com.example.*, org.example.*
```
