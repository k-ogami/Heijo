# RocatMonitorLogger
研究用・テスト用ツール。  
計測プログラムから送信されたデータをプログラム終了時にjsonファイルに出力します。  
可視化プログラムの接続と同じ要領で、先にこれを立ち上げてから計測プログラム（Java用・Android用どちらでも）を立ち上げてください。
```
java -jar RocatMonitorLogger.jar
接続待ち……
接続完了 # 計測プログラムから接続要求があった
methods.jsonを出力 # 最初にメソッド情報を出力
execution.jsonを出力 # 計測プログラムとの接続が切れたとき、あるいはctrl+Zでこちらから強制終了したときに実行時間情報を出力
```

## methods.jsonの構造
```
[
  {"ClassName":"Tetris","MethodID":0,"MethodName":"\u003Cinit\u003E"}, # "<init>"はコンストラクタ
  {"ClassName":"Tetris","MethodID":1,"MethodName":"\u003Cclinit\u003E"}, # "<clinit>"はstaticイニシャライザ
  {"ClassName":"Tetris","MethodID":2,"MethodName":"iniRead"},
  {"ClassName":"Tetris","MethodID":3,"MethodName":"colorSet"},
  ...
]
```

## execution.jsonの構造
```
[
  {
    "CurrentTime":1521187421017,
    "ExeTimes":[
      {"ExeTime":88.23529411764706,"MethodID":37,"ThreadID":1},
      {"ExeTime":11.76470588235294,"MethodID":21,"ThreadID":1}
    ]
  },
  {
    "CurrentTime":1521187421117,
    "ExeTimes":[
      {"ExeTime":100.0,"MethodID":37,"ThreadID":1}
    ]
  },
  {
    "CurrentTime":1521187421217,
    "ExeTimes":[
      {"ExeTime":96.0,"MethodID":37,"ThreadID":1},
      {"ExeTime":2.0,"MethodID":21,"ThreadID":1},
      {"ExeTime":2.0,"MethodID":38,"ThreadID":1}
    ]
  },
  ...
]
```
