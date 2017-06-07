using UnityEngine;
using System.Text;

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
  }

  private void CheckCursor()
  {
    CityObject cursored = null;

    if (!UI.IsPointerOverUI()) {      
      // レイを飛ばしてオブジェクトを取得
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
    }

    // 選択中のオブジェクトが変化した場合
    if (CursoredObject != cursored) {
      time = 0;
      UI.Descriptor.SetVisible(false);
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
      if (CursoredObject != null) {
        SetText();
        // 一定時間カーソルを合わせていたら、説明用のパネルを表示
        if (DescriptTime < time) {
          UI.Descriptor.SetVisible(true);
          UI.Descriptor.SetTransform(Input.mousePosition);
        }
        else {
          time += Time.deltaTime;
        }
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
    foreach (CityObject child in obj.GetChildren()) {
      child.SetVisible(visible);
      if (child.ChildrenVisivle) {
        RecSetChildrenVisible(child, visible);
      }
    }

    /*
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
    */
  }

  private void SetText()
  {
    StringBuilder str = new StringBuilder();
    if (CursoredObject.IsPackage) {
      str.Append("Package Name\t:" + CursoredObject.name);
    }
    else if (CursoredObject.IsClass) {
      str.Append("Class Name\t:" + CursoredObject.name);
    }
    else  {
      str.Append("Method Name\t:" + ((MethodObject)CursoredObject).GetPerfectName());
    }
    float rate = CursoredObject.Time / (Manager.ExeTimeDB.HeightHistory * Mathf.Pow(10, 9));
    if (1 < rate) rate = 1;
    if (0.01 < rate || rate == 0) {
      str.Append("\nExecution Rate\t:" + rate.ToString("0.00%"));
    }
    else {
      str.Append("\nExecution Rate\t:" + (rate * 100).ToString("E2") + "%");
    }
    str.Append("\nThread Num\t\t:" + CursoredObject.ThreadNum);
    UI.Descriptor.SetText(str.ToString());
  }

}
