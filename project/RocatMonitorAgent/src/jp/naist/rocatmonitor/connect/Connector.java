package jp.naist.rocatmonitor.connect;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import jp.naist.rocatmonitor.Agent;
import jp.naist.rocatmonitor.ConstValue;
import jp.naist.rocatmonitor.json.RootJSON;
import net.arnx.jsonic.JSON;
import net.arnx.jsonic.JSONException;

public class Connector
{

  public Object WriteLock = new Object();

  public List<ConnectionInfo> Clients = new LinkedList<ConnectionInfo>();

  public ServerSocket ServerSocket = null;

  private AcceptThread acceptThread = new AcceptThread();

  public void Start() throws IOException
  {
    ServerSocket = new ServerSocket(Agent.Config.Port);
    acceptThread.start();

    // 終了時に、途中の送信を終えるまで待機、切断するためのhookを追加
    Runtime.getRuntime().addShutdownHook(new DisconnectShutdownHook());
  }

  public void SendAll(RootJSON json)
  {

    byte[] data = JSON.encode(json).getBytes();

    Iterator<ConnectionInfo> itr = Clients.iterator();
    while (itr.hasNext()) {
      ConnectionInfo info = (ConnectionInfo)itr.next();
      try {
        // 初送信のとき、メソッド情報送信
        if (info.IsFirst) {
          info.IsFirst = false;
          Write(Agent.StructureDB.MethodInfoData, info.Socket.getOutputStream());
        }
        // 実行情報送信
        Write(data, info.Socket.getOutputStream());
      } catch (Exception e) {
        try {
          info.Socket.close();
        } catch (Exception e2) {
        }
        itr.remove();
        if (Agent.Connector.Clients.size() == 0) {
          Agent.IsAlive = false;
        }
      }
    }
  }

  public void Write(byte[] data, OutputStream stream) throws JSONException, IOException
  {
    byte[] header_inv = ByteBuffer.allocate(ConstValue.HEADER_SIZE).putInt(data.length).array();
    byte[] header = new byte[ConstValue.HEADER_SIZE];
    for (int i = 0; i < ConstValue.HEADER_SIZE; i++) {
      header[i] = header_inv[ConstValue.HEADER_SIZE - i - 1];
    }
    synchronized (WriteLock) {
      stream.write(header, 0, ConstValue.HEADER_SIZE);
      stream.write(data, 0, data.length);
    }
  }

}
