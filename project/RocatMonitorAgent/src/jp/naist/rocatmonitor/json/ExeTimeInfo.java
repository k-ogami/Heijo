package jp.naist.rocatmonitor.json;

public class ExeTimeInfo
{

  public long ThreadID;
  public long MethodID;
  public long ExeTime;

  public ExeTimeInfo(long threadID, long methodID, long exeTime)
  {
    ThreadID = threadID;
    MethodID = methodID;
    ExeTime = exeTime;
  }

}
