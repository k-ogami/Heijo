using UnityEngine;
using System.Text;
using System.Text.RegularExpressions;
using System.Collections.Generic;

public class CityObjectDB : MonoBehaviour
{

  public string DefaultPackageName = null;

  [System.NonSerialized]
  public GameObject CitySpace = null;

  // デフォルトパッケージ(オブジェクト木構造の頂点)
  [System.NonSerialized]
  public CityObject DefaultPackage = null;

  // オブジェクトとオブジェクトIDの紐づけ辞書
  public Dictionary<long, CityObject> ObjectDict = new Dictionary<long, CityObject>();

  // メソッドオブジェクトとメソッドID（オブジェクトIDとは別）の紐づけ辞書
  public Dictionary<long, CityObject> MethodDict = new Dictionary<long, CityObject>();

  private int objectIdIterator = 0;

  private void Awake()
  {
    CitySpace = new GameObject("CitySpace");
  }

  public void Init()
  {
    foreach (Transform child in CitySpace.transform) {
      Destroy(child.gameObject);
    }
    MethodDict.Clear();
    ObjectDict.Clear();
    objectIdIterator = 0;
    DefaultPackage = CityObject.Create(CityObject.TypeEnum.Package, objectIdIterator++, DefaultPackageName);
    ObjectDict.Add(DefaultPackage.ID, DefaultPackage);
    DefaultPackage.transform.parent = CitySpace.transform;
  }

  // メソッドをDBに登録
  public void RegistMethod(MethodInfo info)
  {
    // メソッドより上位のオブジェクトらが登録されていなければを登録。メソッドの1つ上のオブジェクト（クラス）を取得
    CityObject klass = RegistObjectsOverMethod(info.ClassName);

    // メソッドオブジェクト生成
    CityObject method = CityObject.Create(CityObject.TypeEnum.Method , objectIdIterator++, info.MethodName);
    ObjectDict.Add(method.ID, method);
    method.transform.parent = CitySpace.transform;

    // 親子関係の紐づけ
    klass.AddChild(method);

    // メソッドオブジェクトとメソッドIDを紐づけ
    MethodDict.Add(info.MethodID, method);
  }

  private CityObject RegistObjectsOverMethod(string classSig)
  {
    // 例："packageA/packageB/MyClass$InnerClass"

    CityObject lastPackage; // 最下位のパッケージ
    CityObject lastClass; // 最下位のクラス

    Regex reg = new Regex("(.*)\\.(.*)");
    string package_text, class_text;
    Match match = reg.Match(classSig);
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
      class_text = classSig;
      lastPackage = DefaultPackage;
    }

    // すべてのクラス名を取得。$で区切るだけ
    string[] class_names = class_text.Split('$');
    // 未知であるクラスを登録。最下位のクラスを取得
    lastClass = CheckAndRegistUnknownClasses(class_names, lastPackage);

    return lastClass;
  }

  private CityObject CheckAndRegistUnknownPackages(string[] names)
  {
    // 最下位のパッケージオブジェクト
    CityObject lastPackage = DefaultPackage;

    foreach (string name in names) {
      // 未知のパッケージの場合、生成して登録
      if (!lastPackage.PackageChildren.ContainsKey(name)) {
        // 生成
        CityObject newPackage = CityObject.Create(CityObject.TypeEnum.Package, objectIdIterator++, name);
        ObjectDict.Add(newPackage.ID, newPackage);
        newPackage.transform.parent = CitySpace.transform;
        // 親子関係の紐づけ
        lastPackage.AddChild(newPackage);
      }
      // 最下位の更新
      lastPackage = lastPackage.PackageChildren[name];
    }

    return lastPackage;
  }

  private CityObject CheckAndRegistUnknownClasses(string[] names, CityObject lastPackage)
  {
    // 最下位のオブジェクト
    CityObject lastObject = lastPackage;

    foreach (string name in names) {
      // 未知のクラスの場合、生成して登録
      if (!lastObject.ClassChildren.ContainsKey(name)) {
        // 生成
        CityObject newClass = CityObject.Create(CityObject.TypeEnum.Class, objectIdIterator++, name);
        ObjectDict.Add(newClass.ID, newClass);
        newClass.transform.parent = CitySpace.transform;
        // 親子関係の紐づけ
        lastObject.AddChild(newClass);
      }
      // 最下位の更新
      lastObject = lastObject.ClassChildren[name];
    }

    return lastObject;
  }

  private string[] PackageText2PackageNames(string text)
  {
    string[] tokens = text.Split('.');
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

}

