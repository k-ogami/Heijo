package jp.naist.rocatmonitor.message;

import java.util.LinkedList;
import java.util.List;

@org.msgpack.annotation.Message
public class Message
{

  @org.msgpack.annotation.Index(0)
  public long Time = 0;

  @org.msgpack.annotation.Index(1)
  public long SamplingCount = 0;

  @org.msgpack.annotation.Index(2)
  public List<MethodData> MethodDatas = new LinkedList<>();

  @org.msgpack.annotation.Index(3)
  public List<SamplingData> SamplingDatas = new LinkedList<>();

  @org.msgpack.annotation.Optional
  @org.msgpack.annotation.Index(4)
  public List<AnomalyData> AnomalyDatas = new LinkedList<>();

}
