using UnityEngine;
using System.Collections.Generic;

public class CityObject : MonoBehaviour
{

  public long ID = 0;

  public string Name = null;

  public float WidthX = 0;
  public float WidthY = 0;

  public CityObjectType Type = CityObjectType.None;

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

  public static CityObject Create(CityObjectType type, long id, string name)
  {
    CityObject obj = GameObject.CreatePrimitive(PrimitiveType.Cube).AddComponent<CityObject>();

    obj.Type = type;
    obj.ID = id;
    obj.Name = obj.name = name;

    GameObject heighter = Instantiate(Manager.CityMaker.HeigherObject);
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
      case CityObjectType.Package:
        PackageChildren.Add(child.Name, child);
        break;
      case CityObjectType.Class:
        ClassChildren.Add(child.Name, child);
        break;
      case CityObjectType.Method:
        MethodChildren.Add(child.Name, child);
        break;
    }
  }

}
