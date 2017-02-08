using System;
using UnityEngine;

public class VisibleChanger : MonoBehaviour
{

  public Color CursoredColor = Color.black;
  public float DescriptTime = 0;

  [System.NonSerialized]
  public CityObject CursoredObject = null;

  private Color originalColor = Color.black;
  private float time = 0;

  private void Update()
  {
    CheckCursor();
    ChangeByClick();
    Descript();
  }

  private void CheckCursor()
  {
    // レイを飛ばしてオブジェクトを取得
    CityObject cursored = null;
    if (0 <= Input.mousePosition.x
      && Input.mousePosition.x <= Screen.width
      && 0 <= Input.mousePosition.y
      && Input.mousePosition.y <= Screen.height) {
      Ray ray = UI.CameraController.Camera.ScreenPointToRay(Input.mousePosition);
      RaycastHit hit;
      if (Physics.Raycast(ray, out hit, float.MaxValue, 1 << LayerMask.NameToLayer("VisibleCityObject"))) {
        cursored = hit.collider.gameObject.GetComponent<CityObject>();
     }
    }
    // 選択中のオブジェクトが変化した場合
    if (CursoredObject != cursored) {
      time = 0;
      // 選択から外れたオブジェクトの色を戻す
      if (CursoredObject != null) {
        CursoredObject.GetComponent<Renderer>().material.color = originalColor;
        CursoredObject = null;
      }
      // 新たに選択されたオブジェクトの色を変更する
      if (cursored != null) {
        CursoredObject = cursored.GetComponent<CityObject>();
        originalColor = cursored.GetComponent<Renderer>().material.color;
        cursored.GetComponent<Renderer>().material.color = CursoredColor;
      }
    }
    else {
      if (CursoredObject != null && time < DescriptTime) {
        time += Time.deltaTime;
      }
    }
  }

  private void ChangeByClick()
  {
    bool click = Input.GetMouseButtonDown(0);
    if (click && CursoredObject != null && !CursoredObject.IsMethod) {
      if (CursoredObject.ChildrenVisivle) {
        CursoredObject.ChildrenVisivle = false;
        RecSetChildrenVisible(CursoredObject, false);
      }
      else {
        CursoredObject.ChildrenVisivle = true;
        RecSetChildrenVisible(CursoredObject, true);
      }
    }
  }

  private void RecSetChildrenVisible(CityObject obj, bool visible)
  {
    foreach (CityObject child in obj.PackageChildren.Values) {
      child.Visible = visible;
      if (child.ChildrenVisivle) {
        RecSetChildrenVisible(child, visible);
      }
    }
    foreach (CityObject child in obj.ClassChildren.Values) {
      child.Visible = visible;
      if (child.ChildrenVisivle) {
        RecSetChildrenVisible(child, visible);
      }
    }
    foreach (CityObject child in obj.MethodChildren.Values) {
      child.Visible = visible;
      if (child.ChildrenVisivle) {
        RecSetChildrenVisible(child, visible);
      }
    }
  }

  private void Descript()
  {
    // ^p^
    UI.Descriptor.gameObject.SetActive(DescriptTime < time);
  }

}
