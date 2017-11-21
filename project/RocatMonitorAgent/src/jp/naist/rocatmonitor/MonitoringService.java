package jp.naist.rocatmonitor;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import jp.naist.rocatmonitor.debug.DebugValue;
import jp.naist.rocatmonitor.debug.IntervalPrinter;
import jp.naist.rocatmonitor.message.Message;
import jp.naist.rocatmonitor.message.SamplingData;

public class MonitoringService extends TimerTask
{

  private final Config config;

  private final Connector connector;

  private final StructureDB structureDB;

  private final Timer timer = new Timer();

  // sample()のたびにカウンタが進み、一定回数になるとupdate()を実行しカウンタを0に戻す
  private int counter = 0;

  // 1サンプリング間隔でサンプリングされたメソッドのIDのSet。sample()のたびにクリアされる
  private Set<Long> sampledMethodIdSet = new HashSet<>();

  // メソッドごとのサンプル数を記憶するMap。sanple()のたびに加算していき、update()のたびにクリアされる
  private Map<Long, Long> methodIdSmapleMap = new HashMap<>();

  // デバッグ用
  private IntervalPrinter debugSampleIntervalPrinter = null;
  private IntervalPrinter debugUpdateIntervalPrinter = null;

  public MonitoringService(Config config, Connector connector, StructureDB structureDB)
  {
    this.config = config;
    this.connector = connector;
    this.structureDB = structureDB;

    if (DebugValue.DEBUG_FLAG && DebugValue.DEBUG_PRINT_SAMPLE_INTERVAL_FLAG) {
      debugSampleIntervalPrinter = new IntervalPrinter(DebugValue.DEBUG_PRINT_SAMPLE_INTERVAL_TIME, "SAMPLE");
    }
    if (DebugValue.DEBUG_FLAG && DebugValue.DEBUG_PRINT_UPDATE_INTERVAL_FLAG) {
      debugUpdateIntervalPrinter = new IntervalPrinter(DebugValue.DEBUG_PRINT_UPDATE_INTERVAL_TIME, "UPDATE");
    }
  }

  public void start()
  {
    timer.scheduleAtFixedRate(this, 0, config.SampleInterval);
  }

  @Override
  public void run()
  {
    sample();
    if (counter == config.UpdateInterval / config.SampleInterval - 1) {
      update();
      counter = 0;
    } else {
      counter++;
    }
  }

  private void sample()
  {
    if (DebugValue.DEBUG_FLAG && DebugValue.DEBUG_PRINT_SAMPLE_INTERVAL_FLAG) debugSampleIntervalPrinter.interval();

    for (Entry<Thread, StackTraceElement[]> entry : Thread.getAllStackTraces().entrySet()) {
      if (entry.getKey().getId() == Thread.currentThread().getId()) continue;
      for (StackTraceElement frame : entry.getValue()) {
        if (frame.isNativeMethod()) continue;
        if (structureDB.ClassNameSet.contains(frame.getClassName())) {
          try {
            long methodID = structureDB.NameIdMap.get(frame.getClassName() + "." + frame.getMethodName());
            sampledMethodIdSet.add(methodID);
            break;
          } catch (Exception e) {
            //System.out.println(frame);
          }
        }
      }
    }

    for (Long methodID : sampledMethodIdSet) {
      Long value = methodIdSmapleMap.get(methodID);
      if (value == null) {
        methodIdSmapleMap.put(methodID, 1L);
      } else {
        methodIdSmapleMap.put(methodID, value + 1);
      }
    }

    sampledMethodIdSet.clear();
  }

  private void update()
  {
    if (DebugValue.DEBUG_FLAG && DebugValue.DEBUG_PRINT_UPDATE_INTERVAL_FLAG) debugUpdateIntervalPrinter.interval();

    Message message = new Message();
    message.Time = System.currentTimeMillis();
    message.SamplingCount = config.UpdateInterval / config.SampleInterval;
    for (Entry<Long, Long> entry : methodIdSmapleMap.entrySet()) {
      message.SamplingDatas.add(new SamplingData(entry.getKey(), entry.getValue()));
    }

    if (!(DebugValue.DEBUG_FLAG && DebugValue.DEBUG_NO_CONNECT)) {
      try {
        connector.write(message);
      } catch (IOException e) {
        System.err.println("Connection is closed.");
        timer.cancel();
      }
    }

    methodIdSmapleMap.clear();
  }

}
