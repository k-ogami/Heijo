package jp.naist.rocatmonitor.androcatmonitorxp.message;

@org.msgpack.annotation.Message
public class MethodInfo
{

  @org.msgpack.annotation.Index(0)
  public int MethodID;

  @org.msgpack.annotation.Index(1)
  public String ClassName;

  @org.msgpack.annotation.Index(2)
  public String MethodName;

  public MethodInfo()
  {
  }

  public MethodInfo(int methodID, String className, String methodName)
  {
    MethodID = methodID;
    ClassName = className;
    MethodName = methodName;
  }

  @Override
  public String toString()
  {
    return ClassName + "." + MethodName;
  }

}
