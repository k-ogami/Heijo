package jp.naist.heijo.android.message;

@org.msgpack.annotation.Message
public class ExeTimeInfo
{

  @org.msgpack.annotation.Index(0)
  public int MethodID = 0;

  @org.msgpack.annotation.Index(1)
  public long ThreadID = 0;

  @org.msgpack.annotation.Index(2)
  public double ExeTime = 0;

  public ExeTimeInfo()
  {
  }

  public ExeTimeInfo(int methodID, long threadID, double exeTime)
  {
    MethodID = methodID;
    ThreadID = threadID;
    ExeTime = exeTime;
  }

}
