package jp.naist.rocatmonitor.monitor;

import jp.naist.rocatmonitor.Agent;
import jp.naist.rocatmonitor.json.CallCountInfo;

public class Monitor
{

  public static void MethodEnter(long methodID)
  {
    if (!Agent.IsAlive) return;

    CallCountInfo info = Agent.Sampler.CallCountMap.get(methodID);
    if (info == null) {
      info = new CallCountInfo(methodID, 0);
      Agent.Sampler.CallCountMap.put(methodID, info);
    }
    info.Count++;
  }

}
