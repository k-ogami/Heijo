package jp.naist.rocatmonitor.connect;

import java.io.IOException;
import java.net.Socket;

import jp.naist.rocatmonitor.Agent;

public class AcceptThread extends Thread
{

  @Override
  public void run()
  {
    // スレッドIDを登録
    Agent.AgentThreadIdSet.add(Thread.currentThread().getId());

    while (true) {
      try {
        Socket socket = Agent.Connector.ServerSocket.accept();
        Agent.Connector.Clients.add(new ConnectionInfo(socket));
        Agent.IsAlive = true;
      } catch (IOException e) {
      }
    }
  }

}
