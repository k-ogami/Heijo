package jp.naist.rocatmonitor.message;

@org.msgpack.annotation.Message
public class MethodData
{

  @org.msgpack.annotation.Index(0)
  public long MethodID;

  @org.msgpack.annotation.Index(1)
  public String ClassSig;

  @org.msgpack.annotation.Index(2)
  public String MethodName;

  public MethodData()
  {
  }

  public MethodData(long methodID, String classSig, String methodName)
  {
    MethodID = methodID;
    ClassSig = classSig;
    MethodName = methodName;
  }

  @Override
  public String toString()
  {
    return ClassSig + "." + MethodName;
  }

}
