using UnityEngine;
using System.Text;

public class VisibleChanger : MonoBehaviour
{

  public Color CursoredColor = Color.black;
  public float DescriptTime = 0;
  public float CatchDoubleClickTime = 0;

  [System.NonSerialized]
  public CityObject CursoredObject = null;

  private Color originalColor = Color.black;
  private float cursoringTime = 0;
  private float preDoubleClickTime = 0;

  private void Update()
  {
    CheckCursor();
    ChangeByDoubleClick();
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
      cursoringTime = 0;
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
        if (DescriptTime < cursoringTime) {
          UI.Descriptor.SetVisible(true);
          UI.Descriptor.SetTransform(Input.mousePosition);
        }
        else {
          cursoringTime += Time.deltaTime;
        }
      }
    }
  }

  private void SetText()
  {
    StringBuilder str = new StringBuilder();
    if (CursoredObject.Type == CityObject.TypeEnum.Package) {
      str.Append("Package Name\t:" + CursoredObject.Name);
    }
    else if (CursoredObject.Type == CityObject.TypeEnum.Class) {
      str.Append("Class Name\t\t:" + CursoredObject.Name);
    }
    else {
      str.Append("Method Name\t:" + CursoredObject.Name);
    }
    if (0.01 < CursoredObject.Height_0_1 || CursoredObject.Height_0_1 == 0) {
      str.Append("\nElevation\t\t\t:" + CursoredObject.Height_0_1.ToString("0.00%"));
    }
    else {
      str.Append("\nElevation\t\t\t:" + (CursoredObject.Height_0_1 * 100).ToString("E2") + "%");
    }
    str.Append("\nThread Num\t\t:" + CursoredObject.ThreadNum);
    UI.Descriptor.SetText(str.ToString());
  }

  private void ChangeByDoubleClick()
  {
    bool click = Input.GetMouseButtonDown(0);
    bool doubleClick = false;
    if (click) {
      float now = Time.time;
      float interval = now - preDoubleClickTime;
      if (interval < CatchDoubleClickTime) {
        doubleClick = true;
        preDoubleClickTime = 0;
      }
      else {
        preDoubleClickTime = now;
      }
    }

    if (doubleClick && CursoredObject != null) {

      // メソッドをダブルクリックした場合、親となるクラスを閉じる
      CityObject target = CursoredObject.Type == CityObject.TypeEnum.Method ?  CursoredObject.Parent : CursoredObject;

      if (target.ChildrenVisible) {
        target.ChildrenVisible = false;
        RecSetChildrenVisible(target, false);
      }
      else {
        target.ChildrenVisible = true;
        RecSetChildrenVisible(target, true);
      }
      target.SetVisible(true);
    }
  }

  private void RecSetChildrenVisible(CityObject obj, bool visible)
  {
    foreach (CityObject child in obj.Children) {
      if (child.ChildrenVisible) {
        RecSetChildrenVisible(child, visible);
      }
      child.SetVisible(visible);
    }
  }

}
