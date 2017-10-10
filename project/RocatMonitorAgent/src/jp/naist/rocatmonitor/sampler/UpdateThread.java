package jp.naist.rocatmonitor.sampler;

import jp.naist.rocatmonitor.Agent;
import jp.naist.rocatmonitor.detect.Detector;
import jp.naist.rocatmonitor.json.ExecuteInfo;
import jp.naist.rocatmonitor.json.RootJSON;

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

  private Detector detector = new Detector();

  private void Update(long time)
  {
    RootJSON json = new RootJSON();
    json.Time = time;
    synchronized (Agent.Sampler.SampleLock) {
      synchronized (Agent.Sampler.CallLock) {
        json.Sample = Agent.Sampler.SamplingCounter;
        json.ExecuteInfo = Agent.Sampler.ExeInfoMap.values().toArray(new ExecuteInfo[] {});
      }
    }

    //Agent.Connector.SendAll(json);
    detector.putData(json);


    // 送信の度にクリア
    Agent.Sampler.ExeInfoMap.clear();
    Agent.Sampler.SamplingCounter = 0;
  }

}
