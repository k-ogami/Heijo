package jp.naist.rocatmonitor.androcatmonitorxp;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class XposedModule implements IXposedHookLoadPackage {

  public class ConnectionThread extends  Thread {
    public  boolean flag = false;
    @Override
    public void run() {
      try {
        ServerSocket server = new ServerSocket(8000);
        server.accept();
        flag = true;
      } catch (IOException e) {
        XposedBridge.log(e);
        flag = true;
      }
    }
  }

  @Override
  public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {

    String targetNmae = "org.bobstuff.bobball";

    XposedBridge.log(loadPackageParam.packageName + "\t == \t" + targetNmae + " ? : " + loadPackageParam.packageName.equals(targetNmae));
    if (loadPackageParam.packageName.equals(targetNmae)) {
      Socket socket = new Socket("163.221.172.210", 8001);
      XposedBridge.log("Connected!");
    }
  }

}
