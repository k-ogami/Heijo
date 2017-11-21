package jp.naist.rocatmonitor.message;

@org.msgpack.annotation.Message
public class SamplingData
{

  @org.msgpack.annotation.Index(0)
  public long MethodID = 0;

  @org.msgpack.annotation.Index(1)
  public long Sample = 0;

  public SamplingData()
  {
  }

  public SamplingData(long methodID, long sample)
  {
    MethodID = methodID;
    Sample = sample;
  }

}
