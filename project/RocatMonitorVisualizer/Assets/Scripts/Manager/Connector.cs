using UnityEngine;
using System;
using System.Collections.Generic;
using System.Net.Sockets;
using System.Threading;
using System.IO;
using MsgPack.Serialization;
using System.Net;

public class Connector : MonoBehaviour
{

  public const int HEADER_SIZE = 4;

  public int Port = 0;

  [Header("Debug")]
  public bool Connected = false;
  public bool DebugPrintLog = false;

  private TcpClient socket = new TcpClient();
  private Thread thread = null;

  // 受信したデータを蓄積
  private LinkedList<Message> receivedDataList = new LinkedList<Message>();

  private bool connect_flag = false;
  private bool close_flag = false;

  MessagePackSerializer<Message> serializer = SerializationContext.Default.GetSerializer<Message>();

  private void Awake()
  {
    // 接続・受信用スレッドを生成
    thread = new Thread(ThreadLoop);
    thread.Start();
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

  // 受信したデータを持っているか否か
  public bool HasReceivedData()
  {
    return receivedDataList.Count != 0;
  }

  // 初接続フラグを取得。取得後はfalseになる
  public bool GetConnectFlag()
  {
    bool flag = connect_flag;
    connect_flag = false;
    return flag;
  }

  // 切断フラグを取得。取得後はfalseになる
  public bool GetCloseFlag()
  {
    bool flag = close_flag;
    close_flag = false;
    return flag;
  }

  // 初接続後、メソッド情報を抜き出す
  public List<MethodInfo> PopMethodInfo()
  {
    lock (receivedDataList) {
      if (receivedDataList.Count == 0) {
        return null;
      }
      return receivedDataList.First.Value.Methods;
    }
  }

  // 受信したデータを抜き出す
  public List<Message> PopReceivedDataList()
  {
    lock (receivedDataList) {
      if (receivedDataList.Count == 0) {
        return null;
      }
      else {
        List<Message> list = new List<Message>(receivedDataList);
        receivedDataList.Clear();
        return list;
      }
    }
  }

  // 別スレッドで処理される処理
  private void ThreadLoop()
  {
    while (true) {
      try {
        Accept();  // 接続が完了するまでブロック
        Receive(); // 受信ループ。通信が切断されるまでブロック
        connect_flag = Connected = false;
        close_flag = true;
      } catch (Exception e) { print(e); }
    }
  }

  // 接続待ち
  private void Accept()
  {
    // 切断処理
    if (socket.Connected) {
      socket.Close();
    }
    receivedDataList.Clear();

    TcpListener listener = new TcpListener(IPAddress.Any, Port);
    listener.Start();
    socket = listener.AcceptTcpClient();
    listener.Stop();
    connect_flag = Connected = true;
    close_flag = false;
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
        if (DebugPrintLog) {
          print("------------------------------");
          print("Connector:ヘッダ受信開始");
        }
        int h_count = HEADER_SIZE;
        while (h_count != 0) {
          Thread.Sleep(1);
          int read = 0;
          bool close = false;
          try {
            read = stream.Read(header, HEADER_SIZE - h_count, h_count);
          }
          catch (IOException) { }
          if (read == 0) close = true;
          if (close) {
            if (DebugPrintLog) {
              print("Connector:ヘッダ受信中に切断されました");
            }
            return;
          }
          h_count -= read;
        }
        Array.Reverse(header);
        payload_size = BitConverter.ToInt32(header, 0);
        if (DebugPrintLog) {
          print("Connector:ヘッダ受信完了");
        }
      }
      // ペイロード読み込み
      if (DebugPrintLog) {
        print("Connector:ペイロード（" + payload_size + "バイト）受信開始");
      }
      byte[] payload = new byte[payload_size];
      {
        int p_count = payload_size;
        while (p_count != 0) {
          Thread.Sleep(1);
          int read = 0;
          bool close = false;
          try {
            read = stream.Read(payload, payload_size - p_count, p_count);
          }
          catch (IOException) { }
          if (read == 0) close = true;
          if (close) {
            if (DebugPrintLog) {
              print("Connector:ヘッダ受信中に切断されました");
            }
            return;
          }
          p_count -= read;
        }
      }
      if (DebugPrintLog) {
        print("Connector:ペイロード受信完了");
      }
      {
        Message message = serializer.Unpack(new MemoryStream(payload));
        lock (receivedDataList) {
          receivedDataList.AddLast(message);
        }
      }
    }
  }

  public void Close()
  {
    if (socket.Connected) {
      socket.Close();
    }
    connect_flag = Connected = false;
  }

}
