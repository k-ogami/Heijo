package jp.naist.rocatmonitor.androcatmonitorxp.timer;

import android.util.Pair;

import java.io.IOException;
import java.util.Map;

import de.robv.android.xposed.XposedBridge;
import jp.naist.rocatmonitor.androcatmonitorxp.Monitor;
import jp.naist.rocatmonitor.androcatmonitorxp.debug.DebugValue;
import jp.naist.rocatmonitor.androcatmonitorxp.debug.IntervalPrinter;
import jp.naist.rocatmonitor.androcatmonitorxp.message.ExeTimeInfo;
import jp.naist.rocatmonitor.androcatmonitorxp.message.Message;

public class UpdateThread extends Thread
{

  private boolean isFirstSend = true;

  private IntervalPrinter debugIntervalPrinter = null;

  public UpdateThread()
  {
    if (DebugValue.DEBUG_FLAG && DebugValue.DEBUG_PRINT_UPDATE_INTERVAL_FLAG) {
      debugIntervalPrinter = new IntervalPrinter(DebugValue.DEBUG_PRINT_UPDATE_INTERVAL_TIME, "UPDATE");
    }
  }

  @Override
  public void run()
  {
    try {
      if (isFirstSend) {
        firstSend();
        isFirstSend = false;
      }
    } catch (Exception e) {
      XposedBridge.log(e);
    }

    try {
      update();
    } catch (Exception e) {
      XposedBridge.log(e);
    }
  }

  private void update()
  {
    if (DebugValue.DEBUG_FLAG && DebugValue.DEBUG_PRINT_UPDATE_INTERVAL_FLAG) debugIntervalPrinter.interval();

    Message message = new Message();

    synchronized (Monitor.getInstance().Scheduler.Lock) {
      message.CurrentTime = System.currentTimeMillis();
      message.TimeLength = Monitor.getInstance().Config.UpdateInterval;

      for (Map.Entry<Pair<Integer, Long>, Integer> entry : Monitor.getInstance().Scheduler.SampleNumMap.entrySet()) {
        int methodID = entry.getKey().first;
        long threadID = entry.getKey().second;
        double exeRate = (double) entry.getValue() / Monitor.getInstance().Scheduler.Counter;
        double exeTime = exeRate * message.TimeLength;
        ExeTimeInfo info = new ExeTimeInfo(methodID, threadID, exeTime);
        message.ExeTimes.add(info);
      }

      Monitor.getInstance().Scheduler.Counter = 0;
      Monitor.getInstance().Scheduler.SampleNumMap.clear();
    }

    if (!(DebugValue.DEBUG_FLAG && DebugValue.DEBUG_NO_CONNECT)) {
      try {
        Monitor.getInstance().Connector.write(message);
      } catch (IOException e) {
        XposedBridge.log("Connection is closed");
        Monitor.getInstance().Scheduler.Executor.shutdownNow();
      }
    }
  }

  // TODO メソッド情報の送信はMainActivity上での操作中に終わらせて、街の構築が終わるまで待機する仕様にしたい
  private void firstSend()
  {
    if (DebugValue.DEBUG_FLAG && DebugValue.DEBUG_NO_CONNECT) return;

    Message message = new Message();
    message.CurrentTime = System.currentTimeMillis();
    message.TimeLength = 0;
    message.Methods.addAll(Monitor.getInstance().StructureDB.IdDataMap.values());
    try {
      Monitor.getInstance().Connector.write(message);
    } catch (IOException e) {
      XposedBridge.log("Connection is closed");
      Monitor.getInstance().Scheduler.Executor.shutdownNow();
    }
  }

}
