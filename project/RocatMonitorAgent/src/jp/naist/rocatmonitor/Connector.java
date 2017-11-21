package jp.naist.rocatmonitor;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

import org.msgpack.MessagePack;

public class Connector
{

  private Socket socket = null;
  private MessagePack msgpack = new MessagePack();

  public void connect(String host, int port) throws UnknownHostException, IOException
  {
    socket = new Socket(host, port);
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
    try {
      payload = msgpack.write(obj);
    } catch (IOException e) {
      e.printStackTrace();
    }
    byte[] header = ByteBuffer.allocate(ConstValue.HEADER_SIZE).putInt(payload.length).array();
    socket.getOutputStream().write(header);
    socket.getOutputStream().write(payload);
  }

}
