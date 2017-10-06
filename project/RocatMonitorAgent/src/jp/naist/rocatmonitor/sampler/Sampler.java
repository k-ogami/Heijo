package jp.naist.rocatmonitor.sampler;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import jp.naist.rocatmonitor.json.CallCountInfo;
import jp.naist.rocatmonitor.json.SampleCountInfo;
import jp.naist.rocatmonitor.monitor.ThreadMethodKey;

public class Sampler
{

  // 1updateあたりに行ったサンプリング回数のカウンタ
  public int SamplingCounter = 0;

  // サンプル数のカウンタ。キーはスレッドIDとメソッドID
  public Map<ThreadMethodKey, SampleCountInfo> SampleCountMap = Collections.synchronizedMap(new HashMap<ThreadMethodKey, SampleCountInfo>());

  // 呼び出し回数のカウンタ。キーはメソッドID
  public Map<Long, CallCountInfo> CallCountMap = Collections.synchronizedMap(new HashMap<Long, CallCountInfo>());

  public Object SampleLock = new Object();
  public Object CallLock = new Object();

  public void Start()
  {
    new SampleThread(SampleCountMap).start();
    new UpdateThread().start();
  }

}
