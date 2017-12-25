using UnityEngine;

public class CameraController : MonoBehaviour
{
  public Camera Camera = null;

  // ホイールドラッグで移動する速度
  public float MoveSpeed = 0f;
  // 右ドラッグで向きを回転する速度
  public float RotateSpeed = 0f;
  // ホイールで拡大縮小する速度
  public float ZoomSpeed = 0f;

  // 前フレームでのマウス位置
  private Vector3 preMousePos = Vector3.zero;

  private void Update()
  {
    MouseUpdate();
  }

  // Sceneビューのカメラと同じように、移動・回転・拡大縮小を行う
  private void MouseUpdate()
  {
    // クリックされたタイミングで、マウスの初期座標を更新
    if (Input.GetMouseButtonDown(1) || Input.GetMouseButtonDown(2)) {
      preMousePos = Input.mousePosition;
    }
    // マウス位置の差を取得
    Vector3 delta = Input.mousePosition - preMousePos;
    // 拡大縮小
    float wheel = Input.GetAxis("Mouse ScrollWheel");
    if (wheel != 0f && mouseInsideScreen()) {
      Camera.transform.position += Camera.transform.forward * wheel * ZoomSpeed;
    }
    // 回転
    if (Input.GetMouseButton(1)) {
      Camera.transform.RotateAround(Camera.transform.position, Camera.transform.right, -delta.y * RotateSpeed);
      Camera.transform.RotateAround(Camera.transform.position, Vector3.up, delta.x * RotateSpeed);
    }
    // 移動
    if (Input.GetMouseButton(2)) {
      Camera.transform.Translate(-delta * MoveSpeed);
    }
    // 前フレームでのマウス位置の更新
    preMousePos = Input.mousePosition;
  }

  private bool mouseInsideScreen()
  {
    float x = Input.mousePosition.x;
    float y = Input.mousePosition.y;
    return 0 <= x && x < Screen.width && 0 <= y && y < Screen.height;
  }

}
