package jp.naist.rocatmonitor.json;

public class SampleCountInfo
{

  public long ThreadID = 0;
  public long MethodID = 0;
  public long Count = 0;

  public SampleCountInfo(long threadID, long methodID, long count)
  {
    ThreadID = threadID;
    MethodID = methodID;
    Count = count;
  }

}
