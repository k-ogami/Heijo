using UnityEngine;
using System;
using System.Collections.Generic;
using System.Text;
using System.Net;
using System.Net.Sockets;
using System.Threading;
using System.IO;

public class Connector : MonoBehaviour
{

  public const int HEADER_SIZE = 4;

  public int Port = 0;

  [Header("Test")]
  public bool IsConnected = false;
  public bool TestButton = false;
  public string TestFilePath = null;
  public bool PrintLog = false;

  [Header("SaveJSON")]
  public bool SaveJSON = false;
  public string SavePath = null;
  public string SaveFileName = null;

  private TcpClient socket = new TcpClient();
  private Thread thread = null;

  // 受信したデータを蓄積
  private LinkedList<RootJSON> receivedDataList = new LinkedList<RootJSON>();

  private int save_file_num = 0;

  private bool connect_flag = false;

  private void Awake()
  {
    // 接続・受信用スレッドを生成
    thread = new Thread(ThreadLoop);
    thread.Start();
  }

  private void Update()
  {
    // テスト用
    if (TestButton) {
      if (Manager.CityObjectDB.DefaultPackage == null) {
        connect_flag = true;
      }
      TestButton = false;
      AddTestJSON(TestFilePath);
    }
  }

  private void OnApplicationQuit()
  {
    // スレッド終了
    if (thread != null && thread.IsAlive) {
      thread.Abort();
    }
    // 切断
    if (socket != null && socket.Connected) {
      socket.Close();
    }
  }

  // 受信したデータを抜き出す
  public List<RootJSON> PopReceivedDataList()
  {
    lock (receivedDataList) {
      if (receivedDataList.Count == 0) {
        return null;
      }
      else {
        List<RootJSON> list = new List<RootJSON>(receivedDataList);
        receivedDataList.Clear();
        return list;
      }
    }
  }

  // 受信したデータを持っているか否か
  public bool HasReceivedData()
  {
    return receivedDataList.Count != 0;
  }

  public bool GetConnectFlag()
  {
    bool flag = connect_flag;
    connect_flag = false;
    return flag;
  }

  // 別スレッドで処理される処理
  private void ThreadLoop()
  {
    while (true) {
      try {
        Accept();  // 接続が完了するまでブロック
        Receive(); // 受信ループ。通信が切断されるまでブロック
      }
      catch (Exception e) {
        print(e);
      }
    }
  }

  // テスト用
  private void AddTestJSON(string path)
  {
    try {
      StreamReader stream = new StreamReader(TestFilePath, Encoding.GetEncoding("UTF-8"));
      string input = stream.ReadToEnd();
      stream.Close();
      RootJSON json = JsonUtility.FromJson<RootJSON>(input);
      lock (receivedDataList) {
        receivedDataList.AddLast(json);
      }
    }
    catch (FileNotFoundException) {
      print("エラー：テストファイル\"" + TestFilePath + "\"が見つかりません。");
    }
    catch (ArgumentException) {
      print("エラー：テストファイル\"" + TestFilePath + "\"のデシリアライズに失敗しました。");
    }
  }

  // 接続待ち
  private void Accept()
  {
    IsConnected = false;

    // 切断処理
    if (socket.Connected) {
      socket.Close();
      receivedDataList.Clear();
    }

    TcpListener listener = new TcpListener(IPAddress.Any, Port);
    listener.Start();
    socket = listener.AcceptTcpClient();
    listener.Stop();
    IsConnected = true;
    connect_flag = true;
  }

  // 受信ループ処理
  private void Receive()
  {
    NetworkStream stream = socket.GetStream();
    byte[] header = new byte[HEADER_SIZE];

    // ヘッダ受信→ペイロード受信→ヘッダ受信→...の繰り返し
    while (true) {
      Thread.Sleep(1);
      int payload_size = 0;
      {
        if (PrintLog) {
          print("------------------------------");
          print("Connector:ヘッダ受信開始");
        }
        int h_count = HEADER_SIZE;
        while (h_count != 0) {
          Thread.Sleep(1);
          h_count -= stream.Read(header, HEADER_SIZE - h_count, h_count);
        }
        payload_size = BitConverter.ToInt32(header, 0);
        if (PrintLog) {
          print("Connector:ヘッダ受信完了");
        }
      }
      // ペイロード読み込み
      if (PrintLog) {
        print("Connector:ペイロード（" + payload_size + "バイト）受信開始");
      }
      string text = null;
      {
        int p_count = payload_size;
        byte[] payload = new byte[payload_size];
        while (p_count != 0) {
          Thread.Sleep(1);
          p_count -= stream.Read(payload, payload_size - p_count, p_count);
        }
        text = Encoding.UTF8.GetString(payload);
        // JSONテキストをファイルとして保存する（デバッグ用）
        if (SaveJSON) {
          Save(text);
        }
      }
      if (PrintLog) {
        print("Connector:ペイロード受信完了");
      }
      // 受信したJSONテキストをデシリアライズしてリストに格納
      {
        RootJSON json = JsonUtility.FromJson<RootJSON>(text);
        lock (receivedDataList) {
          receivedDataList.AddLast(json);
        }
      }
    }
  }

  private void Save(string text)
  {
    string filename;
    if (SavePath[SavePath.Length - 1] != '/' && SavePath[SavePath.Length - 1] != '\\') {
      SavePath += "/";
    }
    for (; ; save_file_num++) {
      filename = SavePath + SaveFileName + "_" + save_file_num + ".txt";
      if (!File.Exists(filename)) {
        break;
      }
    }
    StreamWriter writer = new StreamWriter(filename, false, Encoding.GetEncoding("UTF-8"));
    writer.Write(text);
    writer.Close();
  }

}
