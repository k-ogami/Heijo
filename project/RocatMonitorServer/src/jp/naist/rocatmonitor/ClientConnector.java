package jp.naist.rocatmonitor;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

import org.msgpack.MessagePack;

public class ClientConnector
{

  public Socket Socket = null;

  private MessagePack msgpack = new MessagePack();

  public ClientConnector(Socket socket)
  {
    this.Socket = socket;
  }

  public void connect(String host, int port) throws UnknownHostException, IOException
  {
    Socket = new Socket(host, port);
  }

  public void close()
  {
    try {
      Socket.close();
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
    byte[] header_inv = ByteBuffer.allocate(ConstValue.HEADER_SIZE).putInt(payload.length).array();
    byte[] header = new byte[ConstValue.HEADER_SIZE];
    for (int i = 0; i < ConstValue.HEADER_SIZE; i++) {
      header[i] = header_inv[ConstValue.HEADER_SIZE - i - 1];
    }
    Socket.getOutputStream().write(header);
    Socket.getOutputStream().write(payload);
  }

  public <T> T read(Class<T> type) throws IOException
  {
    return msgpack.read(readRaw(), type);
  }

  public byte[] readRaw() throws IOException
  {
    // ヘッダ受信
    int h_count = ConstValue.HEADER_SIZE;
    byte[] header = new byte[ConstValue.HEADER_SIZE];
    while (h_count != 0) {
      Thread.yield();
      int read = Socket.getInputStream().read(header, ConstValue.HEADER_SIZE - h_count, h_count);
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
