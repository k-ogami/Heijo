package jp.naist.rocatmonitor;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class DataReceiver<T> extends Thread
{

  private final Class<T> dataType;
  private final ClientConnector client;

  private List<T> dataPool = new LinkedList<>();

  public DataReceiver(ClientConnector client, Class<T> dataType)
  {
    this.dataType = dataType;
    this.client = client;
  }

  @Override
  public void run()
  {
    while (true) {
      try {
        T data = client.read(dataType);
        synchronized (dataPool) {
          dataPool.add(data);
        }
      } catch (IOException e) {
        e.printStackTrace();
        break;
      }
    }
  }

  public List<T> pop()
  {
    synchronized (dataPool) {
      if (dataPool.size() == 0) return null;
      List<T> ret = new LinkedList<>(dataPool);
      dataPool.clear();
      return ret;
    }
  }

}
