package rocatmonitor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import rocatmonitor.json.MethodInfo;

public class CollectClassVisitor extends ClassVisitor
{

  public LinkedList<MethodInfo> MethodInfoList = new LinkedList<MethodInfo>();
  public Map<MethodInfoKey, Long> MethodInfoMap = new HashMap<MethodInfoKey, Long>();

  private String now_classSig = null;
  private long methodID_itrator = 0;

  private Set<String> set = new HashSet<String>();

  public CollectClassVisitor()
  {
    super(Opcodes.ASM5);

  }

  @Override
  public void visit(int version, int access, String name, String signature, String superName, String[] interfaces)
  {
    // 除外リストにあるパッケージのクラスは無視する。 重複するクラスも無視する
    if (Agent.IsIgnoreClass(name) || set.contains(name)) {
      now_classSig = null;
    } else {
      now_classSig = name;
      set.add(name);
    }
  }

  @Override
  public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions)
  {
    if (now_classSig != null) {
      // メソッド情報を生成（IDを付与）して、リストに格納
      MethodInfoList.add(new MethodInfo(methodID_itrator, now_classSig, desc, name));
      MethodInfoMap.put(new MethodInfoKey(now_classSig, desc, name), methodID_itrator);
      methodID_itrator++;
    }

    return null;
  }

}
