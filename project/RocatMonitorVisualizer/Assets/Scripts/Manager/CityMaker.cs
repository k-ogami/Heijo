using UnityEngine;
using System.Collections.Generic;
using System;

public class CityMaker : MonoBehaviour
{

  public string DefaultPackageName = null;
  public GameObject HeigherObject = null;

  [Header("Placement")]
  public float MethodWidth = 0;
  public float EdgeWidth = 0;
  public float Threashold = 0;

  [Header("Texture")]
  public float ReduceColorRate = 0;
  public Color PackageColor = Color.black;
  public Color ClassColor = Color.black;
  public Color MethodColor = Color.black;

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
      CityObject.ID_iterator = 0;
      Manager.CityObjectDB.DefaultPackage = PackageObject.Create(DefaultPackageName);
      Manager.CityObjectDB.DefaultPackage.transform.parent = CitySpace.transform;
      Manager.CityObjectDB.DefaultPackage.Visible = true;
      Manager.CityObjectDB.DefaultPackage.GetComponent<Renderer>().material.color = PackageColor;
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
    ResetPos();
    // x,z座標とサイズの設定
    Place();
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

  private void ResetPos()
  {
    foreach (CityObject obj in Manager.CityObjectDB.ObjectDict.Values) {
      obj.transform.position = Vector3.zero;
    }
  }

  private void Place()
  {
    new BinPackingMaker().Make();
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
    }
    else if (obj.IsClass) {
      color = ClassColor;
    }
    else if (obj.IsMethod) {
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
    }
    else if (obj.IsClass) {
      foreach (CityObject child in obj.ClassChildren.Values) {
        RecSetColor(child, depth + 1);
      }
      foreach (CityObject child in obj.MethodChildren.Values) {
        RecSetColor(child);
      }
    }
  }

}
