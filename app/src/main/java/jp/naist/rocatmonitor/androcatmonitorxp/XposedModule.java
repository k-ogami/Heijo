package jp.naist.rocatmonitor.androcatmonitorxp;

import android.app.Activity;
import android.content.Intent;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class XposedModule implements IXposedHookLoadPackage
{

  // onDestroyなど他でhookするときにフラグとして使用する予定
  private static boolean enable = false;

  @Override
  public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable
  {
    // FIXME Noxのときでは使えた省略処理が実機（Android7.0,huawei nova lite）では動作しない？
    //if (!loadPackageParam.isFirstApplication || !loadPackageParam.packageName.equals(loadPackageParam.processName)) return;
    // FIXME GoogleStoreでインストール時にBadParcelableException: ClassNotFoundException when unmarchalling。Noxだけ？
    //if (loadPackageParam.packageName.equals("com.android.vending")) return;

    // アプリケーションの起動にフックする
    XposedHelpers.findAndHookMethod(
            "android.app.Instrumentation", loadPackageParam.classLoader,
            "newActivity", ClassLoader.class, String.class, Intent.class,
            new XC_MethodHook()
    {
      @Override
      protected void afterHookedMethod(MethodHookParam methodHookParam) throws Throwable
      {
        // Intentにセットされた値（アプリケーションのパッケージ名）から監視対象であるか否かを判別する
        if (methodHookParam.args[2] == null) return;
        Intent intent = (Intent)methodHookParam.args[2];
        String target = intent.getStringExtra(ConstValue.BUNDLE_TARGET_PACKAGE_NAME);
        if (target == null || !target.equals(loadPackageParam.packageName)) return;
        XposedBridge.log("Starting to monitor \"" + target + "\"");
        // 監視のための初期化処理
        Activity activity = (Activity)methodHookParam.getResult();
        enable = Monitor.getInstance().init(activity, intent, loadPackageParam.appInfo);
      }
    });
  }

}
