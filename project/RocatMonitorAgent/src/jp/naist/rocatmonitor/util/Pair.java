package jp.naist.rocatmonitor.util;

import java.util.AbstractMap;

// 標準のPairはJava9から使用可能
public class Pair<K, V> extends AbstractMap.SimpleEntry<K, V>
{

  public Pair(final K key, final V value)
  {
    super(key, value);
  }

  public K first()
  {
    return getKey();
  }

  public V second()
  {
    return getValue();
  }

}
