package jp.naist.rocatmonitor.sampler;

import jp.naist.rocatmonitor.Agent;
import jp.naist.rocatmonitor.json.CallCountInfo;
import jp.naist.rocatmonitor.json.RootJSON;
import jp.naist.rocatmonitor.json.SampleCountInfo;

public class UpdateThread extends Thread
{

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
      Update(time);
      long sleep = Agent.Config.UpdateInterval - (System.currentTimeMillis() - time);
      if (0 < sleep) {
        try {
          Thread.sleep(sleep);
        } catch (InterruptedException e) {
        }
      }
    }
  }

  private void Update(long time)
  {
    RootJSON json = new RootJSON();
    json.Time = time;
    json.Sample = Agent.Sampler.SamplingCounter;
    json.CallCountInfo = (CallCountInfo[])Agent.Sampler.CallCountMap.values().toArray(new CallCountInfo[Agent.Sampler.CallCountMap.values().size()]);
    json.SampleCountInfo = (SampleCountInfo[])Agent.Sampler.SampleCountMap.values().toArray(new SampleCountInfo[Agent.Sampler.SampleCountMap.values().size()]);
    Agent.Connector.SendAll(json);
    // 送信の度にクリア
    Agent.Sampler.CallCountMap.clear();
    Agent.Sampler.SampleCountMap.clear();
    Agent.Sampler.SamplingCounter = 0;
  }

}
