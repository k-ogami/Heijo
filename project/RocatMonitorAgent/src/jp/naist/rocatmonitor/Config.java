package jp.naist.rocatmonitor;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

public class Config
{

  public String Host;
  public int Port;
  public int SampleInterval;
  public int UpdateInterval;
  public Set<String> IgnorePackages = new HashSet<String>() { { add(ConstValue.THIS_PACKAGE_NAME); } };

  public void load() throws Exception
  {
    Properties properties = new Properties();
    properties.load(this.getClass().getResourceAsStream(ConstValue.CONFIG_FILE_PATH));
    Host = getStringProperty(properties, "HOST", true, ConstValue.DEFAULT_HOST);
    Port = getIntegerProperty(properties, "PORT", true, ConstValue.DEFAULT_PORT);
    SampleInterval = getIntegerProperty(properties, "SAMPLE_INTERVAL", true, ConstValue.DEFAULT_SAMPLE_INTERVAL);
    UpdateInterval = getIntegerProperty(properties, "UPDATE_INTERVAL", true, ConstValue.DEFAULT_UPDATE_INTERVAL);
    String[] ignore = getStringArrayProperty(properties, "IGNORE_PACKAGE", true, new String[] {});
    IgnorePackages.addAll(Arrays.asList(ignore));
  }

  private int getIntegerProperty(Properties properties, String key, boolean isOptional, int defaultValue) throws Exception
  {
    try {
      return Integer.valueOf(properties.getProperty(key));
    } catch (Exception e) {
      if (isOptional) {
        return defaultValue;
      } else {
        throw e;
      }
    }
  }

  private String getStringProperty(Properties properties, String key, boolean isOptional, String defaultValue) throws Exception
  {
    String value = properties.getProperty(key);
    if (value == null) {
      if (isOptional) {
        return defaultValue;
      }
      else {
        throw new Exception();
      }
    }
    else {
      return value;
    }
  }

  private String[] getStringArrayProperty(Properties properties, String key, boolean isOptional, String[] defaultValue) throws Exception
  {
    String value = properties.getProperty(key);
    if (value == null) {
      if (isOptional) {
        return defaultValue;
      }
      else {
        throw new Exception();
      }
    }
    else {
      return value.replace(" ", "").replace("\t", "").split(",");
    }
  }

}
