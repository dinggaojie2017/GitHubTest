package com.fontlose.relayctrl;

import java.io.IOException;
import java.net.Socket;

import android.os.Message;
import android.util.Log;

public class ReadThread extends Thread {
    boolean state;
    HandleMsg hOptMsg;
    byte[] sData;
    Socket socket;
    String[] armData = new String[]{"a", "b", "c"};
    public int datalen;

    public ReadThread(HandleMsg hmsg, byte[] sData, Socket socket, String[] armData) {
        hOptMsg = hmsg;
        this.sData = sData;
        this.socket = socket;
        this.armData = armData;
    }
    /*
	 * @see java.lang.Thread#run()
	 * 线程接收主循环
	 */

    public void run() {
        int rlRead;
        state = true;
        try {
            while (state) {
                rlRead = socket.getInputStream().read(sData);//对方断开返回-1
                if (rlRead > 0) {
                    datalen = rlRead;
                    Log.v("aaa", "length:" + rlRead);
                    unpackageCmd(sData, rlRead);
                } else {
                    state = false;
                    hOptMsg.sendEmptyMessage(DataProcess.CLOSETCP);
                    break;
                }
            }
        } catch (Exception e) {
            Log.v("aaa", e.getMessage());
            state = false;
            hOptMsg.sendEmptyMessage(DataProcess.CLOSETCP);
        }
    }


    public void abortRead() {
        if (socket == null) return;
        try {
            socket.shutdownInput();
            socket.shutdownOutput();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        state = false;
    }

    public String unpackageCmd(byte[] cmd, int len) {
        String acsii = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        char[] ascii_char = acsii.toCharArray();
        int size = len;
        if (size <= 0) return null;
        String s = "";
        for (int i = 0; i < size; i++) {
            if (cmd[i] >= 48 && cmd[i] <= 57) { //0-9
                s += cmd[i] - 48;
            } else if (cmd[i] >= 65 && cmd[i] <= 90) {//A-Z
                s += ascii_char[cmd[i] - 39];
            } else if (cmd[i] >= 97 && cmd[i] <= 122) {//a-z
                if (cmd[i] == 122) s += "0";
                else {
                    s += ascii_char[cmd[i] - 97];
                }
            } else if (cmd[i] < 16) {
                s += cmd[i];
            } else {
                s += " ";
            }
        }
        String c1 = s.substring(4);
        String c2 = s.substring(0, 4);
        Log.v("aaa", "c1:" + c1);
        Log.v("aaa", "c2:" + c2);
        boolean l = c2.equals("Temp");
        boolean l1 = c2.equals("Humt");
        boolean l2 = c2.equals("Flux");
        if (l == true) armData[0] = c1;
        if (l1 == true) armData[1] = c1;
        if (l2 == true)
            armData[2] = c1.substring(0, c1.length() - 2) + "." + c1.substring(c1.length() - 2, c1.length());
        Log.v("aaa", "ascii：" + s);
        return s;
    }
}










