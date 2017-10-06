package jp.naist.rocatmonitor.sampler;

import java.util.Map;

import jp.naist.rocatmonitor.Agent;
import jp.naist.rocatmonitor.json.SampleCountInfo;
import jp.naist.rocatmonitor.monitor.ThreadMethodKey;

public class SampleThread extends Thread
{

  private Map<ThreadMethodKey, SampleCountInfo> sampleCountMap = null;

  public SampleThread(Map<ThreadMethodKey, SampleCountInfo> sampleCountMap)
  {
    this.sampleCountMap = sampleCountMap;
  }

  @Override
  public void run()
  {
    // スレッドIDを登録
    Agent.AgentThreadIdSet.add(Thread.currentThread().getId());

    while (true) {
      while (!Agent.IsAlive) {
        Thread.yield();
      }
      long time = System.currentTimeMillis();
      Sample();
      long sleep = Agent.Config.SampleInterval - (System.currentTimeMillis() - time);
      if (0 < sleep) {
        try {
          Thread.sleep(sleep);
        } catch (InterruptedException e) {
        }
      }
    }
  }

  private void Sample()
  {
    synchronized (Agent.Sampler.SampleLock) {
      Agent.Sampler.SamplingCounter++;

      for (Map.Entry<Thread, StackTraceElement[]> entry : Thread.getAllStackTraces().entrySet()) {
        // エージェントのスレッドは無視
        if (Agent.AgentThreadIdSet.contains(entry.getKey().getId())) continue;

        // 監視対象のメソッドに辿り着くまで、スタックトレースの最後から順に探索
        for (int i = entry.getValue().length - 1; 0 <= i; i--) {
          StackTraceElement frame = entry.getValue()[i];
          if (frame.isNativeMethod()) continue;
          String name = frame.getClassName().replace('.', '/') + "/" + frame.getMethodName();
          Long id = Agent.StructureDB.MethodIdMap.get(name);
          if (id != null) {
            // サンプル数を1増やす
            ThreadMethodKey key = new ThreadMethodKey(entry.getKey().getId(), id);
            SampleCountInfo info = sampleCountMap.get(key);
            if (info == null) {
              info = new SampleCountInfo(key.ThreadID, key.MethodID, 0);
              sampleCountMap.put(key, info);
            }
            info.Count++;
            break;
          }
        }
      }
    }
  }

}
