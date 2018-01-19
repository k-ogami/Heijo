package jp.naist.rocatmonitor.timer;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import jp.naist.rocatmonitor.Monitor;
import jp.naist.rocatmonitor.debug.DebugValue;
import jp.naist.rocatmonitor.debug.IntervalPrinter;
import jp.naist.rocatmonitor.util.Pair;

public class SampleThread extends Thread
{

  // <メソッドID, スレッドID>
  private Set<Pair<Integer, Long>> sampledMethodIdSet = new HashSet<>();

  private IntervalPrinter debugIntervalPrinter = null;

  public SampleThread()
  {
    if (DebugValue.DEBUG_FLAG && DebugValue.DEBUG_PRINT_SAMPLE_INTERVAL_FLAG) {
      debugIntervalPrinter = new IntervalPrinter(DebugValue.DEBUG_PRINT_SAMPLE_INTERVAL_TIME, "SAMPLE");
    }
  }

  @Override
  public void run()
  {
    try {
      sample();
    } catch (Exception e) {
      System.err.println(e);
    }
  }

  private void sample()
  {
    if (DebugValue.DEBUG_FLAG && DebugValue.DEBUG_PRINT_SAMPLE_INTERVAL_FLAG) debugIntervalPrinter.interval();

    for (Map.Entry<Thread, StackTraceElement[]> entry : Thread.getAllStackTraces().entrySet()) {
      if (entry.getKey().getId() == Thread.currentThread().getId()) continue;
      for (StackTraceElement frame : entry.getValue()) {
        if (frame.isNativeMethod()) continue;
        if (Monitor.getInstance().StructureDB.ClassNameSet.contains(frame.getClassName())) {
          int methodID = Monitor.getInstance().StructureDB.NameIdMap.get(frame.getClassName() + "." + frame.getMethodName());
          sampledMethodIdSet.add(new Pair<>(methodID, entry.getKey().getId()));
          break;
        }
      }
    }

    synchronized (Monitor.getInstance().Scheduler.Lock) {
      for (Pair<Integer, Long> pair : sampledMethodIdSet) {
        Integer oldSample = Monitor.getInstance().Scheduler.SampleNumMap.get(pair);
        if (oldSample == null) {
          Monitor.getInstance().Scheduler.SampleNumMap.put(pair, 1);
        } else {
          Monitor.getInstance().Scheduler.SampleNumMap.put(pair, oldSample + 1);
        }
      }

      Monitor.getInstance().Scheduler.Counter++;
    }

    sampledMethodIdSet.clear();
  }

}
