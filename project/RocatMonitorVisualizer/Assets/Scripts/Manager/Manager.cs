using UnityEngine;
using System.Collections.Generic;

public class Manager : MonoBehaviour
{

  public static CityMaker CityMaker = null;
  public static CityObjectDB CityObjectDB = null;
  public static ExeTimeDB ExeTimeDB = null;
  public static Connector Connector = null;

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
  }
  
  private void Work()
  {
    // 初生成のとき
    if (Connector.GetConnectFlag()) {
      // 初期化
      CityObjectDB.Init();
      ExeTimeDB.Init();
      // メソッドを登録
      foreach (MethodInfo info in Connector.PopMethodInfo()) {
        CityObjectDB.RegistMethod(info);
      }
      // 非同期に街の構築を開始
      CityMaker.AsyncMake()
    }

    // 街の構築が完了しているとき
    if (CityMaker.IsMade) {
      // メソッドの実行情報を登録
      foreach (RootJSON json in Connector.PopReceivedDataList()) {
          ExeTimeDB.RegistInfo(json);
      }
      // オブジェクトの高さを更新
      ExeTimeDB.SetHeight();
    }

  }

}
