using UnityEngine;

public class UI : MonoBehaviour
{

  public static CameraController CameraController = null;
  public static VisibleChanger VisibleChanger = null;
  public static Descriptor Descriptor = null;

  private void Awake()
  {
    CameraController = GetComponent<CameraController>();
    VisibleChanger = GetComponent<VisibleChanger>();
    Descriptor = GetComponentInChildren<Descriptor>();
  }

}
