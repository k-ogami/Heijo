using UnityEngine;

public class PackageObject : CityObject
{

  public static PackageObject Create(string name)
  {
    PackageObject package = CreatePrefab<PackageObject>();
    package.name = name;
    package.type = Type.Package;
    return package;
  }

}


