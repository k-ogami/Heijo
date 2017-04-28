package rocatmonitor;

import java.io.IOException;

public class IntervalThread extends Thread
{

  private int interval_ms = 0;

  public IntervalThread(int interval_ms)
  {
    this.interval_ms = interval_ms;
  }

  @Override
  public void run()
  {
    while (true) {
      try {
        Monitor.Interval();
        Thread.sleep(interval_ms);
      } catch (IOException e) {
        System.err.println("RocatMonitorAgent:データ送信に失敗しました。監視を中断します。");
        return;
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }

}
