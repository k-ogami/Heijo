package jp.naist.rocatmonitor;

public class ConstValue
{

  public static final int DEFAULT_AGENT_PORT = 8000;
  public static final int DEFAULT_VISUALIZER_PORT = 8001;

  public static final String DEFAULT_JUBATUS_HOST = "localhost";
  public static final int DEFAULT_JUBATUS_PORT = 9199;

  public static final int DEFAULT_ANOMALY_WINDOW_SIZE = 5;
  public static final int DEFAULT_ANOMALY_WINDOW_STEP = 2;
  public static final double DEFAULT_ANOMALY_THRESHOLD = 1.5;

  public static final String CONFIG_FILE_PATH = "/CONFIG.properties";

  public static final int HEADER_SIZE = 4;

  public static final int JUBATUS_TIMEOUT_SEC = 10;

  public static final int DATA_COLLECTION_SIZE = 10000;

  public static final int ANOMALY_TOO_LARGE_VALUE = 100000000;

}
