using UnityEngine;
using UnityEngine.Rendering;
using System.Collections.Generic;

public class CityObject : MonoBehaviour
{

  // MethodIDとは別のIDを振り分ける
  public long ID = 0;

  public float WidthX = 0;
  public float WidthZ = 0;
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
  // キーはMethodID
  public Dictionary<long, MethodObject> MethodChildren = new Dictionary<long, MethodObject>();

  public float Height_0_1 = 0;

  [System.NonSerialized]
  public static int ID_iterator = 0;

  private GameObject heighter = null;
  private Renderer ownRenderer = null;
  private Renderer heigherRenderer = null;

  public void SetHeight(float height_0_1)
  {
    Height_0_1 = height_0_1;
    Height = height_0_1 * UI.HeightSlider.Slider.value;
    if (0 < Height && Height <= Manager.ExeTimeDB.MinHeight) Height = Manager.ExeTimeDB.MinHeight;

    heigherRenderer.enabled = Visible && !ChildrenVisivle && 0 < Height;

    if (Visible) {
      if (Height <= 0) {
        heigherRenderer.enabled = false;
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

  public void SetVisible(bool visible)
  {
    Visible = visible;

    ownRenderer.enabled = Visible;
    heigherRenderer.enabled = Visible && !ChildrenVisivle && 0 < Height;

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
    foreach (CityObject child in GetChildren()) {
      child.MoveTo(child.transform.position + delta);
    }
  }

  public IEnumerable<CityObject> GetChildren()
  {
    foreach (CityObject child in PackageChildren.Values) {
      yield return child;
    }
    foreach (CityObject child in ClassChildren.Values) {
      yield return child;
    }
    foreach (CityObject child in MethodChildren.Values) {
      yield return child;
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
    obj.gameObject.layer = LayerMask.NameToLayer("VisibleCityObject");
    GameObject heighter = GameObject.Instantiate(Manager.CityMaker.HeigherObject);
    heighter.transform.parent = obj.transform;
    obj.heighter = heighter;

    obj.ownRenderer = obj.GetComponent<Renderer>();
    obj.heigherRenderer = heighter.GetComponent<Renderer>();

    // IDを付与しDBに登録
    obj.ID = ID_iterator++;
    Manager.CityObjectDB.ObjectDict[obj.ID] = obj;

    return obj;
  }

}
