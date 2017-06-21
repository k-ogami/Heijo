package rocatmonitor;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import rocatmonitor.json.ExeTimeInfo;
import rocatmonitor.json.MethodInfo;
import rocatmonitor.json.RootJSON;

public class Monitor
{

  private static class TimeFrame
  {
    public TimeFrame(long methodID, long time)
    {
      MethodID = methodID;
      Time = time;
    }

    public long MethodID;
    public long Time;
  }

  private static class ExeTimeKey
  {
    public ExeTimeKey(long threadID, long methodID)
    {
      ThreadID = threadID;
      MethodID = methodID;
    }

    @Override
    public boolean equals(Object obj)
    {
      if (obj instanceof ExeTimeKey) {
        ExeTimeKey key = (ExeTimeKey)obj;
        return this.ThreadID == key.ThreadID && this.MethodID == key.MethodID;
      } else {
        return false;
      }
    }

    @Override
    public int hashCode()
    {
      return Objects.hash(ThreadID, MethodID);
    }

    public long ThreadID;
    public long MethodID;
  }

  // メソッドと開始時間のスタックトレース。<スレッドID, スタックトレース>
  private static Map<Long, List<TimeFrame>> timeStackMap = new HashMap<Long, List<TimeFrame>>();
  // スレッド・メソッドごとの実行時間のマップ。送信先に教えるとクリアされる。<<スレッドID, メソッドID>, 実行時間>
  private static Map<ExeTimeKey, Long> exeTimeMap = new HashMap<ExeTimeKey, Long>();
  // OneLoadモードでない時に使用。初めてロードされたクラスのメソッド情報のリスト。送信先に教えるとクリアされる
  private static List<MethodInfo> newLoadMethodList = new LinkedList<MethodInfo>();

  private static Object lock = new Object();
  private static boolean isAlive = true;
  private static long time = 0;

  public static void MethodEnter(long methodID)
  {
    if (!isAlive) return;

    synchronized (lock) {
      // 時刻を更新
      time = System.nanoTime();

      // スレッドIDを取得
      long threadID = Thread.currentThread().getId();

      // スタックを取得
      if (!timeStackMap.containsKey(threadID)) {
        timeStackMap.put(threadID, new LinkedList<TimeFrame>());
      }
      List<TimeFrame> stack = timeStackMap.get(threadID);

      // 最後のメソッドの実行時間を加算
      if (timeStackMap.get(threadID).size() != 0) {
        TimeFrame frame_top = stack.get(stack.size() - 1);
        AddExeTime(threadID, frame_top.MethodID, time - frame_top.Time);
      }

      // 新たなメソッドの開始を追加
      TimeFrame frame_enter = new TimeFrame(methodID, time);
      stack.add(frame_enter);
    }
  }

  public static void MethodExit(long methodID)
  {
    if (!isAlive) return;

    synchronized (lock) {
      // 時刻を更新
      time = System.nanoTime();

      // スレッドIDを取得
      long threadID = Thread.currentThread().getId();

      // スタックを取得
      List<TimeFrame> stack = timeStackMap.get(threadID);

      // 記録されている最後のメソッドと実際のメソッドが不一致である場合は無視
      if (stack.size() == 0 || methodID != timeStackMap.get(threadID).get(timeStackMap.get(threadID).size() - 1).MethodID) {
        return;
      }

      // 終了したメソッドの実行時間を加算
      TimeFrame frame_exit = stack.get(stack.size() - 1);
      AddExeTime(threadID, frame_exit.MethodID, time - frame_exit.Time);

      // 終了したメソッドを除去
      stack.remove(stack.size() - 1);
      if (stack.size() == 0) {
        timeStackMap.remove(threadID);
      }

      // 最後のメソッドのフレームへ戻った時間を記憶
      if (stack.size() != 0) {
        stack.get(stack.size() - 1).Time = time;
      }
    }
  }

  public static void Interval() throws IOException
  {
    synchronized (lock) {
      // 時刻を更新
      time = System.nanoTime();

      // 最後のメソッドの実行時間を加算
      for (Entry<Long, List<TimeFrame>> entry : timeStackMap.entrySet()) {
        long threadID = entry.getKey();
        List<TimeFrame> stack = entry.getValue();
        if (stack.size() != 0) {
          TimeFrame frame_top = stack.get(stack.size() - 1);
          AddExeTime(threadID, frame_top.MethodID, time - frame_top.Time);
        }
      }

      // 最後のメソッドの開始時間を更新
      for (Entry<Long, List<TimeFrame>> entry : timeStackMap.entrySet()) {
        List<TimeFrame> stack = entry.getValue();
        if (stack.size() != 0) {
          stack.get(stack.size() - 1).Time = time;
        }
      }

      // JSONを生成して送信
      RootJSON json = CreateJSON();
      if (json != null) {
        Agent.Connector.Send(json);
      }

      // 削除
      exeTimeMap.clear();
    }
  }

  public static void RegistMethod(MethodInfo method)
  {
    if (!isAlive)
      return;

    synchronized (lock) {
      newLoadMethodList.add(method);
    }
  }

  private static void AddExeTime(long threadID, long methodID, long time)
  {
    ExeTimeKey key = new ExeTimeKey(threadID, methodID);
    if (!exeTimeMap.containsKey(key)) {
      exeTimeMap.put(key, time);
    } else {
      Long value = exeTimeMap.get(key);
      value += time;
      exeTimeMap.put(key, value);
    }
  }

  private static RootJSON CreateJSON()
  {
    RootJSON json = new RootJSON();
    json.Time = time;
    for (MethodInfo methodInfo : newLoadMethodList) {
      json.MethodInfos.add(methodInfo);
    }
    newLoadMethodList.clear();
    for (Entry<ExeTimeKey, Long> entry : exeTimeMap.entrySet()) {
      json.ExeTimeInfos.add(new ExeTimeInfo(entry.getKey().ThreadID, entry.getKey().MethodID, entry.getValue()));
    }
    return json;
  }

}
