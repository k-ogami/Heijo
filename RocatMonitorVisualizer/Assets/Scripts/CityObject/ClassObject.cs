using UnityEngine;

public class ClassObject : CityObject 
{

  public static ClassObject Create(string name)
  {
    ClassObject klass = CreatePrefab<ClassObject>();
    klass.name = name;
    klass.type = Type.Class;
    return klass;
  }

}
