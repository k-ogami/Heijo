package jp.naist.heijo.android;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;

import com.googlecode.d2j.reader.DexFileReader;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import de.robv.android.xposed.XposedBridge;
import jp.naist.heijo.android.dex.ClassVisitor;
import jp.naist.heijo.android.dex.FileVisitor;
import jp.naist.heijo.android.timer.Scheduler;

public class Monitor
{

  private static Monitor instance = null;

  // FIXME too long
  public static Monitor getInstance()
  {
    if (instance == null) instance = new Monitor();
    return instance;
  }

  public Activity Activity = null;
  public Intent Intent = null;
  public ApplicationInfo AppInfo = null;

  public Config Config = new Config();
  public StructureDB StructureDB = new StructureDB();
  public Connector Connector = new Connector();
  public Scheduler Scheduler = new Scheduler();

  public boolean init(Activity activity, Intent intent, ApplicationInfo appInfo)
  {
    this.Activity = activity;
    this.Intent = intent;
    this.AppInfo = appInfo;
    try {
      readConfig();
      setIgnorePackage();
      collectAllClasses();
      connect();
      startThread();
    } catch (Exception e) {
      XposedBridge.log(e);
      return false;
    }
    return true;
  }

  private void readConfig()
  {
    Config.Host = Intent.getStringExtra(ConstValue.BUNDLE_HOST);
    Config.Port = Intent.getIntExtra(ConstValue.BUNDLE_PORT, 0);

    // IgnorePackage
    String input = Intent.getStringExtra(ConstValue.BUNDLE_IGNORE_PACKAGE_NAMES);
    String[] tokens = input.split(",|\n");
    for (String token : tokens) {
      String pack = token.replaceAll(" |\t", "");
      if (pack.length() == 0) continue;
      Config.IgnorePackages.add(pack);
    }

    // TODO 変更可能にする？ [ms]
    Config.SampleInterval = 2;
    Config.UpdateInterval = 100;
  }

  private void setIgnorePackage()
  {
    StructureDB.IgnorePackageNameSet.addAll(Config.IgnorePackages);
  }

  private void collectAllClasses() throws IOException
  {
    ZipFile apk = new ZipFile(AppInfo.sourceDir);
    Enumeration<? extends ZipEntry> entries = apk.entries();
    while (entries.hasMoreElements()) {
      ZipEntry entry = entries.nextElement();
      String path = entry.getName();
      if (!(path.startsWith("classes") && path.endsWith(".dex"))) continue;
      // dexのbytesを取得
      InputStream input = apk.getInputStream(entry);
      ByteArrayOutputStream output = new ByteArrayOutputStream();
      byte[] buffer = new byte[1024];
      int count;
      while ((count = input.read(buffer)) != -1) {
        output.write(buffer, 0, count);
      }
      output.flush();
      output.close();
      input.close();
      byte[] bytes = output.toByteArray();
      // dexから得たクラス、メソッド情報を登録する
      DexFileReader reader = new DexFileReader(bytes);
      ClassVisitor cv = new ClassVisitor();
      FileVisitor fv = new FileVisitor(cv);
      reader.accept(fv);
    }
    apk.close();
  }

  private void connect() throws Exception
  {
    Connector.connect(Config.Host, Config.Port);
  }

  private void startThread()
  {
    Scheduler.start();
  }

}
