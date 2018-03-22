using UnityEngine;
using System.Collections.Generic;

public class CityObject : MonoBehaviour
{

  public enum TypeEnum
  {
    None,
    Package,
    Class,
    Method
  }

  public int ID = 0;

  public string Name = null;

  public float WidthX = 0;
  public float WidthY = 0;

  public double Time = 0;

  public int ThreadNum = 0;

  public float Height_0_1 = 0;
  public float Height = 0;

  public bool Visible = true;
  public bool ChildrenVisible = true;

  public TypeEnum Type = TypeEnum.None;

  public CityObject Parent = null;

  public Dictionary<string, CityObject> PackageChildren = new Dictionary<string, CityObject>();
  public Dictionary<string, CityObject> ClassChildren = new Dictionary<string, CityObject>();
  public Dictionary<string, CityObject> MethodChildren = new Dictionary<string, CityObject>();

  public IEnumerable<CityObject> Children
  {
    get
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
  }

  private GameObject heighter = null;
  private Renderer ownRenderer = null;
  private Renderer heigherRenderer = null;

  public static CityObject Create(TypeEnum type, int id, string name)
  {
    CityObject obj = Instantiate(Manager.CityMaker.CityObjectClone).AddComponent<CityObject>();
    obj.Type = type;
    obj.ID = id;
    obj.Name = obj.name = name;

    obj.ChildrenVisible = type != TypeEnum.Method;

    GameObject heighter = Instantiate(Manager.CityMaker.HeigherObjectClone);
    heighter.transform.parent = obj.transform;
    obj.heighter = heighter;

    obj.ownRenderer = obj.GetComponent<Renderer>();
    obj.heigherRenderer = heighter.GetComponent<Renderer>();

    return obj;
  }

  public void AddChild(CityObject child)
  {
    child.Parent = this;

    switch (child.Type) {
      case TypeEnum.Package:
        PackageChildren.Add(child.Name, child);
        break;
      case TypeEnum.Class:
        ClassChildren.Add(child.Name, child);
        break;
      case TypeEnum.Method:
        MethodChildren.Add(child.Name, child);
        break;
    }
  }

  public void SetHeight(float height_0_1, float value)
  {
    Height_0_1 = height_0_1;
    Height = height_0_1 * value;

    // 描画のちらつきを防ぐため、最小の高さを保証
    if (0 < Height && Height <= Manager.ExeTimeDB.MinHeight) Height = Manager.ExeTimeDB.MinHeight;

    heigherRenderer.enabled = Visible && !ChildrenVisible && 0 < Height;

    if (Visible) {
      if (Height <= 0) {
        heigherRenderer.enabled = false;
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

  public void SetVisible(bool visible)
  {
    Visible = visible;

    ownRenderer.enabled = Visible;
    heigherRenderer.enabled = Visible && !ChildrenVisible && 0 < Height;

    // レイヤーを変更
    if (Visible) {
      gameObject.layer = LayerMask.NameToLayer("VisibleCityObject");
    }
    else {
      gameObject.layer = LayerMask.NameToLayer("NonVisibleCityObject");
    }
  }

}
