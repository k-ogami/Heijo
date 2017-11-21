package jp.naist.rocatmonitor;

public class ConstValue
{

  // CONFIG.propertiesの読み込みエラー時に適用される値
  public static final String DEFAULT_HOST = "localhost";
  public static final int DEFAULT_PORT = 8000;
  public static final int DEFAULT_SAMPLE_INTERVAL = 1;
  public static final int DEFAULT_UPDATE_INTERVAL = 100;

  public static final String THIS_PACKAGE_NAME = "jp.naist.rocatmonitor.*";

  public static final String CONFIG_FILE_PATH = "/CONFIG.properties";

  public static final String DEFAULT_PACKAGE_NAME = "<default-package>";

  public static final int HEADER_SIZE = 4;
}
