using UnityEngine;
using System.Collections.Generic;
using System;

public class ExeTimeDB : MonoBehaviour
{

  public double MaxTime = 60;
  public double HeightHistory = 0;
  public float MinHeight = 0.01f;

  private LinkedList<Message> messages = new LinkedList<Message>();
  private double history = 0;

  public void Init()
  {
    messages.Clear();
  }

  public void RegistInfo(Message message)
  {
    // データを追加
    messages.AddLast(message);
    // 最長保有時間を超えると古いデータから削除
    while (messages.Count != 0 && MaxTime * Mathf.Pow(10, 3) < messages.Last.Value.CurrentTime - messages.First.Value.CurrentTime) {
      messages.RemoveFirst();
    }
  }

  public void SetHeight()
  {
    // <メソッドID, <スレッドID, 実行時間>>
    Dictionary<int, Dictionary<long, double>> methodDic = new Dictionary<int, Dictionary<long, double>>();

    // 時間範囲内のメソッド毎スレッド毎の実行時間を取得
    long last = messages.Last.Value.CurrentTime;
    long length = (long)(HeightHistory * Mathf.Pow(10, 3));
    for (LinkedListNode<Message> i = messages.Last; i != null && last - i.Value.CurrentTime < length; i = i.Previous) {
      foreach (ExeTimeInfo info in i.Value.ExeTimes) {
        if (!methodDic.ContainsKey(info.MethodID)) {
          methodDic[info.MethodID] = new Dictionary<long, double>();
          methodDic[info.MethodID][info.ThreadID] = info.ExeTime;
        }
        else {
          if (!methodDic[info.MethodID].ContainsKey(info.ThreadID)) {
            methodDic[info.MethodID].Add(info.ThreadID, info.ExeTime);
          }
          else {
            methodDic[info.MethodID][info.ThreadID] += info.ExeTime;
          }
        }
      }
      // 保有時間の更新
      history = last - i.Value.CurrentTime;
    }

    // 高さをリセット
    RecResetHeight();

    // <オブジェクトID, <スレッドID, 実行時間>>
    Dictionary<int, Dictionary<long, double>> objDic = new Dictionary<int, Dictionary<long, double>>();

    // 実行されたメソッドを走査し、自身および親オブジェクトにオブジェクト毎スレッド毎の実行時間を加算していく
    foreach (KeyValuePair<int, Dictionary<long, double>> pair in methodDic) {
      RecAddExeTimeToParents(objDic, pair.Value, Manager.CityObjectDB.MethodDict[pair.Key]);
    }

    // オブジェクトの高さを設定
    foreach (KeyValuePair<int, Dictionary<long, double>> pair in objDic) {
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
      float height;
      height = history != 0 ? (float)(obj.Time / history) : 0;
      if (1 < height) height = 1;
      Manager.CityObjectDB.ObjectDict[pair.Key].SetHeight(height, UI.HeightSlider.Slider.value);
    }
  }

  private void RecAddExeTimeToParents(Dictionary<int, Dictionary<long, double>> objDic, Dictionary<long, double> data, CityObject obj)
  {
    if (!objDic.ContainsKey(obj.ID)) {
      objDic[obj.ID] = new Dictionary<long, double>(data);
    }
    else {
      foreach (KeyValuePair<long, double> pair in data) {
        if (!objDic[obj.ID].ContainsKey(pair.Key)) {
          objDic[obj.ID][pair.Key] = pair.Value;
        }
        else {
          objDic[obj.ID][pair.Key] += pair.Value;
        }
      }
    }
    // 親に再帰
    if (obj.Parent != null) {
      RecAddExeTimeToParents(objDic, data, obj.Parent);
    }
  }

  private void RecResetHeight()
  {
    foreach (CityObject obj in Manager.CityObjectDB.ObjectDict.Values) {
      obj.ThreadNum = 0; // スレッド数もここでリセット
      obj.Time = 0;
      obj.SetHeight(0, UI.HeightSlider.Slider.value);
    }
  }

}
