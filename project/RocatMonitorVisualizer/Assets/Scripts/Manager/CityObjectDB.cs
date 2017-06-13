using UnityEngine;
using System.Text;
using System.Text.RegularExpressions;
using System.Collections.Generic;

public class CityObjectDB : MonoBehaviour
{

  // デフォルトパッケージ(オブジェクト木構造の頂点)
  [System.NonSerialized]
  public PackageObject DefaultPackage = null;

  // メソッドとIDの対応付け辞書
  public Dictionary<long, MethodObject> MethodDict = new Dictionary<long, MethodObject>();

  // オブジェクトとメソッドとIDの対応付け辞書
  public Dictionary<long, CityObject> ObjectDict = new Dictionary<long, CityObject>();

  public void Clear()
  {
    foreach (Transform child in Manager.CityMaker.CitySpace.transform) {
      Destroy(child.gameObject);
    }
    MethodDict.Clear();
  }

  // メソッドをDBに登録
  public void RegistUnknownMethod(MethodInfo info)
  {
    // 返値
    string ret = MethodSig2RetType(info.MethodSig);
    // 引数
    string[] args = MethodSig2ArgTypes(info.MethodSig);
    // 実装クラス名(パッケージ名含む)
    string declare = ConvertTypeName("L" + info.ClassSig + ";");

    // メソッドより上位のオブジェクトらが登録されていなければを登録。メソッドの1つ上のクラスオブジェクトを取得
    ClassObject klass = RegistObjectsOverMethod(declare);

    // メソッドオブジェクト生成
    MethodObject method = MethodObject.Create(info.MethodName, info.MethodID, ret, args);
    method.transform.parent = Manager.CityMaker.CitySpace.transform;

    // 親子関係の紐づけ
    klass.AddChild(method);

    // IDと紐づけ
    MethodDict.Add(info.MethodID, method);
  }

  private ClassObject RegistObjectsOverMethod(string type)
  {
    // 例："packageA/packageB/MyClass$InnerClass"

    PackageObject lastPackage; // 最下位のパッケージ
    ClassObject lastClass; // 最下位のクラス

    Regex reg = new Regex("(.*)/(.*)");
    string package_text, class_text;
    Match match = reg.Match(type);
    if (match.Success) {
      package_text = match.Groups[1].Value;
      class_text = match.Groups[2].Value;
      // すべてのパッケージ名を取得("packageA", "packageA.packageB", "packageA.packageB.packageC", ...)
      string[] package_names = PackageText2PackageNames(package_text);
      // 未知であるパッケージを登録。最下位のパッケージを取得
      lastPackage = CheckAndRegistUnknownPackages(package_names);
    }
    // スラッシュがない場合、デフォルトパッケージにあるクラス
    else {
      class_text = type;
      lastPackage = DefaultPackage;
    }

    // すべてのクラス名を取得。$で区切るだけ
    string[] class_names = class_text.Split('$');
    // 未知であるクラスを登録。最下位のクラスを取得
    lastClass = CheckAndRegistUnknownClasses(class_names, lastPackage);

    return lastClass;
  }

  private PackageObject CheckAndRegistUnknownPackages(string[] names)
  {
    // 最下位のパッケージオブジェクト
    PackageObject lastPackage = DefaultPackage;

    foreach (string name in names) {
      // 未知のパッケージの場合、生成して登録
      if (!lastPackage.PackageChildren.ContainsKey(name)) {
        // 生成
        PackageObject newPackage = PackageObject.Create(name);
        newPackage.transform.parent = Manager.CityMaker.CitySpace.transform;
        // 親子関係の紐づけ
        lastPackage.AddChild(newPackage);
      }
      // 最下位の更新
      lastPackage = lastPackage.PackageChildren[name];
    }

    return lastPackage;
  }

