package rocatmonitor;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.instrument.Instrumentation;
import java.nio.file.Files;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.objectweb.asm.ClassReader;

import rocatmonitor.json.RootJSON;

public class Agent
{

  public static final boolean DEBUG_NO_CONNECT = false;

  public static ConfigReader ConfigReader = null;
  public static Monitor Monitor = null; // これいる？
  public static Connector Connector = null;
  public static IntervalThread IntervalThread = null;

  public static void premain(String args, Instrumentation inst) throws IOException
  {
    // オプションを読み込む
    Agent.ConfigReader = new ConfigReader(args);

    // ビジュアライザと接続する
    Agent.Connector = new Connector();
    if (!DEBUG_NO_CONNECT) {
      try {
        Agent.Connector.Connect(Agent.ConfigReader.Host, Agent.ConfigReader.Port);
      } catch (IOException e) {
        System.err.println("RocatMonitorAgent:接続に失敗しました。監視を中断します。");
        return;
      }
    }

    // クラスパス以下にあるクラスを全走査して、パッケージ・クラス・メソッドの情報を得る
    CollectClassVisitor collector = CollectClassFile();
    // OneLoadモード時、パッケージ・クラス・メソッドの情報をビジュアライザに送信する
    if (ConfigReader.OneLoad) {
      RootJSON json = new RootJSON();
      json.MethodInfos = collector.MethodInfoList;
      Agent.Connector.Send(json);
    }
    // バイトコード書き換えのTransformerを追加
    Agent.Monitor = new Monitor();
    ByteTransformer transformer = new ByteTransformer(collector.MethodInfoMap);
    inst.addTransformer(transformer);

    // 送信用スレッドを開始する
    IntervalThread = new IntervalThread(Agent.ConfigReader.Interval);
    IntervalThread.start();
  }

  public static boolean IsIgnoreClass(String classSig)
  {
    // スラッシュで区切る。最後のスラッシュ以降は型名なので要らない
    List<String> tokens = new LinkedList<String>();
    int s = 0;
    for (int t = 0; t < classSig.length(); t++) {
      if (classSig.charAt(t) == '/') {
        tokens.add(classSig.substring(s, t));
        s = t + 1;
      }
    }

    // デフォルトパッケージ
    if (tokens.size() == 0) {
      tokens.add("<default-package>");
    }

    // 対象外リストに含まれているパッケージ名と一致するか調べる
    // (例) java/lang/String なら java あるいは java.lang が含まれているかを調べる
    StringBuilder pack = new StringBuilder();
    for (String token : tokens) {
      pack.append(token);
      for (String ignore : Agent.ConfigReader.IgnorePackageList) {
        if (pack.toString().equals(ignore)) {
          return true;
        }
      }
      pack.append(".");
    }
    return false;
  }

  private static CollectClassVisitor CollectClassFile() throws IOException
  {
    // すべてのクラスパスを取得
    String class_path = System.getProperty("java.class.path");
    String[] paths = class_path.split(System.getProperty("path.separator"));

    // classファイル、jarファイルを再帰的に走査
    List<File> classFiles = new LinkedList<File>();
    List<JarFile> jarFiles = new LinkedList<JarFile>();
    for (String path : paths) {
      ReadFiles(new File(path), classFiles, jarFiles);
    }

    CollectClassVisitor visitor = new CollectClassVisitor();

    // classファイルのバイトコードを調べる
    for (File classFile : classFiles) {
      byte[] bytes = Files.readAllBytes(classFile.toPath());
      ClassReader reader = new ClassReader(bytes);
      reader.accept(visitor, 0);
    }

    // jarファイル内にあるclassファイルのバイトコードを調べる
    for (JarFile jarFile : jarFiles) {
      for (JarEntry entry : Collections.list(jarFile.entries())) {
        if (entry.getName().endsWith(".class")) {
          InputStream stream = jarFile.getInputStream(entry);
          byte[] bytes = GetBytes(stream);
          ClassReader reader = new ClassReader(bytes);
          reader.accept(visitor, 0);
        }
      }
    }

    return visitor;
  }

  private static void ReadFiles(File path, List<File> classFileList, List<JarFile> jarFileList) throws IOException
  {
    if (path.isDirectory()) {
      for (File p : path.listFiles()) {
        ReadFiles(p, classFileList, jarFileList);
      }
    } else {
      if (path.getName().endsWith(".class")) {
        classFileList.add(path);
      } else if (path.getName().endsWith(".jar")) {
        jarFileList.add(new JarFile(path));
      }
    }
  }

  private static byte[] GetBytes(InputStream input) throws IOException
  {
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    OutputStream output = new BufferedOutputStream(bytes);
    int c;
    while ((c = input.read()) != -1) {
      output.write(c);
    }
    if (output != null) {
      output.flush();
      output.close();
    }
    return bytes.toByteArray();
  }

}
