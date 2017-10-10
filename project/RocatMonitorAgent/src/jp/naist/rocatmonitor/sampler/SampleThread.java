package jp.naist.rocatmonitor.sampler;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import jp.naist.rocatmonitor.Agent;
import jp.naist.rocatmonitor.json.ExecuteInfo;

public class SampleThread extends Thread
{

  private Set<Long> calledMethodSet = new HashSet<Long>();

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
          Long methodID = Agent.StructureDB.MethodIdMap.get(name);
          if (methodID != null) {
            calledMethodSet.add(methodID);
            break;
          }
        }
      }
      for (Long methodID : calledMethodSet) {
        ExecuteInfo info = Agent.Sampler.ExeInfoMap.get(methodID);
        if (info == null) {
          info = new ExecuteInfo(methodID, 0, 0);
          Agent.Sampler.ExeInfoMap.put(methodID, info);
        }
        info.Sample++;
      }
      calledMethodSet.clear();
    }
  }

}
