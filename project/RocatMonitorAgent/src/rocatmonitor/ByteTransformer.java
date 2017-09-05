package rocatmonitor;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.Map;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import rocatmonitor.json.MethodInfo;

public class ByteTransformer implements ClassFileTransformer
{

  private Map<MethodInfoKey, Long> methodInfoMap = null;

  public ByteTransformer(Map<MethodInfoKey, Long> methodInfoMap)
  {
    this.methodInfoMap = methodInfoMap;
  }

  @Override
  public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException
  {
    // 指定のパッケージのクラスは無視
    if (Agent.IsIgnoreClass(className)) {
      return null;
    }

    // クラス内のメソッドを走査し、開始と終了のバイトコードに監視用のメソッドを挿入
    ClassReader reader = new ClassReader(classfileBuffer);
    ClassWriter writer = new ClassWriter(reader, 0);
    AddMonitoringClassVisitor visitor = new AddMonitoringClassVisitor(writer, className, methodInfoMap);
    reader.accept(visitor, 0);

    return writer.toByteArray();
  }

  private class AddMonitoringClassVisitor extends ClassVisitor
  {
    private String classSig = null;
    private Map<MethodInfoKey, Long> methodInfoMap = null;

    private AddMonitoringClassVisitor(ClassWriter writer, String classSig, Map<MethodInfoKey, Long> methodInfoMap)
    {
      super(Opcodes.ASM5, writer);
      this.classSig = classSig;
      this.methodInfoMap = methodInfoMap;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions)
    {
      MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions);
      return mv == null ? null : new AddMonitoringMethodVisitor(mv, classSig, desc, name, methodInfoMap);
    }

  }

  private static class AddMonitoringMethodVisitor extends MethodVisitor
  {
    private long methodID = -1;

    private AddMonitoringMethodVisitor(MethodVisitor mv, String classSig, String methodSig, String methodName, Map<MethodInfoKey, Long> methodInfoMap)
    {
      super(Opcodes.ASM5, mv);
      MethodInfoKey key = new MethodInfoKey(classSig, methodSig, methodName);
      if (methodInfoMap.containsKey(key)) {
        methodID = methodInfoMap.get(key);
        if (!Agent.ConfigReader.OneLoad) {
          Monitor.RegistMethod(new MethodInfo(methodID, classSig, methodSig, methodName));
        }
      }
    }

    @Override
    public void visitCode()
    {
      super.visitCode();
      if (0 <= methodID) {
        mv.visitLdcInsn(methodID);
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, "rocatmonitor/Monitor", "MethodEnter", "(J)V", false);
      }
    }

    @Override
    public void visitInsn(int opcode)
    {
      if (0 <= methodID) {
        if ((Opcodes.IRETURN <= opcode && opcode <= Opcodes.RETURN) || opcode == Opcodes.ATHROW) {
          mv.visitLdcInsn(methodID);
          mv.visitMethodInsn(Opcodes.INVOKESTATIC, "rocatmonitor/Monitor", "MethodExit", "(J)V", false);
        }
      }
      super.visitInsn(opcode);
    }

    @Override
    public void visitMaxs(int maxStack, int maxLocals)
    {
      super.visitMaxs(maxStack + 8, maxLocals);
    }

  }

}
