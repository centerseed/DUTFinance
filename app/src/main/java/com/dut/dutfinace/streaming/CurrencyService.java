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

/**
 * Created by Mac on 16/3/17.
 */
public class CurrencyService extends Service {

    public static final String TAG = "CurrencyService";
    public static final String ARG_START_CONNECT = "_start_connect";
    public static final String ARG_DISCONNECT = "_disconnect";
    public static final String ACTION_UPDATE = "com.dut.dutfinance.PRICE_UPDATE";
    static final int CONNECTED = 0;
    static final int DISCONNECT = 1;
    int mStatus = DISCONNECT;

    String URL = "";
    int PORT = 999;

    Socket mSocket;
    private BufferedWriter mBufferedWriter;
    private BufferedReader mBufferedReader;

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        if (intent != null) {
            if (ARG_START_CONNECT.equals(intent.getAction())) {
                if (mStatus == CONNECTED) disconnect();
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
        if (mSocket == null) try {
            mSocket = new Socket(URL, PORT);

            mBufferedWriter = new BufferedWriter(new OutputStreamWriter(mSocket.getOutputStream()));
            mBufferedReader = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));

            if (mSocket.isConnected()) {
                Log.i(TAG, "Socket Connected: " + URL);
                mStatus = CONNECTED;

                while (mSocket.isConnected()) {
                    String data = mBufferedReader.readLine();
                    if (data != null) {
                        sendPriceBroadcase(0.0);
                    }
                }
            } else {
                mStatus = DISCONNECT;
            }

            mSocket = null;
            mStatus = DISCONNECT;
        } catch (Exception e) {
            mSocket = null;
            mStatus = DISCONNECT;
            Log.e(TAG, e.toString());
        }
    }

    private void disconnect() {
        if (mSocket != null) {
            try {
                mSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
                mSocket = null;
                mStatus = DISCONNECT;
                Log.e(TAG, e.toString());
            }
        }
    }

    private void sendPriceBroadcase(double price) {
        Intent intent = new Intent();
        intent.setAction(CurrencyService.ACTION_UPDATE);
        intent.putExtra(CurrencyService.ACTION_UPDATE, price);
        getApplicationContext().sendBroadcast(intent);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
