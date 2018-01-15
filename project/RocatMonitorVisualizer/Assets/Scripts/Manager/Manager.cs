using UnityEngine;
using System.Collections.Generic;

public class Manager : MonoBehaviour
{

  public static CityMaker CityMaker = null;
  public static CityObjectDB CityObjectDB = null;
  public static ExeTimeDB ExeTimeDB = null;
  public static Connector Connector = null;

#if UNITY_EDITOR
  private IntervalPrinter intervalPrinter = new IntervalPrinter(10);
#endif

  private void Awake()
  {
    CityMaker = GetComponent<CityMaker>();
    CityObjectDB = GetComponent<CityObjectDB>();
    ExeTimeDB = GetComponent<ExeTimeDB>();
    Connector = GetComponent<Connector>();
  }

  private void Update()
  {
    if (Connector.HasReceivedData()) {
      Work();
    }

#if UNITY_EDITOR
    // intervalPrinter.interval();
#endif
}

private void Work()
  {
    // 初生成のとき
    if (Connector.GetConnectFlag()) {
      // 初期化
      CityObjectDB.Init();
      ExeTimeDB.Init();
      // メソッドを登録
      foreach (MethodInfo method in Connector.PopMethodInfo()) {
        CityObjectDB.RegistMethod(method);
      }
      CityMaker.Remake();
    }

    // メソッドの実行情報を登録
    foreach (Message message in Connector.PopReceivedDataList()) {
      ExeTimeDB.RegistInfo(message);
    }
    // オブジェクトの高さを更新
    ExeTimeDB.SetHeight();

  }

}
