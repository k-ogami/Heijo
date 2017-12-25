package jp.naist.rocatmonitor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtMethod;
import jp.naist.rocatmonitor.message.MethodData;

public class StructureDB
{

  // メソッドIDとメソッド情報のMap。現状Listでも良いがデバッグの際に便利なので
  public Map<Long, MethodData> IdDataMap = new HashMap<>();

  // メソッドの完全名（パッケージ名+クラス名+メソッド名）とメソッドIDのMap。サンプリングの際、StackTraceElemet->メソッドIDの変換のために用いる
  public Map<String, Long> NameIdMap = new HashMap<>();

  // クラス名のSet。サンプリングの際、StackTraceElemet.getClassName()がサンプリング対象であるか否かの判別に用いる
  public Set<String> ClassNameSet = new HashSet<>();

  private final Set<String> ignorePackages;

  private long methodIdIterator = 0;

  // メソッド名の重複を防ぐためのSet。1クラスごとにクリアする
  // StackTraceElementがメソッドシグネチャ（引数の型）を含んでいないため、仕様上、オーバーロードされたメソッドはすべて同一と見なす
  Set<String> methodNameSet = new HashSet<>();

  public StructureDB(Set<String> ignorePackages)
  {
    this.ignorePackages = new HashSet<>(ignorePackages);
  }

  public void collectFromClassPath() throws IOException
  {
    // すべてのクラスパスを取得
    String[] paths = System.getProperty("java.class.path").split(System.getProperty("path.separator"));

    // classファイル、jarファイルを再帰的に走査
    Set<String> classFiles = new HashSet<String>();
    Set<String> jarFiles = new HashSet<String>();
    for (String path : paths) {
      findClassAndJar(new File(path), classFiles, jarFiles);
    }

    // classファイルのバイトコードを調べる
    for (String classFile : classFiles) {
      FileInputStream fis = new FileInputStream(classFile);
      collectClass(fis);
      fis.close();
    }

    // jarファイル内にあるclassファイルのバイトコードを調べる
    for (String jarFile : jarFiles) {
      JarFile file = new JarFile(jarFile);
      for (JarEntry entry : Collections.list(file.entries())) {
        if (entry.getName().endsWith(".class")) {
          collectClass(file.getInputStream(entry));
        }
      }
      file.close();
    }
  }

  private void findClassAndJar(File path, Set<String> classFileSet, Set<String> jarFileSet) throws IOException
  {
    if (path.isDirectory()) {
      for (File p : path.listFiles()) {
        findClassAndJar(p, classFileSet, jarFileSet);
      }
    } else {
      if (path.getName().endsWith(".class")) {
        classFileSet.add(path.getAbsolutePath());
      } else if (path.getName().endsWith(".jar")) {
        jarFileSet.add(path.getAbsolutePath());
      }
    }
  }

  private void collectClass(InputStream stream) throws IOException, RuntimeException
  {
    CtClass klass = ClassPool.getDefault().makeClass(stream);

    if (isIgnorePackage(klass.getPackageName())) {
      return;
    } else {
      ClassNameSet.add(klass.getName());
    }


    // コンストラクタ<init>と<clinit>の有無を確認して、存在する場合は追加
    boolean isDecInit = 0 < klass.getDeclaredConstructors().length;
    boolean isDecClinit = false;
    CtConstructor clinit = klass.getClassInitializer();
    isDecClinit = clinit != null && clinit.getDeclaringClass().getName().equals(klass.getName());
    if (isDecInit) {
      MethodData data = new MethodData(methodIdIterator++, klass.getName(), "<init>");
      IdDataMap.put(data.MethodID, data);
      NameIdMap.put(data.toString(), data.MethodID);
    }
    if (isDecClinit) {
      MethodData data = new MethodData(methodIdIterator++, klass.getName(), "<clinit>");
      IdDataMap.put(data.MethodID, data);
      NameIdMap.put(data.toString(), data.MethodID);
    }

    for (CtMethod method : klass.getDeclaredMethods()) {
      // メソッド名の重複を防ぐ
      if (!methodNameSet.contains(method.getName())) {
        methodNameSet.add(method.getName());
        MethodData data = new MethodData(methodIdIterator++, klass.getName(), method.getName());
        IdDataMap.put(data.MethodID, data);
        NameIdMap.put(data.toString(), data.MethodID);
      }
    }
    methodNameSet.clear();
  }

  private boolean isIgnorePackage(String packageName)
  {
    if (packageName == null) {
      return ignorePackages.contains(ConstValue.DEFAULT_PACKAGE_NAME);
    }

    String[] tokens = packageName.split("\\.");

    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < tokens.length; i++) {
      builder.append(tokens[i]);
      if (ignorePackages.contains(builder.toString() + ".*")) {
        return true;
      }
      if (i == tokens.length - 1 && ignorePackages.contains(builder.toString())) {
        return true;
      }
      builder.append(".");
    }
    return false;
  }

}
