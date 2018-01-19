package jp.naist.rocatmonitor.androcatmonitorxp;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import jp.naist.rocatmonitor.androcatmonitorxp.message.MethodInfo;

public class StructureDB
{

  // メソッドIDとメソッド情報のMap。現状Listでも良いがデバッグの際に便利なので
  public Map<Integer, MethodInfo> IdDataMap = new HashMap<>();

  // メソッドの完全名（パッケージ名+クラス名+メソッド名）とメソッドIDのMap。サンプリングの際、StackTraceElemet->メソッドIDの変換のために用いる
  public Map<String, Integer> NameIdMap = new HashMap<>();

  // クラス名のSet。サンプリングの際、StackTraceElemet.getClassName()がサンプリング対象であるか否かの判別に用いる
  public Set<String> ClassNameSet = new HashSet<>();

  // 監視対象外パッケージ
  public Set<String> IgnorePackageNameSet = new HashSet<>();

  private int methodIdIterator = 0;

  public void registMethod(String className, String methodName)
  {
    // オーバーロードされたメソッドは区別しない（スタックトレースからは引数の型が取得できないので）
    if (NameIdMap.containsKey(className + "." + methodName)) return;

    // メソッド情報を登録
    MethodInfo method = new MethodInfo(methodIdIterator++, className, methodName);
    IdDataMap.put(method.MethodID, method);
    NameIdMap.put(method.toString(), method.MethodID);

    ClassNameSet.add(className);

    // XposedBridge.log("registed { ID:" + method.MethodID + ", Name:" + method.toString() + " }");
  }

  public boolean isIgnorePackage(String packageName)
  {
    if (packageName == null || packageName.length() == 0) {
      return IgnorePackageNameSet.contains(ConstValue.DEFAULT_PACKAGE_NAME);
    }

    String[] tokens = packageName.split("\\.");

    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < tokens.length; i++) {
      builder.append(tokens[i]);
      if (IgnorePackageNameSet.contains(builder.toString() + ".*")) {
        return true;
      }
      if (i == tokens.length - 1 && IgnorePackageNameSet.contains(builder.toString())) {
        return true;
      }
      builder.append(".");
    }
    return false;
  }

}
