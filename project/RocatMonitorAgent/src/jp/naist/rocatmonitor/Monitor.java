package jp.naist.rocatmonitor;

import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.util.LinkedList;

import jp.naist.rocatmonitor.debug.DebugValue;
import jp.naist.rocatmonitor.message.Message;
import jp.naist.rocatmonitor.timer.Scheduler;

public class Monitor
{

  private static Monitor instance = null;

  // FIXME too long
  public static Monitor getInstance()
  {
    if (instance == null) instance = new Monitor();
    return instance;
  }

  public Config Config = new Config();
  public StructureDB StructureDB = new StructureDB();
  public Connector Connector = new Connector();
  public Scheduler Scheduler = new Scheduler();

  public static void premain(String args, Instrumentation inst)
  {
    System.out.println("Setting agent...");

    boolean success = true;

    do {
      // 設定ファイル読み込み
      try {
        getInstance().Config.load();
      } catch (Exception e) {
        System.err.println("Failed to load " + ConstValue.CONFIG_FILE_PATH);
      }

      // ビジュアライザに接続
      getInstance().Connector = new Connector();
      if (!(DebugValue.DEBUG_FLAG && DebugValue.DEBUG_NO_CONNECT)) {
        try {
          getInstance().Connector.connect(getInstance().Config.Host, getInstance().Config.Port);
        } catch (Exception e) {
          System.err.println("Failed to connect " + getInstance().Config.Host + ":" + getInstance().Config.Port);
          success = false;
          break;
        }
      }

      // クラスパス以下のクラスファイルを走査して、パッケージ・クラス・メソッド名を収集・送信
      getInstance().StructureDB = new StructureDB();
      getInstance().StructureDB.IgnorePackageNameSet.addAll(getInstance().Config.IgnorePackages);
      try {
        getInstance().StructureDB.collectFromClassPath();
        if (!(DebugValue.DEBUG_FLAG && DebugValue.DEBUG_NO_CONNECT)) {
          Message message = new Message();
          message.Methods = new LinkedList<>(getInstance().StructureDB.IdDataMap.values());
          getInstance().Connector.write(message);
        }
      } catch (IOException e) {
        System.out.println("Failed to access to class files");
        if (!(DebugValue.DEBUG_FLAG && DebugValue.DEBUG_NO_CONNECT)) {
          getInstance().Connector.close();
        }
        success = false;
        break;
      }

    } while (false);

    if (success) {
      System.out.println("Succeeded to set agent");
      getInstance().Scheduler.start();
    }
  }

}
