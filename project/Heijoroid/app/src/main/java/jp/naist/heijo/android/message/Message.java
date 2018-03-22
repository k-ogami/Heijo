package jp.naist.heijo.android.message;

import java.util.LinkedList;
import java.util.List;

@org.msgpack.annotation.Message
public class Message
{

  @org.msgpack.annotation.Index(0)
  public long CurrentTime = 0;

  @org.msgpack.annotation.Index(1)
  public double TimeLength = 0;

  @org.msgpack.annotation.Index(2)
  public List<MethodInfo> Methods = new LinkedList<>();

  @org.msgpack.annotation.Index(3)
  public List<ExeTimeInfo> ExeTimes = new LinkedList<>();

}
