package jp.naist.rocatmonitor.json;

public class ExecuteInfo
{

  public long MethodID = 0;

  public long Sample = 0;

  public long Call = 0;

  public ExecuteInfo(long methodID, long sample, long call)
  {
    MethodID = methodID;
    Sample = sample;
    Call = call;
  }

}
