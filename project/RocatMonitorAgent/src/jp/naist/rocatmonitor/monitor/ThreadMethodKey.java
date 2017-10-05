package jp.naist.rocatmonitor.monitor;

public class ThreadMethodKey implements Comparable<ThreadMethodKey>
{

  public long ThreadID = 0;
  public long MethodID = 0;

  public ThreadMethodKey(long threadID, long methodID)
  {
    ThreadID = threadID;
    MethodID = methodID;
  }

  @Override
  public boolean equals(Object obj)
  {
    if (obj instanceof ThreadMethodKey) {
      ThreadMethodKey other = (ThreadMethodKey)obj;
      return this.ThreadID == other.ThreadID && this.MethodID == other.MethodID;
    }
    return false;
  }

  @Override
  public int hashCode()
  {
    return (int)MethodID << 16 + ThreadID;
  }

  @Override
  public int compareTo(ThreadMethodKey o)
  {
    return Long.compare(this.MethodID, o.MethodID);
  }

}
