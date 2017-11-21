package jp.naist.rocatmonitor;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.LinkedList;
import java.util.List;

public class ServerConnecter
{

  public List<ClientConnector> Clients = new LinkedList<>();

  public void accept(int port) throws IOException
  {
    ServerSocket server = new ServerSocket(port);
    Clients.add(new ClientConnector(server.accept()));
    server.close();
  }

  public void asyncMultiAccept(int port)
  {
    Thread thread = new Thread()
    {
      @Override
      public void run()
      {
        try {
          ServerSocket server = new ServerSocket(port);
          while (true) {
            Clients.add(new ClientConnector(server.accept()));
          }
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    };
    thread.start();
  }

}
