using MsgPack.Serialization;

public class MethodInfo
{

  [MessagePackMember(0)]
  public long MethodID;

  [MessagePackMember(1)]
  public string ClassName;

  [MessagePackMember(2)]
  public string MethodName;

}
