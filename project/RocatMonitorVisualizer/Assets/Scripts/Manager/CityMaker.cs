using UnityEngine;
using System.Collections.Generic;
using System;

public class CityMaker : MonoBehaviour
{

  public string DefaultPackageName = null;

  [Header("Placement")]
  public float MethodWidth = 0;
  public float EdgeWidth = 0;
  public float Interval = 0;
  public float Threashold = 0;

  [Header("Texture")]
  public float ReduceColorRate = 0;
  public Color PackageColor = Color.black;
  public Color ClassColor = Color.black;
  public Color MethodColor = Color.black;
  public Material HeighterMaterial = null;

  // CityObject置き場
  [System.NonSerialized]
  public GameObject CitySpace = null;

  public void Initialize()
  {
    CitySpace = new GameObject("CitySpace");
  }

  public void Work(List<RootJSON> jsons)
  {
    // 初生成のとき
    if (Manager.Connector.GetConnectFlag()) {
      // 全消去
      Manager.CityObjectDB.Clear();
      Manager.ExeTimeDB.Clear();

      // 初期化
      Manager.CityObjectDB.DefaultPackage = PackageObject.Create(DefaultPackageName);
      Manager.CityObjectDB.DefaultPackage.transform.parent = CitySpace.transform;
      Manager.CityObjectDB.DefaultPackage.Visible = true;
      Manager.CityObjectDB.DefaultPackage.GetComponent<Renderer>().material.color = PackageColor;
      CityObject.ID_iterator = 0;
    }

    bool remake = false;

    // 未知のメソッドが知らされたとき、DBに登録して、街を再構築
    foreach (RootJSON json in jsons)
      if (json.MethodInfos.Length != 0) {
        foreach (MethodInfo methodInfo in json.MethodInfos) {
          Manager.CityObjectDB.RegistUnknownMethod(methodInfo);
          remake = true;
        }
      }
    if (remake) {
      Remake();
    }

    // メソッドの実行時間の情報を処理
    foreach (RootJSON json in jsons) {
      if (json != null && json.ExeTimeInfos != null) {
        Manager.ExeTimeDB.RegistInfo(json.ExeTimeInfos, json.Time);
      }
    }
    Manager.ExeTimeDB.SetHeight();
  }

  // 配置
  private void Remake()
  {
    // Visibleの設定
    RecSetVisible(Manager.CityObjectDB.DefaultPackage);
    // 座標のリセット（transformの親子関係を使用せずに親子の移動をさせたことの弊害）
    ResetPos(Manager.CityObjectDB.DefaultPackage);
    // x,z座標の設定
    RecPlace(Manager.CityObjectDB.DefaultPackage);
    // サイズの設定
    RecSetSize(Manager.CityObjectDB.DefaultPackage);
    // 敷居（高さ）の設定
    RecSetThreashold(Manager.CityObjectDB.DefaultPackage);
    // 色の設定
    RecSetColor(Manager.CityObjectDB.DefaultPackage);
  }

  private void RecSetVisible(CityObject obj, bool visible = true)
  {
    if (obj.ChildrenVisivle == false) {
      visible = false;
    }
    foreach (CityObject child in obj.GetChildren()) {
      child.Visible = visible;
      RecSetVisible(child, visible);
    }
  }

  private void ResetPos(CityObject obj)
  {
    obj.transform.position = Vector3.zero;
    foreach (CityObject child in obj.GetChildren()) {
      ResetPos(child);
    }
  }

  private void RecPlace(CityObject obj)
  {
    if (obj.IsMethod) {
      obj.Width = MethodWidth;
    } else {
      // すべての子に再帰
      foreach (CityObject child in obj.GetChildren()) {
        RecPlace(child);
      }
      // 子を配置していく
      Place(obj);
    }
  }

  private void RecSetSize(CityObject obj)
  {
    obj.transform.localScale = new Vector3(obj.Width, 1, obj.Width);
    foreach (CityObject child in obj.GetChildren()) {
      RecSetSize(child);
    }
  }

