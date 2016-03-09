package com.dut.dutfinace.activity;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.dut.dutfinace.CustomCircleProgressBar;
import com.dut.dutfinace.R;
import com.dut.dutfinace.activity.ToolbarActivity;

public class CountdownActivity extends ToolbarActivity {

    CustomCircleProgressBar mCircularProgressBar;
    FrameLayout mProgressBackground;
    TextView mSecond;
    boolean mOrdering = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_countdown);
        mSecond = (TextView) findViewById(R.id.second);
        mProgressBackground = (FrameLayout) findViewById(R.id.progressBack);

        startTimeProgress();

        int side = getIntent().getIntExtra(OrderActivity.ARG_SIDE, 0);
        if (side == OrderActivity.SIDE_UP) {
            mProgressBackground.setBackgroundResource(R.drawable.rect_green);
        } else {
            mProgressBackground.setBackgroundResource(R.drawable.rect_red);
        }
    }

    @Override
    public void onNetError() {

    }


    public void startTimeProgress() {
        mCircularProgressBar = (CustomCircleProgressBar) findViewById(R.id.circleProgress);
        final int countDownConst = 60;

        new CountDownTimer((countDownConst + 2) * 1000, 1000) {
            int second = countDownConst;
            @Override
            public void onFinish() {
                mSecond.setText("Done!");
                mCircularProgressBar.setProgress(100);
                mOrdering = false;
            }

            @Override
            public void onTick(long millisUntilFinished) {
                int progress = countDownConst - second;
                Log.e("OrderActivity", "progress -> " + progress);

                mCircularProgressBar.setProgress(progress * 100/countDownConst);
                mSecond.setText(second-- + "");
            }

        }.start();
        mOrdering = true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mOrdering) {
            return false;
        }
        return super.onOptionsItemSelected(item);
    }
}
