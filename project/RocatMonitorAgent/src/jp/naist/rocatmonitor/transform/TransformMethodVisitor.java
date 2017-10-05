package jp.naist.rocatmonitor.transform;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import jp.naist.rocatmonitor.Agent;

public class TransformMethodVisitor extends MethodVisitor
{

  private long methodID = -1;

  public TransformMethodVisitor(MethodVisitor mv, String classSig, String methodSig, String methodName)
  {
    super(Opcodes.ASM5, mv);
    methodID = Agent.StructureDB.MethodIdMap.get(classSig + "/" + methodName);
  }

  @Override
  public void visitCode()
  {
    super.visitCode();
    if (0 <= methodID) {
      mv.visitLdcInsn(methodID);
      mv.visitMethodInsn(Opcodes.INVOKESTATIC, "jp/naist/rocatmonitor/monitor/Monitor", "MethodEnter", "(J)V", false);
    }
  }

  @Override
  public void visitMaxs(int maxStack, int maxLocals)
  {
    super.visitMaxs(maxStack + 8, maxLocals);
  }

}
