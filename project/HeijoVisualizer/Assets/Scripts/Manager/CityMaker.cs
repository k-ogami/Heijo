using UnityEngine;
using System.Collections.Generic;

public class CityMaker : MonoBehaviour
{

  [Header("Clone")]
  public GameObject CityObjectClone = null;
  public GameObject HeigherObjectClone = null;

  [Header("Placement")]
  public float MethodWidth = 0;
  public float EdgeWidth = 0;
  public float Threashold = 0;

  [Header("Texture")]
  public float ReduceColorRate = 0;
  public Color PackageColor = Color.black;
  public Color ClassColor = Color.black;
  public Color MethodColor = Color.black;

  // 配置
  public void Remake()
  {
    // 座標のリセット（transformの親子関係を使用せずに親子の移動をさせたことの弊害）
    ResetPos();
    // x,z座標とサイズの設定
    Place();
    // 敷居（高さ）の設定
    RecSetThreashold(Manager.CityObjectDB.DefaultPackage);
    // 色の設定
    RecSetColor(Manager.CityObjectDB.DefaultPackage);
    // 可視レベルをパッケージに設定
    UI.VisibilityButton.OnPackageButton();
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
    foreach (CityObject child in obj.Children) {
      RecSetThreashold(child, next);
    }
  }

  private void RecSetColor(CityObject obj, int depth = 0)
  {
    // 色を設定
    Color color = Color.black;
    if (obj.Type == CityObject.TypeEnum.Package) {
      color = PackageColor;
    }
    else if (obj.Type == CityObject.TypeEnum.Class) {
      color = ClassColor;
    }
    else if (obj.Type == CityObject.TypeEnum.Method) {
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
    if (obj.Type == CityObject.TypeEnum.Package) {
      foreach (CityObject child in obj.PackageChildren.Values) {
        RecSetColor(child, depth + 1);
      }
      foreach (CityObject child in obj.ClassChildren.Values) {
        RecSetColor(child);
      }
    }
    else if (obj.Type == CityObject.TypeEnum.Class) {
      foreach (CityObject child in obj.ClassChildren.Values) {
        RecSetColor(child, depth + 1);
      }
      foreach (CityObject child in obj.MethodChildren.Values) {
        RecSetColor(child);
      }
    }
  }

}
