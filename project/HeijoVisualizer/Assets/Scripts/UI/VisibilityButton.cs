using UnityEngine;

public class VisibilityButton : MonoBehaviour
{

  // 可視レベルをクラスを持つパッケージに設定する
  public void OnPackageButton()
  {

    if (Manager.CityObjectDB.DefaultPackage == null) return;
    RecPackage(Manager.CityObjectDB.DefaultPackage);
  }

  private void RecPackage(CityObject obj, bool flag = false)
  {
    bool hasClass = flag;

    if (obj.Type == CityObject.TypeEnum.Package) {
      if (flag) {
        obj.ChildrenVisible = false;
        obj.SetVisible(false);
      }
      else {
        if (obj.ClassChildren.Keys.Count == 0) {
          obj.ChildrenVisible = true;
          obj.SetVisible(true);
        }
        else {
          hasClass = true;
          obj.ChildrenVisible = false;
          obj.SetVisible(true);
        }
      }
    }
    else {
      obj.ChildrenVisible = false;
      obj.SetVisible(false);
    }
    foreach (CityObject child in obj.Children) {
      RecPackage(child, hasClass);
    }
  }

  public void OnClassButton()
  {
    // 可視レベルをクラスに設定する（インナークラスは非表示）

    if (Manager.CityObjectDB.DefaultPackage == null) return;
    RecClass(Manager.CityObjectDB.DefaultPackage);
  }

  private void RecClass(CityObject obj, bool flag = false)
  {
    bool hasClass = flag;

    if (obj.Type == CityObject.TypeEnum.Package) {
      obj.ChildrenVisible = true;
      obj.SetVisible(true);
    }
    else if (obj.Type == CityObject.TypeEnum.Class) {
      if (flag) {
        obj.ChildrenVisible = false;
        obj.SetVisible(false);
      }
      else {
        hasClass = obj.ClassChildren.Keys.Count != 0;
        obj.ChildrenVisible = false;
        obj.SetVisible(true);
      }
    }
    else {
      obj.ChildrenVisible = false;
      obj.SetVisible(false);
    }
    foreach (CityObject child in obj.Children) {
      RecClass(child, hasClass);
    }
  }

  public void OnMethodButton()
  {
    // すべてを可視にする

    if (Manager.CityObjectDB.DefaultPackage == null) return;
    RecMethod(Manager.CityObjectDB.DefaultPackage);
  }

  private void RecMethod(CityObject obj)
  {
    obj.ChildrenVisible = obj.Type != CityObject.TypeEnum.Method;
    obj.SetVisible(true);

    foreach (CityObject child in obj.Children) {
      RecMethod(child);
    }
  }

}
