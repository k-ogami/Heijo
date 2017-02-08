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

    CityMaker.Initialize();
  }

  private void Update()
  {
    if (Connector.HasReceivedData()) {
      CityMaker.Work(Connector.PopReceivedDataList());
    }
  }

}
