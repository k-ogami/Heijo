package jp.naist.rocatmonitor.json;

public class MethodInfo
{

  public long MethodID;
  public String ClassSig;
  public String MethodSig;
  public String MethodName;

  public MethodInfo(long methodID, String classSig, String methodSig, String methodName)
  {
    MethodID = methodID;
    ClassSig = classSig;
    MethodSig = methodSig;
    MethodName = methodName;
  }

}
