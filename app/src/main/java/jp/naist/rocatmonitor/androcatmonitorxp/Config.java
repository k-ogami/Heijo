package jp.naist.rocatmonitor.androcatmonitorxp;

import java.util.HashSet;
import java.util.Set;

public class Config
{

  public String Host;
  public int Port;
  public double SampleInterval;
  public double UpdateInterval;

  public Set<String> IgnorePackages = new HashSet<String>() { { add(ConstValue.THIS_PACKAGE_NAME); } };

}
