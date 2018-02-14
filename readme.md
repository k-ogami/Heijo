# RocatMonitor
奈良先端科学技術大学院大学 情報科学研究科 ソフトウェア工学研究室 大神による、『リアルタイム3Dプロファイラ』のリポジトリ。ツール名未定。

## 使い方
（詳細は後日書きます。）

取り急ぎ、実行方法について。

必要なプログラムはreleaseディレクトリにあります。

まず、RocatMonitorVisualizerを実行して、可視化プログラムを起動させます。
Windows版とMac版をビルドしてあります。

Javaアプリケーションのプロファイリングを行う場合は、以下のようなコマンドを実行します。（適宜ファイルパスを指定）
java -javaagent:"RocatMonitorAgent.jar" -jar TargetApp.jar

Androidアプリケーションのプロファイリングを行う場合は、Xposed FrameworkがインストールされたAndroidが必要です。
実機を使用する場合はroot化などやや危険で面倒な作業が伴うので、ワンクリックでroot化が可能なAndroid仮想マシンであるNoxを使用することをお勧めします。
AndroidにAndrocatMonitorXP.apkをインストールさせ、Xposed Frameworkの画面からAndrocatMonitorXPを有効にした後、一度Androidを再起動します。
その後、AndrocatMonitorXPを起動させれば、プロファイリング開始のための設定画面が開きます。
