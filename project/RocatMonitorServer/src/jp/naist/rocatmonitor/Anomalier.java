package jp.naist.rocatmonitor;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import jp.naist.rocatmonitor.debug.DebugValue;
import jp.naist.rocatmonitor.debug.IntervalPrinter;
import jp.naist.rocatmonitor.message.AnomalyData;
import jp.naist.rocatmonitor.message.Message;
import jp.naist.rocatmonitor.message.SamplingData;
import us.jubat.anomaly.AnomalyClient;
import us.jubat.common.Datum;

public class Anomalier
{

  public List<AnomalyData> ResultHistory = new LinkedList<>();

  private final AnomalyClient client;
  private final DataCollection collection;
  private final Config config;

  private int addAnomalyCount = 0;
  private Map<Long, Long> methodIdSampleMap = new HashMap<>();
  private List<AnomalyData> anomalyRetList = new LinkedList<>();

  private IntervalPrinter debugAnomalyIntervalPrinter = null;

  public Anomalier(AnomalyClient client, DataCollection collection, Config config)
  {
    this.client = client;
    this.collection = collection;
    this.config = config;

    if (DebugValue.DEBUG_FLAG && DebugValue.DEBUG_PRINT_ANOMALY_INTERVAL_FLAG) {
      debugAnomalyIntervalPrinter = new IntervalPrinter(DebugValue.DEBUG_PRINT_ANOMALY_INTERVAL_TIME, "ANOMALY");
    }
  }

  public int checkAddCount(List<Message> messages)
  {
    int size = messages.size();
    if (messages.get(0).Time == 0) size -= 1;
    if (size == 0) return 0;
    addAnomalyCount += size;
    int t = addAnomalyCount / config.AnomalyWindowStep;
    addAnomalyCount %= config.AnomalyWindowStep;
    return t;
  }

  public AnomalyData[] addAnomaly(int count, boolean returnAll)
  {
    for (int i = count; 0 < i; i--) {
      int index = collection.History.size() - (i * config.AnomalyWindowSize - (i - 1) * config.AnomalyWindowStep);
      if (index < 0) continue;
      int sumSampling = 0;
      // WindowSizeぶんのサンプル数を合算する
      for (int j = 0; j < config.AnomalyWindowSize; j++) {
        Message message = collection.History.get(index);
        sumSampling += message.SamplingCount;
        for (SamplingData data : message.SamplingDatas) {
          if (!methodIdSampleMap.containsKey(data.MethodID)) {
            methodIdSampleMap.put(data.MethodID, 0L);
          }
          methodIdSampleMap.put(data.MethodID, methodIdSampleMap.get(data.MethodID) + data.Sample);
        }
      }
      // Datumを生成してAnomalyに追加
      Datum datum = new Datum();
      AnomalyData result = new AnomalyData();
      for (Entry<Long, Long> entry : methodIdSampleMap.entrySet()) {
        // 0~1の値に正規化する
        double sampleRate = (double)entry.getValue() / sumSampling;
        datum.addNumber(entry.getKey().toString(), sampleRate);
        result.MethodIdSampleRateMap.put(entry.getKey(), sampleRate);
      }
      float score;
      if (DebugValue.DEBUG_FLAG && DebugValue.DEBUG_NO_JUBATUS) {
        score = 1F;
      } else {
        score = result.Score = client.add(datum).score;
      }
      if (DebugValue.DEBUG_FLAG && DebugValue.DEBUG_PRINT_ANOMALY_INTERVAL_FLAG) {
        debugAnomalyIntervalPrinter.interval();
      }
      if (returnAll || config.AnomalyThreshold < result.Score) {
        anomalyRetList.add(result);
      }
      // デバッグ用
      debugPrintAnomalyResult(methodIdSampleMap, score, sumSampling, index);
      // 結果を保存。一定数以上貯まると古い物から削除（DATA_COLLECTION_SIZEに依存）
      ResultHistory.add(result);
      removeOldResult();
      methodIdSampleMap.clear();
    }

    AnomalyData[] ret = anomalyRetList.toArray(new AnomalyData[] {});
    anomalyRetList.clear();
    return ret;
  }

  private void removeOldResult()
  {
    int reqSize = ConstValue.DATA_COLLECTION_SIZE / (config.AnomalyWindowSize - config.AnomalyWindowStep);
    while (reqSize < ResultHistory.size()) {
      ResultHistory.remove(0);
    }
  }

  private void debugPrintAnomalyResult(Map<Long, Long> map, float score, long sum, int index)
  {
    if (!DebugValue.DEBUG_FLAG || !DebugValue.DEBUG_PRINT_ANOMALY_RESULT) return;

    long start = collection.History.get(index).Time;
    long end = collection.History.get(index + config.AnomalyWindowSize - 1).Time;

    if (config.AnomalyThreshold < score && score < ConstValue.ANOMALY_TOO_LARGE_VALUE) {
      System.err.printf("%1.2e\t%d~%d\t", score, start, end);
      for (Entry<Long, Long> entry : map.entrySet()) {
        System.err.printf(" (%d, %.2f)", entry.getKey(), (double)entry.getValue() / sum);
      }
      System.err.println();
    } else {
      System.out.printf("%1.2e\t%d~%d\t", score, start, end);
      for (Entry<Long, Long> entry : map.entrySet()) {
        System.out.printf(" (%d, %.2f)", entry.getKey(), (double)entry.getValue() / sum);
      }
      System.out.println();
    }
  }

}