  private ClassObject CheckAndRegistUnknownClasses(string[] names, PackageObject lastPackage)
  {
    // 最下位のオブジェクト
    CityObject lastObject = lastPackage;

    foreach (string name in names) {
      // 未知のクラスの場合、生成して登録
      if (!lastObject.ClassChildren.ContainsKey(name)) {
        // 生成
        ClassObject newClass = ClassObject.Create(name);
        newClass.transform.parent = Manager.CityMaker.CitySpace.transform;
        // 親子関係の紐づけ
        lastObject.AddChild(newClass);
      }
      // 最下位の更新
      lastObject = lastObject.ClassChildren[name];
    }

    // 正しく処理されていれば必ずキャストできるはず……
    return (ClassObject)lastObject;
  }

  private string[] PackageText2PackageNames(string text)
  {
    string[] tokens = text.Split('/');
    string[] outputs = new string[tokens.Length];

    for (int i = 0; i < tokens.Length; i++) {
      StringBuilder sum = new StringBuilder();
      for (int j = 0; j <= i; j++) {
        if (j != 0) {
          sum.Append('.');
        }
        sum.Append(tokens[j]);
      }
      outputs[i] = sum.ToString();
    }

    return outputs;
  }

  private string MethodSig2RetType(string sig)
  {
    // void main(String[])の場合"([Ljava/lang/String;)V"
    // ↑返値部分"V"を取得
    Regex reg = new Regex("\\(.*\\)(.*)");
    string ret = reg.Match(sig).Groups[1].Value;

    // 型名を変換
    return ConvertTypeName(ret);
  }

  private string[] MethodSig2ArgTypes(string sig)
  {
    // void main(String[])の場合"([Ljava/lang/String;)V"
    // ↑引数部分"[Ljava/lang/String;"を取得(複数)
    Regex reg = new Regex("\\((.*)\\).*");
    string args = reg.Match(sig).Groups[1].Value;
    LinkedList<string> types = new LinkedList<string>();

    bool isArray = false;
    for (int i = 0; i < args.Length; i++) {
      // [から始まる場合、次に現れる型は引数型
      if (args[i] == '[') {
        isArray = true;
      }
      else {
        string type;
        // Lから始まる場合、Lから;までが型名
        if (args[i] == 'L') {
          int start = i++;
          for (; args[i] != ';'; i++) ;
          int end = i;
          type = args.Substring(start, end - start + 1);
        }
        // それ以外の場合、型名は1文字（プリミティブ型）
        else {
          type = args[i].ToString();
        }
        // 配列フラグ処理
        if (isArray) {
          type = "[" + type;
          isArray = false;
        }
        // 型名を整形
        type = ConvertTypeName(type);
        // 追加
        types.AddLast(type);
      }
    }

    // LinkedList -> 配列
    string[] array = new string[types.Count];
    int itrator = 0;
    foreach (string type in types) {
      array[itrator] = type;
      itrator++;
    }

    return array;
  }

  private string ConvertTypeName(string type)
  {
    string output = type;

    // [から始まる場合は配列型。1文字ずらす
    int arrayCount = 0;
    while (output[0] == '[') {
      arrayCount++;
      output = output.Substring(1, output.Length - 1);
    }

    // Lから始まる場合、Lと;の間が型名
    if (output[0] == 'L') {
      output = output.Substring(1, output.Length - 2);
    }
    // それ以外の場合、プリミティブ型
    else {
      switch (output[0]) {
        case 'V':
          output = "void";
          break;
        case 'Z':
          output = "boolean";
          break;
        case 'B':
          output = "byte";
          break;
        case 'C':
          output = "char";
          break;
        case 'S':
          output = "short";
          break;
        case 'I':
          output = "int";
          break;
        case 'J':
          output = "long";
          break;
        case 'F':
          output = "float";
          break;
        case 'D':
          output = "double";
          break;
        default:
          MonoBehaviour.print("エラー：\"" + output + "\"は不明なプリミティブ型名です。");
          break;
      }
    }

    // 配列処理
    for (int i = 0; i < arrayCount; i++) {
      output += "[]";
    }

    return output;
  }

}
