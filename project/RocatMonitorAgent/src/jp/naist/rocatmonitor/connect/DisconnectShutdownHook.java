package jp.naist.rocatmonitor.connect;

import java.io.IOException;

import jp.naist.rocatmonitor.Agent;

public class DisconnectShutdownHook extends Thread
{

  @Override
  public void run()
  {
    synchronized (Agent.Connector.WriteLock) {
      try {
        Agent.Connector.ServerSocket.close();
      } catch (IOException e) {
      }
      for ( ConnectionInfo info : Agent.Connector.Clients) {
        try {
          info.Socket.close();
        } catch (IOException e1) {
        }
      }
    }

  }

}
