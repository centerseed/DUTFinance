package com.dut.dutfinace.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.dut.dutfinace.AccountUtils;
import com.dut.dutfinace.Const;
import com.dut.dutfinace.CustomCircleProgressBar;
import com.dut.dutfinace.DigitUtils;
import com.dut.dutfinace.JSONBuilder;
import com.dut.dutfinace.NewPriceText;
import com.dut.dutfinace.PriceTextView;
import com.dut.dutfinace.R;
import com.dut.dutfinace.URLBuilder;
import com.dut.dutfinace.activity.ToolbarActivity;
import com.dut.dutfinace.network.AsyncResponseParser;
import com.dut.dutfinace.streaming.CurrencyService;
import com.dut.dutfinace.streaming.SocketClient;

import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class CountdownActivity extends ToolbarActivity {

    CustomCircleProgressBar mCircularProgressBar;
    FrameLayout mProgressBackground;
    TextView mSecond;
    TextView mSide;
    TextView mName;
    TextView mExchangeRate;
    TextView mAmount;
    ImageView mDivider;
    NewPriceText mPrice;

    int mColor;
    int mOrderID;

    boolean mOrdering = false;
    private final OkHttpClient mClient = new OkHttpClient();
    BroadcastReceiver mReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_countdown);
        mSecond = (TextView) findViewById(R.id.second);
        mName = (TextView) findViewById(R.id.name);
        mSide = (TextView) findViewById(R.id.side);
        mExchangeRate = (TextView) findViewById(R.id.exchange_rate);
        mAmount = (TextView) findViewById(R.id.amount);
        mProgressBackground = (FrameLayout) findViewById(R.id.progressBack);
        mDivider = (ImageView) findViewById(R.id.divider);
        mPrice = (NewPriceText) findViewById(R.id.price);
        mPrice.setTextSize(48);

        String name = getIntent().getStringExtra(OrderActivity.ARG_TARGET_NAME);
        mPrice.setPrice(PreferenceManager.getDefaultSharedPreferences(this).getFloat(name, 0));
        mName.setText(name);
        mPrice.setLastPointNum(DigitUtils.getDigit(name));

        mSide.setText(getIntent().getIntExtra(OrderActivity.ARG_SIDE, 0) == 1 ? "看漲" : "看跌");
        mExchangeRate.setText(getIntent().getDoubleExtra(OrderActivity.ARG_EXCHANGE_RATE, 1) + "");
        mAmount.setText(getIntent().getIntExtra(OrderActivity.ARG_AMOUNT, 10) + "");

        mOrderID = getIntent().getIntExtra(OrderActivity.ARG_ORDER_ID, 0);

        startTimeProgress();

        int side = getIntent().getIntExtra(OrderActivity.ARG_SIDE, 0);
        if (side == OrderActivity.SIDE_UP) {
            mColor = getResources().getColor(R.color.colorUp);
            mProgressBackground.setBackgroundResource(R.drawable.rect_green);
        } else {
            mColor = getResources().getColor(R.color.colorDown);
            mProgressBackground.setBackgroundResource(R.drawable.rect_red);
        }

        mName.setTextColor(mColor);
        mSide.setTextColor(mColor);
        mExchangeRate.setTextColor(mColor);
        mAmount.setTextColor(mColor);
        mDivider.setBackgroundColor(mColor);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mReceiver = new Receiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(CurrencyService.ACTION_UPDATE);
        registerReceiver(mReceiver, filter);

        startStreaming();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
        stopStreaming();
    }

    public void startTimeProgress() {
        mCircularProgressBar = (CustomCircleProgressBar) findViewById(R.id.circleProgress);
        final int countDownConst = 60;

        new CountDownTimer((countDownConst + 2) * 1000, 1000) {
            int second = countDownConst;
            @Override
            public void onFinish() {
                mCircularProgressBar.setProgress(100);
                mOrdering = false;
                getResult();
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

    private void getResult() {

        String json = new JSONBuilder().setParameter(
                "usersys_id", AccountUtils.getSysId(this),
                "session_id", AccountUtils.getToken(this),
                "investsys_id", mOrderID + ""
        ).build();

        RequestBody body = RequestBody.create(Const.JSON, json);
        String url = new URLBuilder(this).host(R.string.host).path("DUT", "api", "GetResult").toString();
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        mClient.newCall(request).enqueue(new AsyncResponseParser(this, this) {

            @Override
            protected void parseResponse(final JSONObject obj) throws Exception {

                mName.post(new Runnable() {
                    @Override
                    public void run() {
                        if (obj.optInt("session_status") == 2) {
                            Intent intent = new Intent(CountdownActivity.this, LoginActivity.class);
                            startActivity(intent);
                            return;
                        }

                        double startPrice = obj.optInt("start_price");
                        double endPrice = obj.optInt("end_price");
                        String charge = obj.optString("charge");
                        String result = obj.optString("invest_result");
                        int amount = obj.optInt("invest_amount");
                        String completed = obj.optString("isCompleted");

                        mSecond.setText(result.equals("1")? "賺" : "賠");
                    }
                });
            }
        });
    }

    private class Receiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (null == intent) return;
            if (CurrencyService.ACTION_UPDATE.equals(intent.getAction())) {
                try {
                    String name = intent.getStringExtra(SocketClient.ARG_NAME);
                    double price = intent.getDoubleExtra(SocketClient.ARG_PRICE, 0.0);
                    if (name.contains(mName.getText().toString()))
                        mPrice.setPrice(price);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void startStreaming() {
        Intent intent = new Intent(this, CurrencyService.class);
        intent.setAction(CurrencyService.ARG_START_CONNECT);
        startService(intent);
    }

    private void stopStreaming() {
        Intent intent = new Intent(this, CurrencyService.class);
        intent.setAction(CurrencyService.ARG_DISCONNECT);
        startService(intent);
    }
}
