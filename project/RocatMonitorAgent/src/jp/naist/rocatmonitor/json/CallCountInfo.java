package jp.naist.rocatmonitor.json;

public class CallCountInfo
{

  public long MethodID;

  public long Count;

  public CallCountInfo(long methodID, long count)
  {
    MethodID = methodID;
    Count = count;
  }

}
