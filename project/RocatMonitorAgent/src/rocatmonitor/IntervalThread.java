package rocatmonitor;

import java.io.IOException;

public class IntervalThread extends Thread
{

  public int Interval;

  @Override
  public void run()
  {
    while (true) {
      try {
        Monitor.Interval();
        Thread.sleep(Interval);
      } catch (IOException e) {
        System.err.println("RocatMonitorAgent:データ送信に失敗しました。監視を中断します。");
        return;
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }

  }

}