  private void RecSetThreashold(CityObject obj, float height = 0)
  {
    Vector3 pos = obj.transform.position;
    Vector3 scale = obj.transform.localScale;
    obj.transform.position = new Vector3(pos.x, height + Threashold / 2, pos.z);
    obj.transform.localScale = new Vector3(scale.x, Threashold, scale.z);
    float next = height + Threashold;
    foreach (CityObject child in obj.GetChildren()) {
      RecSetThreashold(child, next);
    }
  }

  private void RecSetColor(CityObject obj, int depth = 0)
  {
    // 色を設定
    Color color = Color.black;
    if (obj.IsPackage) {
      color = PackageColor;
    } else if (obj.IsClass) {
      color = ClassColor;
    } else if (obj.IsMethod) {
      color = MethodColor;
    }
    for (int i = 0; i < depth; i++) {
      color.r += (1f - color.r) * (1f - ReduceColorRate);
      color.g += (1f - color.g) * (1f - ReduceColorRate);
      color.b += (1f - color.b) * (1f - ReduceColorRate);
      if (1 < color.r) {
        color.r = 1;
      }
      if (1 < color.g) {
        color.g = 1;
      }
      if (1 < color.b) {
        color.b = 1;
      }
    }
    obj.GetComponent<Renderer>().material.color = color;
    // 子へ再帰
    if (obj.IsPackage) {
      foreach (CityObject child in obj.PackageChildren.Values) {
        RecSetColor(child, depth + 1);
      }
      foreach (CityObject child in obj.ClassChildren.Values) {
        RecSetColor(child);
      }
    } else if (obj.IsClass) {
      foreach (CityObject child in obj.ClassChildren.Values) {
        RecSetColor(child, depth + 1);
      }
      foreach (CityObject child in obj.MethodChildren.Values) {
        RecSetColor(child);
      }
    }
  }

  private void Place(CityObject obj)
  {
    // 子をサイズの大きさ順でソートする
    List<CityObject> sizeSortedObjects = new List<CityObject>();
    foreach (CityObject child in obj.GetChildren()) {
      sizeSortedObjects.Add(child);
    }
    sizeSortedObjects.Sort(CompareByObjSize);
    // 最も正方形に近い配置が可能なサイズを探索する
    List<List<CityObject>> result_lines = null;
    float max_size = 0;
    int count;
    for (count = 0; count < sizeSortedObjects.Count; count++) {
      List<List<CityObject>> lines = new List<List<CityObject>>();
      lines.Add(new List<CityObject>());
      // 最大サイズを求める
      max_size = 0;
      for (int i = 0; i <= count; i++) {
        lines[0].Add(sizeSortedObjects[i]);
        if (i != 0) {
          max_size += Interval;
        }
        max_size += sizeSortedObjects[i].Width;
      }
      // z方向のサイズを求める。最大サイズを超えた場合はcontinue
      bool flag = false;
      float sizeX = max_size;
      float sizeZ = lines[0][0].Width;
      for (int i = count + 1; i < sizeSortedObjects.Count; i++) {
        sizeX += sizeSortedObjects[i].Width + Interval;
        if (max_size < sizeX) {
          lines.Add(new List<CityObject>());
          lines[lines.Count - 1].Add(sizeSortedObjects[i]);
          sizeX = sizeSortedObjects[i].Width;
          sizeZ += sizeSortedObjects[i].Width + Interval;
          if (max_size < sizeZ) {
            flag = true;
            break;
          }
        } else {
          lines[lines.Count - 1].Add(sizeSortedObjects[i]);
        }
      }
      if (flag) {
        continue;
      } else {
        result_lines = lines;
        break;
      }
    }
    // サイズを決定
    obj.Width = max_size + EdgeWidth * 2;
    // 配置する
    float x = 0;
    float z = 0;
    foreach (List<CityObject> line in result_lines) {
      float center = line[0].Width / 2;
      foreach (CityObject child in line) {
        child.MoveTo(new Vector3(x + child.Width / 2 - max_size / 2, 0, z + center - max_size / 2));
        x += child.Width + Interval;
      }
      x = 0;
      z += line[0].Width + Interval;
    }
  }

  private int CompareByObjSize(CityObject a, CityObject b)
  {
    if (a.Width == b.Width) {
      return 0;
    } else if (a.Width < b.Width) {
      return 1;
    } else {
      return -1;
    }
  }

}
