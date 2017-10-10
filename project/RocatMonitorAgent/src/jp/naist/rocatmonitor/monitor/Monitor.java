package jp.naist.rocatmonitor.monitor;

import jp.naist.rocatmonitor.Agent;
import jp.naist.rocatmonitor.json.ExecuteInfo;

public class Monitor
{

  public static void MethodEnter(long methodID)
  {
    if (!Agent.IsAlive) return;

    synchronized (Agent.Sampler.CallLock) {
      ExecuteInfo info = Agent.Sampler.ExeInfoMap.get(methodID);
      if (info == null) {
        info = new ExecuteInfo(methodID, 0, 0);
        Agent.Sampler.ExeInfoMap.put(methodID, info);
      }
      info.Call++;
    }
  }

}
