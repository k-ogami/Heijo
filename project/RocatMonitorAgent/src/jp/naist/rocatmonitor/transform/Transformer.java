package jp.naist.rocatmonitor.transform;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import jp.naist.rocatmonitor.Agent;

public class Transformer implements ClassFileTransformer
{

  @Override
  public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException
  {
    if (!Agent.StructureDB.ClassSet.contains(className)) {
      return null;
    }

    // クラス内のメソッドを走査し、開始位置に監視用のメソッドを挿入
    ClassReader reader = new ClassReader(classfileBuffer);
    ClassWriter writer = new ClassWriter(reader, 0);
    TransformClassVisitor visitor = new TransformClassVisitor(writer, className);
    reader.accept(visitor, 0);

    return writer.toByteArray();
  }



}
