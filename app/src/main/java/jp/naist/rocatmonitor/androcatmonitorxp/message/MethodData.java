package jp.naist.rocatmonitor.androcatmonitorxp.message;

//@org.msgpack.annotation.Message
public class MethodData
{

  //@org.msgpack.annotation.Index(0)
  public int MethodID;

  //@org.msgpack.annotation.Index(1)
  public String ClassName;

  //@org.msgpack.annotation.Index(2)
  public String MethodName;

  public MethodData()
  {
  }

  public MethodData(int methodID, String className, String methodName)
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
