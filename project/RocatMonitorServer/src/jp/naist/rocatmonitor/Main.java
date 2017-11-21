package jp.naist.rocatmonitor;

import java.io.IOException;

import jp.naist.rocatmonitor.debug.DebugValue;
import us.jubat.anomaly.AnomalyClient;

public class Main
{

  public static void main(String[] args)
  {
    boolean success = true;
    Manager manager = null;

    do {
      // 設定ファイル読み込み
      Config config = new Config();
      try {
        config.load();
      } catch (Exception e) {
        System.err.println("Failed to load " + ConstValue.CONFIG_FILE_PATH);
      }

      // jubatusに接続
      AnomalyClient anomaly = null;
      if (!(DebugValue.DEBUG_FLAG && DebugValue.DEBUG_NO_JUBATUS)) {
        try {
          anomaly = new AnomalyClient(config.JubatusHost, config.JubatusPort, "jp.naist.rocatmonitor", 3600);
          anomaly.clear(); // 初期化兼疎通確認
          System.out.println("Succeeded to connect Jubatus server.");
        } catch (Exception e) {
          System.err.println("Failed to connect Jubatus server");
          success = false;
        }
      }

      // Agentの接続を待つ
      ClientConnector agentClient = null;
      try {
        ServerConnecter agentSever = new ServerConnecter();
        System.out.println("Waiting agent connection...");
        agentSever.accept(config.AgentPort);
        agentClient = agentSever.Clients.get(0);
        System.out.println("Succeeded to connect agent from " + agentClient.Socket.getInetAddress().getHostAddress());
      } catch (IOException e) {
        e.printStackTrace();
        success = false;
      }

      // Visualizer（複数）の接続の待機を開始する
      ServerConnecter visSever = new ServerConnecter();
      visSever.asyncMultiAccept(config.VisualizerPort);

      manager = new Manager(config, agentClient, visSever, anomaly);
    } while (false);

    if (success && manager != null) {
      manager.start();
    }
  }

}
