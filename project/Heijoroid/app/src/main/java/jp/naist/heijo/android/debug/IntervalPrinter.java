package jp.naist.heijo.android.debug;

import java.util.Formatter;

import de.robv.android.xposed.XposedBridge;

public class IntervalPrinter
{

  private final int time;
  private final String tag;

  private long before = 0;
  private long sum = 0;
  private int counter = 0;

  private Formatter formatter = new Formatter();

  public IntervalPrinter(int time)
  {
    this(time, null);
  }

  public IntervalPrinter(int time, String tag)
  {
    this.time = time <= 0 ? 1 : time;
    this.tag = tag == null ? "PS" : tag;
  }

  public void interval()
  {
    long now = System.nanoTime();

    if (before == 0) {
      counter++;
      before = now;
      return;
    }

    long diff = now - before;
    sum += diff;

    if (time <= counter) {
      double average = (double)sum / time / 1000000;
      if (tag == null) {
        XposedBridge.log(formatter.format("%.2f [ms]\n", average).toString());
      }
      else {
        XposedBridge.log(formatter.format("[" + tag + "] %.2f [ms]\n", average).toString());
      }
      sum = counter = 0;
    }
    before = now;
    counter++;
  }

}
