package rocatmonitor;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;

import net.arnx.jsonic.JSON;
import rocatmonitor.json.RootJSON;

public class Connector
{

  public static final int HEADER_SIZE = 8;
  public static final String JSON_CHARSET = "UTF-8";

  public Socket Socket = null;
  public OutputStream Stream = null;

  public void Connect(String host, int port) throws IOException
  {
    Socket = new Socket(host, port);
    Stream = Socket.getOutputStream();
  }

  public void Send(RootJSON json) throws IOException
  {
    if (Agent.DEBUG_NO_CONNECT) {
      System.out.println(JSON.encode(json));
      return;
    }

    byte[] payload = JSON.encode(json).getBytes(JSON_CHARSET);
    byte[] header_inv = ByteBuffer.allocate(HEADER_SIZE).putInt(payload.length).array();
    byte[] header = new byte[HEADER_SIZE];
    for (int i = 0; i < HEADER_SIZE; i++) {
      header[i] = header_inv[HEADER_SIZE - i - 1];
    }
    Stream.write(header, 0, HEADER_SIZE);
    Stream.write(payload, 0, payload.length);
  }

}
