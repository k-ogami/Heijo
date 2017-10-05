package jp.naist.rocatmonitor.collect;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.objectweb.asm.ClassReader;

import net.arnx.jsonic.JSON;

public class StructureDB
{

  public byte[] MethodInfoData = null;
  public Map<String, Long> MethodIdMap = null;
  public Set<String> ClassSet = null;

  public void Collect() throws IOException
  {
    // すべてのクラスパスを取得
    String[] paths = System.getProperty("java.class.path").split(System.getProperty("path.separator"));

    // classファイル、jarファイルを再帰的に走査
    Set<String> classFiles = new HashSet<String>();
    Set<String> jarFiles = new HashSet<String>();
    for (String path : paths) {
      FindClassAndJar(new File(path), classFiles, jarFiles);
    }

    CollectClassVisitor visitor = new CollectClassVisitor();

    // classファイルのバイトコードを調べる
    for (String classFile : classFiles) {
      File file = new File(classFile);
      byte[] bytes = Files.readAllBytes(file.toPath());
      ClassReader reader = new ClassReader(bytes);
      reader.accept(visitor, 0);
    }

    // jarファイル内にあるclassファイルのバイトコードを調べる
    for (String jarFile : jarFiles) {
      JarFile file = new JarFile(jarFile);
      for (JarEntry entry : Collections.list(file.entries())) {
        if (entry.getName().endsWith(".class")) {
          InputStream stream = file.getInputStream(entry);
          byte[] bytes = GetBytes(stream);
          ClassReader reader = new ClassReader(bytes);
          reader.accept(visitor, 0);
        }
      }
      file.close();
    }

    MethodInfoData = JSON.encode(visitor.MethodInfoList).getBytes();
    MethodIdMap = visitor.MethodIdMap;
    ClassSet = visitor.ClassSet;
  }

  private void FindClassAndJar(File path, Set<String> classFileSet, Set<String> jarFileSet) throws IOException
  {
    if (path.isDirectory()) {
      for (File p : path.listFiles()) {
        FindClassAndJar(p, classFileSet, jarFileSet);
      }
    } else {
      if (path.getName().endsWith(".class")) {
        classFileSet.add(path.getAbsolutePath());
      } else if (path.getName().endsWith(".jar")) {
        jarFileSet.add(path.getAbsolutePath());
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
