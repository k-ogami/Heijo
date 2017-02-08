package rocatmonitor.json;

public class MethodInfo
{

  public MethodInfo(long methodID, String classSig, String methodSig, String methodName)
  {
    MethodID = methodID;
    ClassSig = classSig;
    MethodSig = methodSig;
    MethodName = methodName;
  }

  public long MethodID;
  public String ClassSig;
  public String MethodSig;
  public String MethodName;

}
