package jp.naist.rocatmonitor.json;

public class MethodInfo
{

  public long MethodID;
  public String ClassSig;
  public String MethodName;

  public MethodInfo(long methodID, String classSig, String methodName)
  {
    MethodID = methodID;
    ClassSig = classSig;
    MethodName = methodName;
  }

}
