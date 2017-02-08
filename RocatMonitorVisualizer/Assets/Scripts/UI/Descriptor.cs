using UnityEngine;
using UnityEngine.UI;
using System.Collections.Generic;

public class Descriptor : MonoBehaviour
{

  public Image UI_panel = null;
  public Text UI_name = null;
  public Text UI_time = null;
  public Text UI_thread = null;

  [System.NonSerialized]
  public RectTransform rectTransform = null;

  private void Awake()
  {
    rectTransform = GetComponent<RectTransform>();
    SetText("a", "bb", "ccccccccccccccc");
  }

  public void SetText(string name, string time, string thread)
  {
    UI_name.text = name;
    UI_time.text = time;
    UI_thread.text = thread;
    float max = 0;
    max = max < UI_name.preferredWidth ? UI_name.preferredWidth : max;
    max = max < UI_time.preferredWidth ? UI_time.preferredWidth : max;
    max = max < UI_thread.preferredWidth ? UI_thread.preferredWidth : max;
    UI_panel.rectTransform.sizeDelta = new Vector2(max + 20f, UI_panel.rectTransform.sizeDelta.y);
  }

}
