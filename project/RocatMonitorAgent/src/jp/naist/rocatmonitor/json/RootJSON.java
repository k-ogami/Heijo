package jp.naist.rocatmonitor.json;

import java.util.LinkedList;
import java.util.List;

public class RootJSON
{

  public long Time = 0;
  public List<MethodInfo> MethodInfos = new LinkedList<MethodInfo>();
  public List<ExeTimeInfo> ExeTimeInfos = new LinkedList<ExeTimeInfo>();;

}
