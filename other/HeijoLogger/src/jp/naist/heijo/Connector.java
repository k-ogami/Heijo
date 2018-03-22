package jp.naist.heijo;

import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;

import org.msgpack.MessagePack;

public class Connector
{

  public static final int HEADER_SIZE = 4;

  public Socket Socket = null;

  private MessagePack msgpack = new MessagePack();

  public Connector(Socket socket)
  {
    this.Socket = socket;
  }

  public <T> T read(Class<T> type) throws IOException
  {
    return msgpack.read(readRaw(), type);
  }

  public byte[] readRaw() throws IOException
  {
    // ヘッダ受信
    int h_count = HEADER_SIZE;
    byte[] header = new byte[HEADER_SIZE];
    while (h_count != 0) {
      Thread.yield();
      int read = Socket.getInputStream().read(header, HEADER_SIZE - h_count, h_count);
      h_count -= read;
    }
    int payload_size = ByteBuffer.wrap(header).getInt();
    // ペイロード受信
    int p_count = payload_size;
    byte[] payload = new byte[payload_size];
    while (p_count != 0) {
      Thread.yield();
      int read = Socket.getInputStream().read(payload, payload_size - p_count, p_count);
      p_count -= read;
    }
    return payload;
  }

}