package jp.naist.rocatmonitor.transform;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class TransformClassVisitor extends ClassVisitor
{

  private String classSig = null;

  public TransformClassVisitor(ClassWriter writer, String classSig)
  {
    super(Opcodes.ASM5, writer);
    this.classSig = classSig;
  }

  @Override
  public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions)
  {
    MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions);
    return mv == null ? null : new TransformMethodVisitor(mv, classSig, desc, name);
  }

}
