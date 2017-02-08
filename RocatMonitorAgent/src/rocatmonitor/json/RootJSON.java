package rocatmonitor.json;

import java.util.LinkedList;
import java.util.List;

public class RootJSON
{

  public long Time;
  public List<MethodInfo> MethodInfos = new LinkedList<MethodInfo>();
  public List<ExeTimeInfo> ExeTimeInfos = new LinkedList<ExeTimeInfo>();

}
