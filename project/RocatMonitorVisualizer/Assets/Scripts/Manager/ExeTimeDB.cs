using UnityEngine;
using System.Collections.Generic;
using System;

public class ExeTimeDB : MonoBehaviour
{

  public float MaxTime = 0;
  public float HeightHistory = 0;
  public float MinHeight = 0.01f;

  private LinkedList<TimeData> timeDataList = new LinkedList<TimeData>();
  private float history = 0;

  public class TimeData
  {
    public long Time_ns;
    public ExeTimeInfo[] ExeTimeInfos;
  }

  public void Clear()
  {
    timeDataList.Clear();
  }

  public void RegistInfo(ExeTimeInfo[] infos, long time)
  {
    // データを追加
    TimeData timeData = new TimeData();
    timeData.Time_ns = time;
    timeData.ExeTimeInfos = infos;
    timeDataList.AddLast(timeData);
    // 最長保有時間を超えると古いデータから削除
    while (timeDataList.Count != 0 && MaxTime * Mathf.Pow(10, 9) < timeDataList.Last.Value.Time_ns - timeDataList.First.Value.Time_ns) {
      timeDataList.RemoveFirst();
    }
  }

  public void SetHeight()
  {
    // <メソッドID, <スレッドID, 実行時間>>
    Dictionary<long, Dictionary<long, long>> methodDic = new Dictionary<long, Dictionary<long, long>>();

    // 時間範囲内のメソッド毎スレッド毎の実行時間を取得
    long last = timeDataList.Last.Value.Time_ns;
    long length = (long)(HeightHistory * Mathf.Pow(10, 9));
    for (LinkedListNode<TimeData> i = timeDataList.Last; i != null && last - i.Value.Time_ns < length; i = i.Previous) {
      foreach (ExeTimeInfo info in i.Value.ExeTimeInfos) {
        if (!methodDic.ContainsKey(info.MethodID)) {
          methodDic[info.MethodID] = new Dictionary<long, long>();
          methodDic[info.MethodID][info.ThreadID] = info.ExeTime;
        } else {
          if (!methodDic[info.MethodID].ContainsKey(info.ThreadID)) {
            methodDic[info.MethodID].Add(info.ThreadID, info.ExeTime);
          } else {
            methodDic[info.MethodID][info.ThreadID] += info.ExeTime;
          }
        }
      }
      // 保有時間の更新
      history = last - i.Value.Time_ns;
    }

    // 高さをリセット
    RecResetHeight(Manager.CityObjectDB.DefaultPackage);

    // <オブジェクトID, <スレッドID, 実行時間>>
    Dictionary<long, Dictionary<long, long>> objDic = new Dictionary<long, Dictionary<long, long>>();

    // 実行されたメソッドを走査し、自身および親オブジェクトにオブジェクト毎スレッド毎の実行時間を加算していく
    foreach (KeyValuePair<long, Dictionary<long, long>> pair in methodDic) {
      RecAddExeTimeToParents(objDic, pair.Value, Manager.CityObjectDB.MethodDict[pair.Key]);
    }

    // オブジェクトの高さを設定
    foreach (KeyValuePair<long, Dictionary<long, long>> pair in objDic) {
      CityObject obj = Manager.CityObjectDB.ObjectDict[pair.Key];
      // 各スレッドで最も長い実行時間から高さを算出
      long time = 0;
      foreach (long t in pair.Value.Values) {
        if (time < t) {
          time = t;
        }
      }
      obj.Time = time;
      // スレッド数もここで更新
      obj.ThreadNum = pair.Value.Count;
      // 高さを設定
      float height = history != 0 ? obj.Time / history : 0;
      Manager.CityObjectDB.ObjectDict[pair.Key].SetHeight(height);
    }
  }

  private void RecAddExeTimeToParents(Dictionary<long, Dictionary<long, long>> objDic, Dictionary<long, long> data, CityObject obj)
  {
    if (!objDic.ContainsKey(obj.ID)) {
      objDic[obj.ID] = new Dictionary<long, long>(data);
    } else {
      foreach (KeyValuePair<long, long> pair in data) {
        if (!objDic[obj.ID].ContainsKey(pair.Key)) {
          objDic[obj.ID][pair.Key] = pair.Value;
        } else {
          objDic[obj.ID][pair.Key] += pair.Value;
        }
      }
    }
    // 親に再帰
    if (obj.Parent != null) {
      RecAddExeTimeToParents(objDic, data, obj.Parent);
    }
  }
  
  private void RecResetHeight(CityObject obj)
  {
    obj.SetHeight(0);
    foreach (CityObject child in obj.GetChildren()) {
      RecResetHeight(child);
    }
  }
  
}
