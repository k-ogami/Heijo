using UnityEngine;
using UnityEngine.UI;

// ポート番号などの設定のために拡張するかも

public class ConnectionUI : MonoBehaviour
{

  // エディタから参照させること
  public Button Button = null;

  public void OnCloseButton()
  {
    Manager.Connector.Close();
    Closed();
  }

  public void Connected()
  {
    Button.interactable = true;
    Button.GetComponentInChildren<Text>().text = "Close";
  }

  public void Closed()
  {
    Button.interactable = false;
    Button.GetComponentInChildren<Text>().text = "Waiting...";
  }

}