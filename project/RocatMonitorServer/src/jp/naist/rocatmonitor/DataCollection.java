package jp.naist.rocatmonitor;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import jp.naist.rocatmonitor.debug.DebugValue;
import jp.naist.rocatmonitor.message.Message;
import jp.naist.rocatmonitor.message.MethodData;

public class DataCollection
{

  public List<Message> History = new LinkedList<>();
  public List<MethodData> MethodDatas = new LinkedList<>();

  private Map<Long, MethodData> debugIdNameMap = null;

  public DataCollection()
  {
    if (DebugValue.DEBUG_FLAG && DebugValue.DEBUG_CAN_MAPPING_ID_TO_NAME) {
      debugIdNameMap = new HashMap<>();
    }
  }

  public void add(List<Message> messages)
  {
    for (Message message : messages) {
      // メソッド情報を保存
      if (0 < message.MethodDatas.size()) {
        MethodDatas.addAll(message.MethodDatas);
        if (DebugValue.DEBUG_FLAG && DebugValue.DEBUG_CAN_MAPPING_ID_TO_NAME) {
          for (MethodData method : message.MethodDatas) {
            debugIdNameMap.put(method.MethodID, method);
          }
        }
        continue;
      }
      History.add(message);
    }
    // 一定数を超えると古いデータから削除
    while (ConstValue.DATA_COLLECTION_SIZE < History.size()) {
      History.remove(0);
    }
  }

}
