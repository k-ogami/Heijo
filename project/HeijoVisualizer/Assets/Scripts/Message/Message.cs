using MsgPack.Serialization;
using System.Collections.Generic;

public class Message
{

  [MessagePackMember(0)]
  public long CurrentTime = 0;

  [MessagePackMember(1)]
  public double TimeLength = 0;

  [MessagePackMember(2)]
  public List<MethodInfo> Methods = null;

  [MessagePackMember(3)]
  public List<ExeTimeInfo> ExeTimes = null;

}
