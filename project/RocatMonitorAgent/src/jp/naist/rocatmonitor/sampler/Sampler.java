package jp.naist.rocatmonitor.sampler;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import jp.naist.rocatmonitor.json.ExecuteInfo;

public class Sampler
{

  // 1Updateで行われたサンプリング回数をカウント
  public long SamplingCounter = 0;

  // 送信される実行情報。キーはメソッドID
  public Map<Long, ExecuteInfo> ExeInfoMap = Collections.synchronizedMap(new HashMap<Long, ExecuteInfo>());;

  public Object SampleLock = new Object();
  public Object CallLock = new Object();

  public void Start()
  {
    new SampleThread().start();
    new UpdateThread().start();
  }

}
