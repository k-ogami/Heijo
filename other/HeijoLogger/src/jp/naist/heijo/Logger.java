package jp.naist.heijo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;

import jp.naist.heijo.json.ExeTimeJson;
import jp.naist.heijo.message.Message;
import net.arnx.jsonic.JSON;

public class Logger
{

  public static void main(String[] args) throws IOException
  {

    System.out.println("接続待ち……");

    // 接続待ち
    ServerSocket server = new ServerSocket(8000);
    Socket socket = server.accept();
    server.close();

    System.out.println("接続完了");

    Connector connector = new Connector(socket);
    List<ExeTimeJson> exeTimeJsons = new LinkedList<>();

    // 終了時にexecution.json書き出し
    Runtime.getRuntime().addShutdownHook(new Thread()
    {
      @Override
      public void run()
      {
        try {
          FileOutputStream exeOutput = new FileOutputStream(new File("execution.json"));
          synchronized (exeTimeJsons) {
            exeOutput.write(JSON.encode(exeTimeJsons).getBytes("UTF-8"));
          }
          exeOutput.close();
          System.out.println("execution.jsonを出力");
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    });

    // データ受信ループ
    while (socket.isConnected()) {

      // データを受信
      Message message = null;
      try {
        message = connector.read(Message.class);
      } catch (Exception e) {
        break;
      }

      // メソッド情報を受信したとき（初回）
      if (message.Methods != null && 0 < message.Methods.size()) {
        byte[] bytes = JSON.encode(message.Methods).getBytes("UTF-8");
        FileOutputStream methodOutput = new FileOutputStream(new File("methods.json"));
        methodOutput.write(bytes);
        methodOutput.close();
        System.out.println("methods.jsonを出力");
      }

      // 実行情報を受信したとき
      if (message.ExeTimes != null && 0 < message.ExeTimes.size()) {
        ExeTimeJson jsonObj = new ExeTimeJson();
        jsonObj.CurrentTime = message.CurrentTime;
        jsonObj.ExeTimes = message.ExeTimes;
        synchronized (exeTimeJsons) {
          exeTimeJsons.add(jsonObj);
        }
      }

    }
  }

}
