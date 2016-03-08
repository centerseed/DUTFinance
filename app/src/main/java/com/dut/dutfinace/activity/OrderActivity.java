package com.dut.dutfinace.activity;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.dut.dutfinace.CustomCircleProgressBar;
import com.dut.dutfinace.R;

public class OrderActivity extends ToolbarActivity {

    CustomCircleProgressBar mCircularProgressBa;
    TextView mSecond;
    boolean mOrdering = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activiy_order);
        mSecond = (TextView) findViewById(R.id.second);
    }

    public void startTimeProgress(final View v) {
        mCircularProgressBa = (CustomCircleProgressBar) findViewById(R.id.circleProgress);
        final int countDownConst = 60;

        new CountDownTimer((countDownConst + 2) * 1000, 1000) {
            int second = countDownConst;
            @Override
            public void onFinish() {
                mSecond.setText("Done!");
                v.setEnabled(true);
                mOrdering = false;
            }

            @Override
            public void onTick(long millisUntilFinished) {
                int progress = countDownConst - second;
                Log.e("OrderActivity", "progress -> " + progress);

                mCircularProgressBa.setProgress(progress * 100/countDownConst);
                mSecond.setText(second-- + "");
            }

        }.start();
        v.setEnabled(false);
        mOrdering = true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mOrdering) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onNetError() {

    }
}
