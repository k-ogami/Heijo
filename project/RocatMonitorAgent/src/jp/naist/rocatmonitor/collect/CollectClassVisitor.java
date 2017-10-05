package jp.naist.rocatmonitor.collect;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import jp.naist.rocatmonitor.Agent;
import jp.naist.rocatmonitor.json.MethodInfo;

public class CollectClassVisitor extends ClassVisitor
{

  public List<MethodInfo> MethodInfoList = new LinkedList<MethodInfo>();
  public Map<String, Long> MethodIdMap = new HashMap<String, Long>();
  public Set<String> ClassSet = new HashSet<String>();

  private String now_classSig = null;
  private long methodID_itrator = 0;

  public CollectClassVisitor()
  {
    super(Opcodes.ASM5);
  }

  @Override
  public void visit(int version, int access, String name, String signature, String superName, String[] interfaces)
  {
    // 除外リストにあるパッケージのクラスは無視する。 重複するクラスも無視する
    if (Agent.IsIgnoreClass(name) || ClassSet.contains(name)) {
      now_classSig = null;
    } else {
      now_classSig = name;
      ClassSet.add(name);
    }
  }

  @Override
  public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions)
  {
    String key = now_classSig + "/" + name;
    if (now_classSig != null && !MethodIdMap.containsKey(key)) {
      MethodInfoList.add(new MethodInfo(methodID_itrator, now_classSig, name));
      MethodIdMap.put(key, methodID_itrator);
      methodID_itrator++;
    }

    return null;
  }

}
