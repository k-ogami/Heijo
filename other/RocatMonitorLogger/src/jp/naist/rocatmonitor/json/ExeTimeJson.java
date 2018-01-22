package jp.naist.rocatmonitor.json;

import java.util.LinkedList;
import java.util.List;

import jp.naist.rocatmonitor.message.ExeTimeInfo;

public class ExeTimeJson
{

  public long CurrentTime = 0;
  public List<ExeTimeInfo> ExeTimes = new LinkedList<>();

}
