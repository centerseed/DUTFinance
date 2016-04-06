package com.dut.dutfinace.streaming;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.UnknownHostException;

public class CurrencyService extends Service {

    public static final String TAG = "CurrencyService";
    public static final String ARG_START_CONNECT = "_start_connect";
    public static final String ARG_DISCONNECT = "_disconnect";
    public static final String ACTION_UPDATE = "com.dut.dutfinance.PRICE_UPDATE";

    final String URL = "1.34.243.17";

    SocketClient mEURUSDClient;
    SocketClient mGBPJPYSocket;
    SocketClient mGBPUSDSocket;
    SocketClient mXAUUSDSocket;

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        if (intent != null) {
            if (ARG_START_CONNECT.equals(intent.getAction())) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        connect();
                    }
                }).start();
            }

            if (ARG_DISCONNECT.equals(intent.getAction())) {
                disconnect();
            }
        }
        return Service.START_STICKY;
    }

    private void connect() {
        mEURUSDClient = new SocketClient(getApplicationContext());
        mGBPJPYSocket = new SocketClient(getApplicationContext());
        mGBPUSDSocket = new SocketClient(getApplicationContext());
        mXAUUSDSocket = new SocketClient(getApplicationContext());

        new Thread(new Runnable() {
            @Override
            public void run() {
                mEURUSDClient.connect(URL, 6101);
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                mGBPJPYSocket.connect(URL, 6102);
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                mGBPUSDSocket.connect(URL, 6103);
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                mXAUUSDSocket.connect(URL, 6104);
            }
        }).start();
    }

    private void disconnect() {
        if (null != mEURUSDClient) mEURUSDClient.disconnect();
        if (null != mGBPJPYSocket) mGBPJPYSocket.disconnect();
        if (null != mGBPUSDSocket) mGBPUSDSocket.disconnect();
        if (null != mXAUUSDSocket) mXAUUSDSocket.disconnect();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
