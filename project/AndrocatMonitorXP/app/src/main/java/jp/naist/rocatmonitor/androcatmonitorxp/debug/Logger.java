package jp.naist.rocatmonitor.androcatmonitorxp.debug;

import android.util.Log;

import de.robv.android.xposed.XposedBridge;
import jp.naist.rocatmonitor.androcatmonitorxp.ConstValue;

public class Logger
{

  public static void write(String text)
  {
    Log.v(ConstValue.LOG_TAG, text);
    XposedBridge.log(ConstValue.LOG_TAG + ":" + text);
  }

}
