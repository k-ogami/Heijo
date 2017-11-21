package jp.naist.rocatmonitor;

import java.util.Properties;

public class Config
{

  public int AgentPort;
  public int VisualizerPort;
  public String JubatusHost;
  public int JubatusPort;
  public double AnomalyThreshold;
  public int AnomalyWindowSize;
  public int AnomalyWindowStep;

  public void load() throws Exception
  {
    Properties properties = new Properties();
    properties.load(this.getClass().getResourceAsStream(ConstValue.CONFIG_FILE_PATH));

    AgentPort = (int)getNumberProperty(properties, "AGENT_PORT", true, ConstValue.DEFAULT_AGENT_PORT);
    VisualizerPort = (int)getNumberProperty(properties, "VISUALIZER_PORT", true, ConstValue.DEFAULT_VISUALIZER_PORT);
    AnomalyThreshold = getNumberProperty(properties, "ANOMALY_THRESHOLD", true, ConstValue.DEFAULT_ANOMALY_THRESHOLD);
    JubatusHost = getStringProperty(properties, "JUBATUS_HOST", true, ConstValue.DEFAULT_JUBATUS_HOST);
    JubatusPort = (int)getNumberProperty(properties, "JUBATUS_PORT", true, ConstValue.DEFAULT_JUBATUS_PORT);
    AnomalyWindowSize = (int)getNumberProperty(properties, "ANOMALY_WINDOW_SIZE", true, ConstValue.DEFAULT_ANOMALY_WINDOW_SIZE);
    AnomalyWindowStep = (int)getNumberProperty(properties, "ANOMALY_WINDOW_STEP", true, ConstValue.DEFAULT_ANOMALY_WINDOW_STEP);
  }

  private double getNumberProperty(Properties properties, String key, boolean isOptional, double defaultValue) throws Exception
  {
    try {
      return Double.valueOf(properties.getProperty(key));
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
      } else {
        throw new Exception();
      }
    } else {
      return value;
    }
  }

  private String[] getStringArrayProperty(Properties properties, String key, boolean isOptional, String[] defaultValue) throws Exception
  {
    String value = properties.getProperty(key);
    if (value == null) {
      if (isOptional) {
        return defaultValue;
      } else {
        throw new Exception();
      }
    } else {
      return value.replace(" ", "").replace("\t", "").split(",");
    }
  }

}
