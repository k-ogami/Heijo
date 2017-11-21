package jp.naist.rocatmonitor;

import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.util.LinkedList;

import jp.naist.rocatmonitor.debug.DebugValue;
import jp.naist.rocatmonitor.message.Message;

public class Agent
{

  public static void premain(String args, Instrumentation inst)
  {
    System.out.println("Setting agent...");

    boolean success = true;
    MonitoringService service = null;

    do {
      // 設定ファイル読み込み
      Config config = new Config();
      try {
        config.load();
      } catch (Exception e) {
        System.err.println("Failed to load " + ConstValue.CONFIG_FILE_PATH);
      }

      // サーバに接続
      Connector connector = new Connector();
      if (!(DebugValue.DEBUG_FLAG && DebugValue.DEBUG_NO_CONNECT)) {
        try {
          connector.connect(config.Host, config.Port);
        } catch (Exception e) {
          System.err.println("Failed to connect " + config.Host + ":" + config.Port);
          success = false;
          break;
        }
      }

      // クラスパス以下のクラスファイルを走査して、パッケージ・クラス・メソッド名を収集・送信
      StructureDB structureDB = new StructureDB(config.IgnorePackages);
      try {
        structureDB.collectFromClassPath();
        if (!(DebugValue.DEBUG_FLAG && DebugValue.DEBUG_NO_CONNECT)) {
          Message message = new Message();
          message.MethodDatas = new LinkedList<>(structureDB.IdDataMap.values());
          connector.write(message);
        }
      } catch (IOException e) {
        System.out.println("Failed to access to class files");
        if (!(DebugValue.DEBUG_FLAG && DebugValue.DEBUG_NO_CONNECT)) {
          connector.close();
        }
        success = false;
        break;
      }

      service = new MonitoringService(config, connector, structureDB);
    } while (false);

    if (success) {
      System.out.println("Succeeded to set agent");
      if (service != null) {
        service.start();
      }
    }
  }

}
