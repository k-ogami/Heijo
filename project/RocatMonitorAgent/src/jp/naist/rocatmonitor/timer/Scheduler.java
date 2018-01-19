package jp.naist.rocatmonitor.timer;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import jp.naist.rocatmonitor.Monitor;
import jp.naist.rocatmonitor.util.Pair;

public class Scheduler
{

  public ScheduledExecutorService Executor = Executors.newScheduledThreadPool(2);

  Object Lock = new Object();

  public SampleThread Sampler;
  public UpdateThread Updater;


  // sampleした回数。updateでクリアされる
  public int Counter = 0;

  // <<メソッドID, スレッドID>, サンプル数>
  // samplerで書き込まれ、updaterで送信後クリアされる。同期用のlockerも兼ねる
  public Map<Pair<Integer, Long>, Integer> SampleNumMap = new HashMap<>();

  private static final int MILISEC_TO_NANOSEC = 1000000;

  public Scheduler()
  {
    Sampler = new SampleThread();
    Updater = new UpdateThread();
  }

  public void start()
  {
    long sInterval = (long)(Monitor.getInstance().Config.SampleInterval * MILISEC_TO_NANOSEC);
    long uInterval = (long)(Monitor.getInstance().Config.UpdateInterval * MILISEC_TO_NANOSEC);
    Executor.scheduleAtFixedRate(Sampler, sInterval, sInterval, TimeUnit.NANOSECONDS);
    Executor.scheduleAtFixedRate(Updater, uInterval, uInterval, TimeUnit.NANOSECONDS);
  }

}
