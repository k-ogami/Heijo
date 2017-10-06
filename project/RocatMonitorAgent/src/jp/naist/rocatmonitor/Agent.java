package jp.naist.rocatmonitor;

import java.lang.instrument.Instrumentation;
import java.util.HashSet;
import java.util.Set;

import jp.naist.rocatmonitor.Exceptions.ClassFileAccessException;
import jp.naist.rocatmonitor.Exceptions.ServerOpenException;
import jp.naist.rocatmonitor.collect.StructureDB;
import jp.naist.rocatmonitor.connect.Connector;
import jp.naist.rocatmonitor.sampler.Sampler;
import jp.naist.rocatmonitor.transform.Transformer;

public class Agent
{

  public static Config Config = new Config();

  public static Connector Connector = new Connector();

  public static StructureDB StructureDB = new StructureDB();

  public static Sampler Sampler = new Sampler();

  // 接続数が0のときfalseになり、処理を省略する
  public static boolean IsAlive = false;

  // 通信やサンプラで使用するスレッドのIDのセット
  public static Set<Long> AgentThreadIdSet = new HashSet<Long>();

  public static void premain(String args, Instrumentation inst)
  {
    System.out.println("Setting agent...");

    try {

      // 設定ファイル読み込み
      Config.Load();

      // 通信用スレッド開始
      Connector.Start();

      // クラスパス以下のクラスファイルを走査して、パッケージ・クラス・メソッド名を収集
      StructureDB.Collect();

      // クラスロード時にバイトコード書き換えを行うTransformerを追加
      inst.addTransformer(new Transformer());

      // サンプリング＆送信用の スレッドを開始
      Sampler.Start();

      System.out.println("Succeeded to set agent. (Port:" + Agent.Config.Port + ")");

    } catch (ServerOpenException e) {
      System.out.println("Failed to open server. (Port:" + Agent.Config.Port + ")");
    } catch (ClassFileAccessException e) {
      System.out.println("Failed to access to class files");
      Connector.Close();
    }
  }

  public static boolean IsIgnoreClass(String classSig)
  {
    String[] tokens = classSig.split("/");

    if (tokens.length == 1) {
      return Agent.Config.IgnorePackages.contains(ConstValue.DEFAULT_PACKAGE_NAME);
    }

    // パッケージが除外対象であるか調べる
    // たとえば、classSigがjava/lang/Stringの場合、java.*, java.lang.*,
    // java.langのいずれかが除外対象に含まれていれば除外
    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < tokens.length - 1; i++) {
      builder.append(tokens[i]);
      if (Agent.Config.IgnorePackages.contains(builder.toString() + ".*")) {
        return true;
      }
      if (i == tokens.length - 2 && Agent.Config.IgnorePackages.contains(builder.toString())) {
        return true;
      }
      builder.append(".");
    }
    return false;
  }

}
