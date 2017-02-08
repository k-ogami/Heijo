using UnityEngine;
using System.Text;

public class MethodObject : CityObject
{

  public long MethodID = -1;
  public string RetType = null;
  public string[] ArgTypes = null;
  
  public static MethodObject Create(string name, long methodID, string ret, string[] args)
  {
    MethodObject method = CreatePrefab<MethodObject>();
    method.name = name;
    method.MethodID = methodID;
    method.RetType = ret;
    method.ArgTypes = args;
    method.type = Type.Method;
    method.ChildrenVisivle = false;
    return method;
  }

  public string GetPerfectName()
  {
    StringBuilder str = new StringBuilder();
    if (RetType != null) {
      str.Append(RetType + " ");
    }
    str.Append(name + "(");
    for (int i = 0; i < ArgTypes.Length; i++) {
      str.Append(ArgTypes[i]);
      if (i != ArgTypes.Length - 1) {
        str.Append(", ");
      }
    }
    str.Append(")");
    return str.ToString();
  }

}

