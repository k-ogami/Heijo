package rocatmonitor;

import java.io.IOException;

public class IntervalThread extends Thread
{

  private int interval_ms = 0;

  private boolean alive = false;

  public IntervalThread(int interval_ms)
  {
    this.interval_ms = interval_ms;
  }

  @Override
  public void run()
  {
    alive = true;

    while (alive) {
      try {
        Monitor.Interval();
        Thread.sleep(interval_ms);
      } catch (IOException e) {
        if (alive) {
          Monitor.IsAlive = false;
          // System.err.println("RocatMonitorAgent:データ送信に失敗しました。監視を中断します。");
          System.err.println("AgentError:Connection failed.");
        }
        return;
      } catch (InterruptedException e) {
        return;
      }
    }
  }

  public void Destroy()
  {
    alive = false;
  }

}
