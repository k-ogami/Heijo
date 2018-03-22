using MsgPack.Serialization;

public class ExeTimeInfo
{

  [MessagePackMember(0)]
  public int MethodID = 0;

  [MessagePackMember(1)]
  public long ThreadID = 0;

  [MessagePackMember(2)]
  public double ExeTime = 0;

}