package jp.naist.rocatmonitor.connect;

import java.net.Socket;

public class ConnectionInfo
{

  public boolean IsFirst = true;
  public Socket Socket = null;

  public ConnectionInfo(Socket socket)
  {
    Socket = socket;
  }

}
