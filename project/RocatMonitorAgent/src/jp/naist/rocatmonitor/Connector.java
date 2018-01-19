package jp.naist.rocatmonitor;

import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;

import org.msgpack.MessagePack;

import jp.naist.rocatmonitor.debug.DebugValue;

public class Connector
{

  private Socket socket = null;
  private MessagePack msgpack = new MessagePack();

  private class ConnectingThread extends Thread
  {

    public boolean Finish = false;
    public Exception Exception = null;

    private final String host;
    private final int port;

    public ConnectingThread(String host, int port)
    {
      this.host = host;
      this.port = port;
    }

    @Override
    public void run() {
      try {
        socket = new Socket(host, port);
      } catch (Exception e) {
        Exception = e;
      }
      Finish = true;
    };

  }

  public void connect(String host, int port) throws Exception
  {
    if (DebugValue.DEBUG_FLAG && DebugValue.DEBUG_NO_CONNECT) return;

    ConnectingThread thread = new ConnectingThread(host, port);
    thread.start();
    while (!thread.Finish) {
      Thread.yield();
    }
    if (thread.Exception != null) throw thread.Exception;
    if (socket == null) throw new Exception("socket is null");
  }

  public void close()
  {
    try {
      socket.close();
    } catch (IOException e) {
    }
  }

  public <T> void write(T obj) throws IOException
  {
    byte[] payload = null;
    payload = msgpack.write(obj);
    byte[] header = ByteBuffer.allocate(ConstValue.HEADER_SIZE).putInt(payload.length).array();
    socket.getOutputStream().write(header);
    socket.getOutputStream().write(payload);
  }

}

