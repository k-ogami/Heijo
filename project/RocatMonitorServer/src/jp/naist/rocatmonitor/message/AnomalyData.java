package jp.naist.rocatmonitor.message;

import java.util.HashMap;
import java.util.Map;

@org.msgpack.annotation.Message
public class AnomalyData
{

  @org.msgpack.annotation.Index(0)
  public float Score;

  @org.msgpack.annotation.Index(1)
  public Map<Long, Double> MethodIdSampleRateMap = new HashMap<>();

}
