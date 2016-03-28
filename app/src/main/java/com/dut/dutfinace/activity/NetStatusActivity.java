package com.dut.dutfinace.activity;

import android.support.v7.app.AppCompatActivity;

import com.dut.dutfinace.ToastUtils;
import com.dut.dutfinace.network.AsyncResponseParser;

import java.io.IOException;

public class NetStatusActivity extends AppCompatActivity implements AsyncResponseParser.NetError{
    @Override
    public void onNetError(final IOException e) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ToastUtils.showNetErrorToast(getApplicationContext(), e.toString());
            }
        });
    }

    @Override
    public void onResponseError(final int error) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ToastUtils.showNetErrorToast(getApplicationContext(), error);
            }
        });
    }
}
