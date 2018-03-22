package jp.naist.heijo.android.timer;

import android.util.Pair;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.robv.android.xposed.XposedBridge;
import jp.naist.heijo.android.Monitor;
import jp.naist.heijo.android.debug.DebugValue;
import jp.naist.heijo.android.debug.IntervalPrinter;

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
      XposedBridge.log(e);
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
          Integer methodID_obj = Monitor.getInstance().StructureDB.NameIdMap.get(frame.getClassName() + "." + frame.getMethodName());
          if (methodID_obj == null) continue;
          int methodID = methodID_obj;
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
