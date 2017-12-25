package jp.naist.rocatmonitor;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import jp.naist.rocatmonitor.message.Message;
import us.jubat.anomaly.AnomalyClient;

public class Manager
{

  private final Config config;
  private final ClientConnector agentClient;
  private final ServerConnecter visServer;
  private final Anomalier anomalier;

  private DataCollection collection = new DataCollection();

  private List<ClientConnector> visClientsList = new LinkedList<>();

  public Manager(Config config, ClientConnector agentCli, ServerConnecter visSever, AnomalyClient anomalyClient)
  {
    this.config = config;
    this.agentClient = agentCli;
    this.visServer = visSever;
    anomalier = new Anomalier(anomalyClient, collection, config);
  }

  public void start()
  {
    // Agentからのデータの非同期受け付けを開始する
    DataReceiver<Message> agentReceiver = new DataReceiver<>(agentClient, Message.class);
    agentReceiver.start();

    while (true) {
      Thread.yield();
      List<Message> messages = agentReceiver.pop();
      if (messages == null) continue;
      collection.add(messages);

      // Visualizerに送信
      if (visServer.Clients.size() != 0) {
        synchronized (visServer.Clients) {
          visClientsList.addAll(visServer.Clients);
        }
        for (Message message : messages) {
          for (ClientConnector cli : visClientsList) {
            if (cli.IsFirstWrite()) {
              message.MethodDatas = collection.MethodDatas;
            }
            try {
              cli.write(message);
              message.MethodDatas = null;
            } catch (IOException e) {
              // TODO 切断処理
              message.MethodDatas = null;
            }
          }
        }
        visClientsList.clear();
      }

      // 封印
      /*
      int addCount = anomalier.checkAddCount(messages);
      if (0 < addCount) {
        AnomalyData[] anomalyResult = anomalier.addAnomaly(addCount, false);
        if (0 < anomalyResult.length) {
        }
      }
      */
    }
  }

}
