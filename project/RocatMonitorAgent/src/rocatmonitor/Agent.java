package rocatmonitor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class Agent
{

  // オプション（デフォルト値）
  private static String host = "localhost";
  private static int port = 8000;
  private static int interval = 20;
  private static String ignore_file_path = null;

  public static void premain(String args, Instrumentation inst)
  {
    // オプションを読み込む
    SetOptions(args);

    // 接続する
    try {
      Monitor.Connector.Connect(host, port);
    } catch (IOException e) {
      System.err.println("RocatMonitorAgent:接続に失敗しました。監視を中断します。");
      return;
    }

    // 送信用スレッドを開始する
    Monitor.IntervalThread.Interval = interval;
    Monitor.IntervalThread.start();

    // バイトコード書き換えのTransformerを追加
    Transformer transformer = new Transformer();
    try {
      SetIgnore(transformer, ignore_file_path);
    } catch (IOException e) {
      System.err.println("RocatMonitorAgent:ファイル\"" + ignore_file_path + "\"を開けません。デフォルトの設定を適応します。");
    }
    inst.addTransformer(transformer);
  }

  private static void SetOptions(String options)
  {
    // オプションの形式（順不同でカンマで区切る）
    // host:****,port:****,interval:****,ignore_file_path:****
    // host: 送信先のホスト名あるいはIPアドレス デフォルトではlocalhost
    // port: 送信先のポート番号 デフォルトでは8000
    // interval: 送信の間隔 デフォルトでは20[ms]
    // ignore_file_path: 監視対象から除外するパッケージの名前を記述したファイルのパス
    // デフォルトでは標準ライブラリを含むパッケージの名前が設定される

    // オプション読み込み
    if (options != null) {
      Pattern pattern_host = Pattern.compile("^host:(.*)$");
      Pattern pattern_port = Pattern.compile("^port:([0-9]*)$");
      Pattern pattern_interval = Pattern.compile("^interval:([0-9]*)$");
      Pattern pattern_ignore_file_path = Pattern.compile("^ignore_file_path:(.*)$");
      String[] tokens = options.split(",");
      for (String token : tokens) {
        Matcher matcher;
        matcher = pattern_host.matcher(token);
        if (matcher.find()) {
          host = matcher.group(1);
          continue;
        }
        matcher = pattern_port.matcher(token);
        if (matcher.find()) {
          port = Integer.valueOf(matcher.group(1));
          continue;
        }
        matcher = pattern_interval.matcher(token);
        if (matcher.find()) {
          interval = Integer.valueOf(matcher.group(1));
          continue;
        }
        matcher = pattern_ignore_file_path.matcher(token);
        if (matcher.find()) {
          ignore_file_path = matcher.group(1);
          continue;
        }
      }
    }
  }

  private static void SetIgnore(Transformer transformer, String path) throws IOException
  {
    if (path == null) {
      // デフォルト設定で監視対象から除外されるパッケージの名前
      transformer.IgnorePackageNames.add("java");
      transformer.IgnorePackageNames.add("javax");
      transformer.IgnorePackageNames.add("jdk");
      transformer.IgnorePackageNames.add("sun");
      transformer.IgnorePackageNames.add("com/sun");
    } else {
      BufferedReader reader;
      reader = new BufferedReader(new FileReader(new File(path)));
      String line;
      while ((line = reader.readLine()) != null) {
        if (line.length() != 0) {
          transformer.IgnorePackageNames.add(line);
        }
      }
      reader.close();
    }
  }

  private static class Transformer implements ClassFileTransformer
  {

    public LinkedList<String> IgnorePackageNames = new LinkedList<String>();

    public Transformer()
    {
      // 重要。監視クラス自身を監視対象から除外しないと、監視が無限ループしてスタックオーバーフローして死ぬ
      IgnorePackageNames.add("rocatmonitor");
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException
    {
      // 指定のパッケージのクラスは無視
      if (IsIgnoreClass(className)) {
        return null;
      }

      // クラス内のメソッドを走査し、開始と終了のバイトコードに監視用のメソッドを挿入
      ClassReader reader = new ClassReader(classfileBuffer);
      ClassWriter writer = new ClassWriter(reader, 0);
      AddMonitoringClassVisitor visitor = new AddMonitoringClassVisitor(writer, "L" + className + ";");
      reader.accept(visitor, 0);
      return writer.toByteArray();
    }

    private boolean IsIgnoreClass(String classSig)
    {
      // スラッシュで区切る。最後のスラッシュ以降は型名なので要らない
      List<String> tokens = new LinkedList<String>();
      int s = 0;
      for (int t = 0; t < classSig.length(); t++) {
        if (classSig.charAt(t) == '/') {
          tokens.add(classSig.substring(s, t - s));
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
        for (String ignore : IgnorePackageNames) {
          if (pack.toString().equals(ignore)) {
            return true;
          }
        }
        pack.append(".");
      }
      return false;
    }

  }

  private static class AddMonitoringClassVisitor extends ClassVisitor
  {

    private String classSig = null;

    private AddMonitoringClassVisitor(ClassWriter writer, String classSig)
    {
      super(Opcodes.ASM5, writer);
      this.classSig = classSig;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions)
    {
      MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions);
      return mv == null ? null : new AddMonitoringMethodVisitor(mv, classSig, desc, name);
    }

  }

  private static class AddMonitoringMethodVisitor extends MethodVisitor
  {
    private String classSig = null;
    private String methodSig = null;
    private String methodName = null;

    private static long methodID = 0;

    private AddMonitoringMethodVisitor(MethodVisitor mv, String classSig, String methodSig, String methodName)
    {
      super(Opcodes.ASM5, mv);
      this.classSig = classSig;
      this.methodSig = methodSig;
      this.methodName = methodName;
    }

    @Override
    public void visitCode()
    {
      // メソッド情報を登録
      Monitor.RegistMethod(methodID, classSig, methodSig, methodName);

      // バイトコード挿入
      super.visitCode();
      mv.visitLdcInsn(methodID);
      mv.visitMethodInsn(Opcodes.INVOKESTATIC, "rocatmonitor/Monitor", "MethodEnter", "(J)V", false);
    }

    @Override
    public void visitInsn(int opcode)
    {
      if ((Opcodes.IRETURN <= opcode && opcode <= Opcodes.RETURN) || opcode == Opcodes.ATHROW) {
        // バイトコード挿入
        mv.visitLdcInsn(methodID);
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, "rocatmonitor/Monitor", "MethodExit", "(J)V", false);
      }
      super.visitInsn(opcode);
    }

    @Override
    public void visitMaxs(int maxStack, int maxLocals)
    {
      super.visitMaxs(maxStack + 8, maxLocals);
    }

    @Override
    public void visitEnd()
    {
      super.visitEnd();
      methodID++;
    }

  }

}
