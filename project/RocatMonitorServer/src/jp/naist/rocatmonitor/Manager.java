package jp.naist.rocatmonitor;

import java.util.List;

import jp.naist.rocatmonitor.message.AnomalyData;
import jp.naist.rocatmonitor.message.Message;
import us.jubat.anomaly.AnomalyClient;

public class Manager
{

  private final Config config;
  private final ClientConnector agentClient;
  private final ServerConnecter visServer;
  private final Anomalier anomalier;

  private DataCollection collection = new DataCollection();

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
      int addCount = anomalier.checkAddCount(messages);
      if (0 < addCount) {
        AnomalyData[] anomalyResult = anomalier.addAnomaly(addCount, false);
        if (0 < anomalyResult.length) {

        }
      }
    }
  }

}
