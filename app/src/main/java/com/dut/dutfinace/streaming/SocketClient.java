package com.dut.dutfinace.streaming;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

public class SocketClient {
    public static final String TAG = "SocketClient";
    public static final String ACTION_UPDATE = "com.dut.dutfinance.PRICE_UPDATE";
    public static final String ARG_NAME = "arg_name";
    public static final String ARG_PRICE = "arg_price";

    Context mContext;
    Socket mSocket;
    ByteArrayOutputStream mByteArrayOutputStream = new ByteArrayOutputStream(128);

    public SocketClient(Context context) {
        mContext = context;
    }

    public void connect(String url, int port) {
        disconnect();

        try {
            if (mSocket != null) {
                mSocket.close();
                mSocket = null;
            }

            mSocket = new Socket(url, port);

            mByteArrayOutputStream = new ByteArrayOutputStream(128);

            if (mSocket.isConnected()) {
                InputStream inputStream = mSocket.getInputStream();

                Log.i(TAG, "Socket Connected: " + url + ":" + port);
                byte[] buffer = new byte[64];
                int bytesRead = -1;

                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    String data = "";
                    mByteArrayOutputStream.reset();
                    mByteArrayOutputStream.write(buffer, 0, bytesRead);
                    data += mByteArrayOutputStream.toString();
                    if (data != null && data.contains("T,CURRENCY")) {
                        String str[] = data.split(",");
                        Log.i(TAG, "get: " + str[1] + " value: " + str[8]);
                        sendPriceBroadcast(str[1], Double.valueOf(str[8]));
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
    }

    public void disconnect() {
        if (mSocket != null) {
            try {
                mSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
                mSocket = null;
                Log.e(TAG, e.toString());
            }
        }
    }

    private void sendPriceBroadcast(String name, double price) {
        Intent intent = new Intent();
        intent.setAction(ACTION_UPDATE);
        intent.putExtra(ARG_NAME, name);
        intent.putExtra(ARG_PRICE, price);
        mContext.sendBroadcast(intent);
    }
}
