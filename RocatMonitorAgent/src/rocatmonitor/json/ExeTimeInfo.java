package rocatmonitor.json;

public class ExeTimeInfo
{

  public ExeTimeInfo(long threadID, long methodID, long exeTime)
  {
    ThreadID = threadID;
    MethodID = methodID;
    ExeTime = exeTime;
  }

  public long ThreadID;
  public long MethodID;
  public long ExeTime;

}
