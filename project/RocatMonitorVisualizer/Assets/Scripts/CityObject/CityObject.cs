using UnityEngine;
using UnityEngine.Rendering;
using System.Collections.Generic;

public class CityObject : MonoBehaviour
{

  public float Width = 0;
  public float Height = 0;
  public long Time = 0;
  public bool Visible = true;
  public bool ChildrenVisivle = true;
  public int ThreadNum = 0;

  public CityObject Parent = null;

  // キーはパッケージ名
  public Dictionary<string, PackageObject> PackageChildren = new Dictionary<string, PackageObject>();
  // キーはクラス名
  public Dictionary<string, ClassObject> ClassChildren = new Dictionary<string, ClassObject>();
  // キーはID
  public Dictionary<long, MethodObject> MethodChildren = new Dictionary<long, MethodObject>();

  private GameObject heighter = null;

  /*
  protected void Update()
  {
    GetComponent<Renderer>().enabled = Visible;
    heighter.GetComponent<Renderer>().enabled = Visible && !ChildrenVisivle;
    // レイヤーを変更
    if (Visible) {
      gameObject.layer = LayerMask.NameToLayer("VisibleCityObject");
    }
    else {
      gameObject.layer = LayerMask.NameToLayer("NonVisibleCityObject");
    }
    // 高さの変更
    if (Visible) {
      if (Height <= 0) {
        heighter.GetComponent<Renderer>().enabled = false;
      }
      else {
        // サイズの更新
        Vector3 scale = heighter.transform.localScale;
        if (transform.localScale.y != 0) {
          scale.y = Height / transform.localScale.y;
        }
        heighter.transform.localScale = scale;
        // 座標の更新
        Vector3 pos = transform.position;
        pos.y += (transform.lossyScale.y + heighter.transform.lossyScale.y) / 2;
        heighter.transform.position = pos;
      }
    }
  }
  */

  public void SetHeight(float height)
  {
    Height = height;

    if (Visible) {
      if (Height <= 0) {
        heighter.GetComponent<Renderer>().enabled = false;
      } else {
        // サイズの更新
        Vector3 scale = heighter.transform.localScale;
        if (transform.localScale.y != 0) {
          scale.y = Height / transform.localScale.y;
        }
        heighter.transform.localScale = scale;
        // 座標の更新
        Vector3 pos = transform.position;
        pos.y += (transform.lossyScale.y + heighter.transform.lossyScale.y) / 2;
        heighter.transform.position = pos;
      }
    }
  }

  public void SetVisible(bool visible, bool childrenVisible)
  {
    Visible = visible;
    ChildrenVisivle = childrenVisible;

    GetComponent<Renderer>().enabled = Visible;
    heighter.GetComponent<Renderer>().enabled = Visible && !ChildrenVisivle;

    // レイヤーを変更
    if (Visible) {
      gameObject.layer = LayerMask.NameToLayer("VisibleCityObject");
    } else {
      gameObject.layer = LayerMask.NameToLayer("NonVisibleCityObject");
    }
  }

  // 子を追加して紐付けする
  public void AddChild(CityObject child)
  {
    // 子の親を設定
    child.Parent = this;

    // 子を追加
    if (child.IsPackage) {
      PackageChildren.Add(child.name, (PackageObject)child);
    } else if (child.IsClass) {
      ClassChildren.Add(child.name, (ClassObject)child);
    } else if (child.IsMethod) {
      MethodObject method = (MethodObject)child;
      MethodChildren.Add(method.MethodID, method);
    }
  }

  // 指定した位置に子と共に移動。transformの親子関係はスケールなどの関係で面倒くさい……
  public void MoveTo(Vector3 position)
  {
    Vector3 delta = position - transform.position;
    transform.position = position;
    foreach (CityObject child in PackageChildren.Values) {
      child.MoveTo(child.transform.position + delta);
    }
    foreach (CityObject child in ClassChildren.Values) {
      child.MoveTo(child.transform.position + delta);
    }
    foreach (CityObject child in MethodChildren.Values) {
      child.MoveTo(child.transform.position + delta);
    }
  }

  public bool IsPackage { get { return type == Type.Package; } }
  public bool IsClass { get { return type == Type.Class; } }
  public bool IsMethod { get { return type == Type.Method; } }

  protected enum Type { Package, Class, Method, none }
  protected Type type = Type.none;

  protected static T CreatePrefab<T>() where T : CityObject
  {
    T obj = GameObject.CreatePrimitive(PrimitiveType.Cube).AddComponent<T>();
    GameObject heighter = GameObject.CreatePrimitive(PrimitiveType.Cube);
    heighter.transform.parent = obj.transform;
    heighter.GetComponent<Renderer>().material = Manager.CityMaker.HeighterMaterial;
    heighter.GetComponent<Renderer>().shadowCastingMode = ShadowCastingMode.Off;
    obj.heighter = heighter;
    return obj;
  }

}
