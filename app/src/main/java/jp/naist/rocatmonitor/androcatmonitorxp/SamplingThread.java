package jp.naist.rocatmonitor.androcatmonitorxp;

import android.util.Pair;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import de.robv.android.xposed.XposedBridge;
import jp.naist.rocatmonitor.androcatmonitorxp.debug.DebugValue;
import jp.naist.rocatmonitor.androcatmonitorxp.debug.IntervalPrinter;
import jp.naist.rocatmonitor.androcatmonitorxp.message.ExeTimeInfo;
import jp.naist.rocatmonitor.androcatmonitorxp.message.Message;

public class SamplingThread extends TimerTask
{

  private static final int MILISEC_TO_NANOSEC = 1000000;

  private final Timer timer = new Timer();

  // sample()のたびにカウンタが進み、一定回数になるとupdate()を実行しカウンタを0に戻す
  private int counter = 0;

  // <メソッドID, スレッドID>
  private Set<Pair<Long, Long>> sampledMethodIdSet = new HashSet<>();

  // <<メソッドID, スレッドID>, サンプル数>
  private Map<Pair<Long, Long>, Long> sampleNumMap = new HashMap<>();

  private boolean isFirstSend = true;

  private IntervalPrinter debugSampleIntervalPrinter = null;
  private IntervalPrinter debugUpdateIntervalPrinter = null;

  public SamplingThread()
  {
    if (DebugValue.DEBUG_FLAG && DebugValue.DEBUG_PRINT_SAMPLE_INTERVAL_FLAG) {
      debugSampleIntervalPrinter = new IntervalPrinter(DebugValue.DEBUG_PRINT_SAMPLE_INTERVAL_TIME, "SAMPLE");
    }
    if (DebugValue.DEBUG_FLAG && DebugValue.DEBUG_PRINT_UPDATE_INTERVAL_FLAG) {
      debugUpdateIntervalPrinter = new IntervalPrinter(DebugValue.DEBUG_PRINT_UPDATE_INTERVAL_TIME, "UPDATE");
    }
  }

  public void start()
  {
    timer.scheduleAtFixedRate(this, 0, Monitor.getInstance().Config.SampleInterval);
  }

  @Override
  public void run()
  {
    try {
      if (isFirstSend) {
        firstSend();
        isFirstSend = false;
      }
      sample();
      if (counter == Monitor.getInstance().Config.UpdateInterval / Monitor.getInstance().Config.SampleInterval - 1) {
        update();
        counter = 0;
      } else {
        counter++;
      }
    } catch (Exception e) {
      XposedBridge.log(e);
    }
  }

  private void sample()
  {
    if (DebugValue.DEBUG_FLAG && DebugValue.DEBUG_PRINT_SAMPLE_INTERVAL_FLAG) debugSampleIntervalPrinter.interval();

    for (Map.Entry<Thread, StackTraceElement[]> entry : Thread.getAllStackTraces().entrySet()) {
      if (entry.getKey().getId() == Thread.currentThread().getId()) continue;
      for (StackTraceElement frame : entry.getValue()) {
        if (frame.isNativeMethod()) continue;
        if (Monitor.getInstance().StructureDB.ClassNameSet.contains(frame.getClassName())) {
          long methodID = Monitor.getInstance().StructureDB.NameIdMap.get(frame.getClassName() + "." + frame.getMethodName());
          sampledMethodIdSet.add(new Pair<Long, Long>(methodID, entry.getKey().getId()));
          break;
        }
      }
    }

    for (Pair<Long, Long> pair : sampledMethodIdSet) {
      Long oldSample = sampleNumMap.get(pair);
      if (oldSample == null) {
        sampleNumMap.put(pair, 1L);
      } else {
        sampleNumMap.put(pair, oldSample + 1);
      }
    }

    sampledMethodIdSet.clear();
  }

  private void update()
  {
    if (DebugValue.DEBUG_FLAG && DebugValue.DEBUG_PRINT_UPDATE_INTERVAL_FLAG) debugUpdateIntervalPrinter.interval();

    Message message = new Message();
    message.CurrentTime = System.nanoTime();
    message.TimeLength = Monitor.getInstance().Config.UpdateInterval * MILISEC_TO_NANOSEC;
    for (Map.Entry<Pair<Long, Long>, Long> entry : sampleNumMap.entrySet()) {
      Long methodID = entry.getKey().first;
      Long threadID = entry.getKey().second;
      double exeRate = (double)entry.getValue() / Monitor.getInstance().Config.UpdateInterval * Monitor.getInstance().Config.SampleInterval;
      Long exeTime =  (long)(exeRate * message.TimeLength);
      ExeTimeInfo info = new ExeTimeInfo(methodID, threadID, exeTime);
      message.ExeTimes.add(info);
    }

    if (!(DebugValue.DEBUG_FLAG && DebugValue.DEBUG_NO_CONNECT)) {
      try {
        Monitor.getInstance().Connector.write(message);
      } catch (IOException e) {
        XposedBridge.log("Connection is closed");
        timer.cancel();
      }
    }

    sampleNumMap.clear();
  }

  private void firstSend()
  {
    if (DebugValue.DEBUG_FLAG && DebugValue.DEBUG_NO_CONNECT) return;

    Message message = new Message();
    message.CurrentTime = System.nanoTime();
    message.TimeLength = 0;
    message.Methods.addAll(Monitor.getInstance().StructureDB.IdDataMap.values());
    try {
      Monitor.getInstance().Connector.write(message);
    } catch (IOException e) {
      XposedBridge.log("Connection is closed");
      timer.cancel();
    }
  }

}
