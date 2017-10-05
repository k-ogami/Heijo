package jp.naist.rocatmonitor;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

public class Config
{

  public int Port = ConstValue.DEFAULT_PORT;
  public int SampleInterval = ConstValue.DEFAULT_SAMPLE_INTERVAL;
  public int UpdateInterval = ConstValue.DEFAULT_UPDATE_INTERVAL;
  public Set<String> IgnorePackages = new HashSet<String>() { { add(ConstValue.THIS_PACKAGE_NAME); } };

  public void Load()
  {
    Properties conf = new Properties();
    try {
      conf.load(this.getClass().getResourceAsStream(ConstValue.CONFIG_FILE_PATH));
      int port = Integer.valueOf(conf.getProperty("PORT"));
      int sample = Integer.valueOf(conf.getProperty("SAMPLE_INTERVAL"));
      int update = Integer.valueOf(conf.getProperty("UPDATE_INTERVAL"));
      String[] ignore = conf.getProperty("IGNORE_PACKAGE").replaceAll("[\\s\t]", "").split(",");
      Port = port;
      SampleInterval = sample;
      UpdateInterval = update;
      IgnorePackages.addAll(Arrays.asList(ignore));
    } catch (IOException e) {
      System.err.println("Failed to load " + ConstValue.CONFIG_FILE_PATH);
    }
  }

}
