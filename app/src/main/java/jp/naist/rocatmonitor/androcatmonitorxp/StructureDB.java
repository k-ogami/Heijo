package jp.naist.rocatmonitor.androcatmonitorxp;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import jp.naist.rocatmonitor.androcatmonitorxp.message.MethodData;

public class StructureDB
{

  // メソッドIDとメソッド情報のMap。現状Listでも良いがデバッグの際に便利なので
  public Map<Integer, MethodData> IdDataMap = new HashMap<>();

  // メソッドの完全名（パッケージ名+クラス名+メソッド名）とメソッドIDのMap。サンプリングの際、StackTraceElemet->メソッドIDの変換のために用いる
  public Map<String, Integer> NameIdMap = new HashMap<>();

  // クラス名のSet。サンプリングの際、StackTraceElemet.getClassName()がサンプリング対象であるか否かの判別に用いる
  public Set<String> ClassNameSet = new HashSet<>();

  // 監視対象外パッケージ
  public Set<String> IgnorePackageNameSet = new HashSet<>();

  private int methodIdIterator = 0;

  private String nowClassName = null;

  private Set<String> methodNameSet = new HashSet<>();

  public void registMethod(String className, String methodName)
  {
    if (nowClassName == null || !nowClassName.equals(className)) {
      nowClassName = className;
      methodNameSet.clear();
    }

    // getStackTraceではメソッドの引数の型情報は取れないので、オーバーロードされたメソッドは同一と見なす
    if (methodNameSet.contains(methodName)) return;
    methodNameSet.add(methodName);

    // メソッド情報を登録
    MethodData methodData = new MethodData(methodIdIterator++, className, methodName);
    IdDataMap.put(methodData.MethodID, methodData);
    NameIdMap.put(methodData.toString(), methodData.MethodID);

    // XposedBridge.log("registed { ID:" + methodData.MethodID + ", Name:" + methodData.toString() + " }");
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
