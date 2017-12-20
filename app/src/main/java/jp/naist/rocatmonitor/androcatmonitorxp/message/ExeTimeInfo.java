package jp.naist.rocatmonitor.androcatmonitorxp.message;

@org.msgpack.annotation.Message
public class ExeTimeInfo
{

  @org.msgpack.annotation.Index(0)
  public long MethodID = 0;

  @org.msgpack.annotation.Index(1)
  public long ThreadID = 0;

  @org.msgpack.annotation.Index(2)
  public long ExeTime = 0;

  public ExeTimeInfo()
  {
  }

  public ExeTimeInfo(long methodID, long threadID, long exeTime)
  {
    MethodID = methodID;
    ThreadID = threadID;
    ExeTime = exeTime;
  }

}
