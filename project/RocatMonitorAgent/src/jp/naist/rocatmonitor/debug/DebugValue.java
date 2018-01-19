package jp.naist.rocatmonitor.debug;

public class DebugValue
{

  // falseのとき、すべてのDEBUGフラグを無視する
  public static final boolean DEBUG_FLAG = false;

  // サーバへの接続を省略する
  public static final boolean DEBUG_NO_CONNECT = false;

  // サンプリングおよび更新のレートをコンソールに表示する（FLAG：コンソール表示のフラグ，TIME：指定した回数分のレートの平均を表示する）
  public static final boolean DEBUG_PRINT_SAMPLE_INTERVAL_FLAG = false;
  public static final boolean DEBUG_PRINT_UPDATE_INTERVAL_FLAG = false;
  public static final int DEBUG_PRINT_SAMPLE_INTERVAL_TIME = 1000;
  public static final int DEBUG_PRINT_UPDATE_INTERVAL_TIME = 10;

}
