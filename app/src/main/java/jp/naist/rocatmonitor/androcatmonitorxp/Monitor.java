package jp.naist.rocatmonitor.androcatmonitorxp;

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
import jp.naist.rocatmonitor.androcatmonitorxp.dex.ClassVisitor;
import jp.naist.rocatmonitor.androcatmonitorxp.dex.FileVisitor;

public class Monitor
{

  private static Monitor instance = null;

  public static Monitor getInstance()
  {
    if (instance == null) instance = new Monitor();
    return instance;
  }

  public StructureDB StructureDB = new StructureDB();
  public Connector Connector = new Connector();

  public Activity Activity = null;
  public Intent Intent = null;
  public ApplicationInfo AppInfo = null;

  public boolean init(Activity activity, Intent intent, ApplicationInfo appInfo)
  {
    this.Activity = activity;
    this.Intent = intent;
    this.AppInfo = appInfo;
    try {
      setIgnorePackage();
      collectAllClasses();
      // connect();
      // startThread();
    } catch (Exception e) {
      XposedBridge.log(e);
      return false;
    }
    return true;
  }

  private void setIgnorePackage()
  {
    // このパッケージ自身も監視対象外にセットする
    StructureDB.IgnorePackageNameSet.add(ConstValue.THIS_PACKAGE_NAME + ".*");
    // 入力されたパッケージを監視対象外にセットする
    String input = Intent.getStringExtra(ConstValue.BUNDLE_IGNORE_PACKAGE_NAMES);
    String[] tokens = input.split(",|\n");
    for (String token : tokens) {
      String pack = token.replaceAll(" |\t", "");
      if (pack.length() == 0) continue;
      StructureDB.IgnorePackageNameSet.add(pack);
    }
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

}
