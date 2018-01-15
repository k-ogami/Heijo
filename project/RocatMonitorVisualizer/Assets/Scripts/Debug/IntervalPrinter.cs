using UnityEngine;
using System;

public class IntervalPrinter
{

  private readonly int time;
  private readonly string tag;

  private long before = 0;
  private long sum = 0;
  private int counter = 0;

  public IntervalPrinter(int time) : this(time, null) { }

  public IntervalPrinter(int time, string tag)
  {
    this.time = time <= 0 ? 1 : time;
    this.tag = tag == null ? "PS" : tag;
  }

  public void interval()
  {
    long now = DateTime.Now.Ticks;

    if (before == 0) {
      counter++;
      before = now;
      return;
    }

    long diff = now - before;
    sum += diff;

    if (time <= counter) {
      float average = (float)sum / time / 10000;
      if (tag == null) {
        MonoBehaviour.print(String.Format("{0:0.00} [ms]\n", average));
      }
      else {
        MonoBehaviour.print(String.Format("{0:0.00} [ms]\n", average));
      }
      sum = counter = 0;
    }
    before = now;
    counter++;
  }


}