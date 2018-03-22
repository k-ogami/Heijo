using UnityEngine;
using UnityEngine.UI;
using System;

public class Descriptor : MonoBehaviour
{

  public Vector2 Margin = Vector2.zero;

  private Image ui_panel = null;
  private Text ui_text = null;
  private RectTransform rectTransform = null;

  private void Awake()
  {
    ui_panel = GetComponentInChildren<Image>();
    ui_text = GetComponentInChildren<Text>();
    rectTransform = GetComponent<RectTransform>();
  }

  public void SetText(string text)
  {
    ui_text.text = text;
    ui_text.rectTransform.sizeDelta = new Vector2(ui_text.preferredWidth, ui_text.preferredHeight);//.rectTransform.sizeDelta.y);
    ui_panel.rectTransform.sizeDelta = ui_text.rectTransform.sizeDelta + Margin;
  }

  public void SetVisible(bool visible)
  {
    ui_panel.gameObject.SetActive(visible);
    ui_text.gameObject.SetActive(visible);
  }

  public void SetTransform(Vector2 position)
  {
    rectTransform.position = position + ui_panel.rectTransform.sizeDelta.y / 2 * Vector2.up;
  }

}
