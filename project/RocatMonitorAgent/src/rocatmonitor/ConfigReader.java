package rocatmonitor;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConfigReader
{
  // 値は適当
  // default.confの読み込みに失敗し、オプションで設定されなかった場合にはこの初期値が適用される
  public String Host = "localhost";
  public int Port = 8000;
  public int Interval = 100;
  public boolean OneLoad = true;

  // 除外パッケージのリスト
  public List<String> IgnorePackageList = new LinkedList<String>();

  // 設定ファイルパス（jar内に存在）
  private static final String default_file_path = "/default.conf";
  private static final String ignore_file_path = "/ignore.conf";

  public ConfigReader(String options)
  {
    // 設定ファイルから値を読み込む
    ReadDefaultConf();
    // オプションから値を読み込む
    SetOptions(options);
    // ignore.confから除外パッケージのリストを読み込む
    SetIgnore();
  }

  private void ReadDefaultConf()
  {
    try {
      InputStream stream = Agent.class.getResourceAsStream(default_file_path);
      if (stream != null) {
        Pattern pattern_host = Pattern.compile("^host:(.*)$");
        Pattern pattern_port = Pattern.compile("^port:([0-9]*)$");
        Pattern pattern_interval = Pattern.compile("^interval:([0-9]*)$");
        Pattern pattern_oneload = Pattern.compile("^oneload:(.*)$");
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        String line;
        while ((line = reader.readLine()) != null) {
          if (line.length() != 0) {
            Matcher matcher;
            matcher = pattern_host.matcher(line);
            if (matcher.find()) {
              Host = matcher.group(1);
              continue;
            }
            matcher = pattern_port.matcher(line);
            if (matcher.find()) {
              Port = Integer.valueOf(matcher.group(1));
              continue;
            }
            matcher = pattern_interval.matcher(line);
            if (matcher.find()) {
              Interval = Integer.valueOf(matcher.group(1));
              continue;
            }
            matcher = pattern_oneload.matcher(line);
            if (matcher.find()) {
              OneLoad = Boolean.valueOf(matcher.group(1));
              continue;
            }
          }
        }
        reader.close();
        stream.close();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void SetOptions(String options)
  {
    // オプションの形式（順不同でカンマで区切る）
    // host:****,port:****,interval:****,oneload:****
    // host: 送信先のホスト名あるいはIPアドレス
    // port: 送信先のポート番号
    // interval: 送信の間隔 [ミリ秒]
    // oneload:"true" or other

    // オプション読み込み
    if (options != null) {
      Pattern pattern_host = Pattern.compile("^host:(.*)$");
      Pattern pattern_port = Pattern.compile("^port:([0-9]*)$");
      Pattern pattern_interval = Pattern.compile("^interval:([0-9]*)$");
      Pattern pattern_oneload = Pattern.compile("^oneload:(.*)$");
      String[] tokens = options.split(",");
      for (String token : tokens) {
        Matcher matcher;
        matcher = pattern_host.matcher(token);
        if (matcher.find()) {
          Host = matcher.group(1);
          continue;
        }
        matcher = pattern_port.matcher(token);
        if (matcher.find()) {
          Port = Integer.valueOf(matcher.group(1));
          continue;
        }
        matcher = pattern_interval.matcher(token);
        if (matcher.find()) {
          Interval = Integer.valueOf(matcher.group(1));
          continue;
        }
        matcher = pattern_oneload.matcher(token);
        if (matcher.find()) {
          OneLoad = Boolean.valueOf(matcher.group(1));
          continue;
        }
      }
    }
  }

  private void SetIgnore()
  {
    // 重要。監視クラス自身を監視対象から除外しないと、監視が無限ループしてスタックオーバーフローして死ぬ
    IgnorePackageList.add("rocatmonitor");

    try {
      // ファイルから各行を読み込む
      InputStream stream = Agent.class.getResourceAsStream(ignore_file_path);
      if (stream != null) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
        String line;
        while ((line = reader.readLine()) != null) {
          if (line.length() != 0) {
            IgnorePackageList.add(line);
          }
        }
        reader.close();
        stream.close();
      }
    } catch (Exception e) {
    }
  }

}
