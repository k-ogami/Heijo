using UnityEngine;
using UnityEngine.EventSystems;
using System.Collections.Generic;

public class UI : MonoBehaviour
{

  public static CameraController CameraController = null;
  public static VisibleChanger VisibleChanger = null;
  public static Descriptor Descriptor = null;
  public static HeightSlider HeightSlider = null;
  public static VisibilityButton VisibilityButton = null;
  public static ConnectionUI ConnectionUI = null;

  private void Awake()
  {
    CameraController = GetComponent<CameraController>();
    VisibleChanger = GetComponent<VisibleChanger>();
    Descriptor = GetComponentInChildren<Descriptor>();
    HeightSlider = GetComponentInChildren<HeightSlider>();
    VisibilityButton = GetComponent<VisibilityButton>();
    ConnectionUI = GetComponent<ConnectionUI>();
  }

  // マウスカーソルがUIの上にあるか否か
  public static bool IsPointerOverUI()
  {
    PointerEventData pointer = new PointerEventData(EventSystem.current);
    pointer.position = Input.mousePosition;
    List<RaycastResult> list = new List<RaycastResult>();
    // レイを飛ばしてヒットするオブジェクトを取得
    EventSystem.current.RaycastAll(pointer, list);
    return list.Count != 0;
  }

}
