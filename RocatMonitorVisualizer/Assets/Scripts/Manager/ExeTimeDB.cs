using UnityEngine;
using System.Collections.Generic;
using System;

public class ExeTimeDB : MonoBehaviour
{

  public float MaxTime = 0;
  public float HeightHistory = 0;
  public float Height = 0;

  private LinkedList<TimeData> timeDataList = new LinkedList<TimeData>();
  private float history = 0;

  public class TimeData
  {
    public long Time_ns;
    public ExeTimeInfo[] ExeTimeInfos;
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
    Dictionary<long, Dictionary<long, long>> dic = new Dictionary<long, Dictionary<long, long>>();

    // 時間範囲内のメソッド毎スレッド毎の実行時間を取得
    long last = timeDataList.Last.Value.Time_ns;
    long length = (long)(HeightHistory * Mathf.Pow(10, 9));
    for (LinkedListNode<TimeData> i = timeDataList.Last; i != null && last - i.Value.Time_ns < length; i = i.Previous) {
      foreach (ExeTimeInfo info in i.Value.ExeTimeInfos) {
        if (!dic.ContainsKey(info.MethodID)) {
          dic[info.MethodID] = new Dictionary<long, long>();
          dic[info.MethodID][info.ThreadID] = info.ExeTime;
        }
        else {
          if (!dic[info.MethodID].ContainsKey(info.ThreadID)) {
            dic[info.MethodID].Add(info.ThreadID, info.ExeTime);
          }
          else {
            dic[info.MethodID][info.ThreadID] += info.ExeTime;
          }
        }
      }
      // 保有時間の更新
      history = last - i.Value.Time_ns;
    }
    // 可視であるオブジェクトの高さを設定
    RecSetHeight(Manager.CityObjectDB.DefaultPackage, dic);
  }

  private void RecSetHeight(CityObject obj, Dictionary<long, Dictionary<long, long>> dic)
  {
    if (!obj.ChildrenVisivle) {
      obj.Height = GetHeight(obj, dic);
    }
    else {
      obj.Height = 0;
      foreach (CityObject child in obj.PackageChildren.Values) {
        RecSetHeight(child, dic);
      }
      foreach (CityObject child in obj.ClassChildren.Values) {
        RecSetHeight(child, dic);
      }
      foreach (CityObject child in obj.MethodChildren.Values) {
        RecSetHeight(child, dic);
      }
    }
  }

  private float GetHeight(CityObject obj, Dictionary<long, Dictionary<long, long>> dic)
  {
    float time = GetTime(obj, dic);
    float height;
    height = history != 0 ? time / history * Height : 0;
    return height;
  }

  private long GetTime(CityObject obj, Dictionary<long, Dictionary<long, long>> dic)
  {
    // オブジェクト以下にあるメソッドID（あるいは自身のメソッドID）を再帰的に取得
    LinkedList<long> methods = RecGetUnderMethodID(obj);
    // スレッド毎時間に変換
    Dictionary<long, long> dic2 = new Dictionary<long, long>();
    foreach (long method in methods) {
      if (dic.ContainsKey(method)) {
        foreach (long thread in dic[method].Keys) {
          if (!dic2.ContainsKey(thread)) {
            if (dic[method].ContainsKey(thread)) {
              dic2[thread] = dic[method][thread];
            }
          }
          else {
            if (dic[method].ContainsKey(thread)) {
              dic2[thread] += dic[method][thread];
            }
          }
        }
      }
    }
    // 各スレッドで最も長い実行時間から高さを算出
    long time = 0;
    foreach (long t in dic2.Values) {
      if (time < t) {
        time = t;
      }
    }
    return time;
  }

  private LinkedList<long> RecGetUnderMethodID(CityObject obj, LinkedList<long> list = null)
  {
    if (list == null) {
      list = new LinkedList<long>();
    }
    if (obj.IsMethod) {
      list.AddLast(((MethodObject)obj).MethodID);
    }
    else {
      foreach (CityObject child in obj.PackageChildren.Values) {
        RecGetUnderMethodID(child, list);
      }
      foreach (CityObject child in obj.ClassChildren.Values) {
        RecGetUnderMethodID(child, list);
      }
      foreach (CityObject child in obj.MethodChildren.Values) {
        RecGetUnderMethodID(child, list);
      }
    }
    return list;
  }

}
