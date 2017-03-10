package com.fontlose.relayctrl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

public class DataProcess {

    public static byte RELAYOPT = 1;
    public static byte RELAYCHK = 2;
    public static byte APPQUIT = 4;
    public static byte CLOSETCP = 5;
    public int ccc;
    protected Socket socket;//Socket 数据
    //目标端口
    public boolean State;
    public static byte[] sData = new byte[1024];//接收缓存
    public static String[] armData = new String[]{"a", "b", "c"};//ARM数据接收缓存
    ReadThread readThread = null;
    HandleMsg hOptMsg = null;
    Context mct = null;

    public DataProcess(HandleMsg hmsg, Context context) {
        socket = new Socket();
        hOptMsg = hmsg;
        mct = context;
    }

    public String[] getsData() {
        return armData;
    }

    public void sendCmd(String cmd) throws IOException {
        byte[] send = null;
        boolean l = false;
        send = cmd.getBytes();
        try {
            while (l != true) {
                l = sendData(send);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean sendData(byte[] data) throws IOException {
        // TODO Auto-generated method stub

        OutputStream out = socket.getOutputStream();
        if (out == null) return false;
        out.write(data);
        return true;
    }

    public boolean getData(byte[] sdata) throws IOException {

        int rlRead;
        boolean state = true;
        try {
            while (state) {
                rlRead = socket.getInputStream().read(sdata);//对方断开返回-1
                if (rlRead > 0) {
                    Log.v("aaa", "state true");
                } else {
                    Log.v("aaa", "state false");
                    state = false;
                    break;
                }
            }
        } catch (Exception e) {
            Log.v("aaa", e.getMessage());
            state = false;
            return false;
        }
        return true;
    }

    /**
     * 停止socket
     *
     * @param handler
     */
    public void stopConn(final Handler handler) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    socket.close();
                    handler.sendEmptyMessage(2);
                    State = false;
                    if (readThread == null)
                        readThread.abortRead();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * 开始socket
     *
     * @param handler
     */
    public void startConn(final String ip, final int port, final Handler handler) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (socket.isClosed()) socket = new Socket();
                SocketAddress remoteAddr = new InetSocketAddress(ip, port);
                try {
                    socket.connect(remoteAddr, 2000);
                } catch (IOException e) {
                    socket = new Socket();
                    Log.v("aaa", e.getMessage());

                    handler.sendEmptyMessage(0);
                    return;
                }
                readThread = new ReadThread(hOptMsg, sData, socket, armData);
                readThread.start();
                State = true;
                handler.sendEmptyMessage(1);
            }
        }).start();
    }


    public byte[] packageCmd(byte id, byte opt) {

        if (id > 5) return null;

        byte[] cmd = new byte[]{0x55, 0x01, 0x01, 0, 0, 0, 0, 0};
        if (id == 5) {
            cmd[2] = 0;
        } else if (id == 0) {
            cmd[3] = opt;
            cmd[4] = opt;
            cmd[5] = opt;
            cmd[6] = opt;
        } else cmd[2 + id] = opt;
        cmd[7] = (byte) (cmd[0] + cmd[1] + cmd[2] + cmd[3] + cmd[4] + cmd[5] + cmd[6]);
        return cmd;
    }
}
