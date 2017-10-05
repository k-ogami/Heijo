package jp.naist.rocatmonitor.json;

public class RootJSON
{

  public long Time = 0;

  public int Sample = 0;

  public CallCountInfo[] CallCountInfo = new CallCountInfo[] {};

  public SampleCountInfo[] SampleCountInfo = new SampleCountInfo[] {};

  public MethodInfo[] MethodInfo = new MethodInfo[] {};

}
