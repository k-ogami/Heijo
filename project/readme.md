# RocatMonitorVisualizer
可視化プログラムのUnityプロジェクトです。

# RocatMonitorAgent
計測プログラム（Java用）のEclipseプロジェクトです。

# AndrocatMonitorXP
計測プログラム（Android用）のAndroid Studioプロジェクトです。  
ActivityとXposedモジュールの両方が含まれており、プロファイリングの際にはそれぞれ2種類のプロセスで実行されます。  

* jp/naist/rocatmonitor/androcatmonitorxp/MainActivity.java
一般的なアプリケーションと同様、ホームからアイコンをタップして起動します。  
プロファイリング用の値をputExtraして任意のアプリケーションをstartActivityするだけの簡単なアプリケーションです。
```
Intent selectedApp = getPackageManager().getLaunchIntentForPackage(packageName);
if (selectedApp == null) {
  logTextView.setText("Error:Selected app cannot be launched.");
  return;
}
// 転送するデータをここに追加する
{
  selectedApp.putExtra(ConstValue.BUNDLE_TARGET_PACKAGE_NAME, packageName);
  selectedApp.putExtra(ConstValue.BUNDLE_HOST, hostEditText.getText().toString());
  selectedApp.putExtra(ConstValue.BUNDLE_PORT, Integer.valueOf(portEditText.getText().toString()));
  selectedApp.putExtra(ConstValue.BUNDLE_IGNORE_PACKAGE_NAMES, ignoreEditText.getText().toString());
}
startActivity(selectedApp);
```

* jp/naist/rocatmonitor/androcatmonitorxp/XposedModule.java
Xposedフレームワークから実行され、すべてのActivityの起動（android.app.Instrumentatio.newActivity）の直後にフックをかけます。  
Intentを参照し、MainActivityによってセットされた値が存在する場合にのみ、プロファイリング処理の用意を開始します。
```
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
      Logger.write("Starting to monitor \"" + target + "\"");
      // 監視のための初期化処理
      Activity activity = (Activity)methodHookParam.getResult();
      enable = Monitor.getInstance().init(activity, intent, loadPackageParam.appInfo);
    }
});
```
