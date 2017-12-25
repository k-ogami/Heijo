using UnityEngine;
using UnityEngine.UI;

public class HeightSlider : MonoBehaviour
{

  [System.NonSerialized]
  public Slider Slider = null;

  protected void Awake()
  {
    Slider = GetComponent<Slider>();

    // イベントの追加
    Slider.onValueChanged.AddListener(OnValueChanged);
  }

  private void OnValueChanged(float value)
  {
    // 高さを更新
    RecSetHeight(Manager.CityObjectDB.DefaultPackage);
  }

  private void RecSetHeight(CityObject obj)
  {
    obj.SetHeight(obj.Height_0_1, Slider.value);
    foreach (CityObject child in obj.PackageChildren.Values) {
      RecSetHeight(child);
    }
    foreach (CityObject child in obj.ClassChildren.Values) {
      RecSetHeight(child);
    }
    foreach (CityObject child in obj.MethodChildren.Values) {
      RecSetHeight(child);
    }
  }

}
