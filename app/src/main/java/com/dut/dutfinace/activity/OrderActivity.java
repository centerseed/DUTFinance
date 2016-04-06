package com.dut.dutfinace.activity;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.dut.dutfinace.AccountUtils;
import com.dut.dutfinace.Const;
import com.dut.dutfinace.CustomCircleProgressBar;
import com.dut.dutfinace.JSONBuilder;
import com.dut.dutfinace.PriceTextView;
import com.dut.dutfinace.R;
import com.dut.dutfinace.URLBuilder;
import com.dut.dutfinace.network.AsyncResponseParser;
import com.dut.dutfinace.provider.MainProvider;
import com.dut.dutfinace.streaming.CurrencyService;
import com.dut.dutfinace.streaming.SocketClient;

import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class OrderActivity extends ToolbarActivity {

    public static final String ARG_TARGET_NAME = "target_name";
    public static final String ARG_TARGET_ID = "target_id";
    public static final String ARG_EXCHANGE_RATE = "exchange_rate";
    public static final String ARG_AMOUNT = "amount";
    public static final String ARG_SIDE = "side";
    public static final String ARG_ORDER_ID = "order_id";

    public static final int SIDE_UP = 1;
    public static final int SIDE_DOWN = 2;

    TextView mCurrency;
    PriceTextView mPrice;
    EditText mAmount;
    String mName;
    String mId;
    BroadcastReceiver mReceiver;
    private final OkHttpClient mClient = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activiy_order);
        mCurrency = (TextView) findViewById(R.id.currency);
        mPrice = (PriceTextView) findViewById(R.id.price);
        mPrice.setTextSize(48);
        mAmount = (EditText) findViewById(R.id.amount);

        mName = getIntent().getStringExtra(ARG_TARGET_NAME);
        mId = getIntent().getStringExtra(ARG_TARGET_ID);
        mCurrency.setText(mName);
        mPrice.setPrice(PreferenceManager.getDefaultSharedPreferences(this).getFloat(mName, 0));
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
        PreferenceManager.getDefaultSharedPreferences(this).edit().putFloat(mCurrency.getText().toString(), (float) mPrice.getPrice()).commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    public void onLookUp(View v) {
        showOrderDialog(SIDE_UP);
    }

    public void onLookDown(View v) {
        showOrderDialog(SIDE_DOWN);
    }

    protected void showOrderDialog(final int side) {
        new AlertDialog.Builder(this)
                .setTitle("確認")
                .setMessage("確認是否下注")
                .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            int amount = Integer.valueOf(mAmount.getText().toString());
                            if (amount < 10) {
                                Toast.makeText(OrderActivity.this, "最小單位為10", Toast.LENGTH_LONG).show();
                                return;
                            }

                            int maxAmount = AccountUtils.getMaxOrderFund(OrderActivity.this);
                            if (amount > maxAmount) {
                                Toast.makeText(OrderActivity.this, "超過最大下單金額:" + maxAmount, Toast.LENGTH_LONG).show();
                                return;
                            }

                            makeOrder(amount, side);
                        } catch (Exception e) {
                            Toast.makeText(OrderActivity.this, "無效的金額", Toast.LENGTH_LONG).show();
                            return;
                        }

                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private void makeOrder(int amount, final int side) {

        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddhhmmss");
        String time = formatter.format(new Date(System.currentTimeMillis()));

        String json = new JSONBuilder().setParameter(
                "usersys_id", AccountUtils.getSysId(this),
                "session_id", AccountUtils.getToken(this),
                "currencysys_id", mId,
                "invest_amount", amount + "",
                "invest_time", time,
                "change", side + ""
        ).build();

        RequestBody body = RequestBody.create(Const.JSON, json);
        String url = new URLBuilder(this).host(R.string.host).path("DUT", "api", "Invest").toString();
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        mClient.newCall(request).enqueue(new AsyncResponseParser(this, this) {

            @Override
            protected void parseResponse(final JSONObject obj) throws Exception {

                mCurrency.post(new Runnable() {
                    @Override
                    public void run() {
                        if (obj.optInt("session_status") == 2) {
                            Intent intent = new Intent(OrderActivity.this, LoginActivity.class);
                            startActivity(intent);
                            return;
                        }

                        int result = obj.optInt("invest_code");
                        if (result == 1) {
                            Intent intent = new Intent(OrderActivity.this, CountdownActivity.class);
                            intent.putExtra(ARG_TARGET_NAME, mName);
                            intent.putExtra(ARG_TARGET_ID, mId);
                            intent.putExtra(ARG_EXCHANGE_RATE, obj.optDouble("start_price"));
                            intent.putExtra(ARG_AMOUNT, obj.optInt("invest_amount"));
                            intent.putExtra(ARG_SIDE, side);
                            intent.putExtra(ARG_ORDER_ID, obj.optInt("investsys_id"));
                            startActivity(intent);
                            OrderActivity.this.finish();
                        } else if (result == 2) {
                            Toast.makeText(OrderActivity.this, "下單金額超過最大下單金額", Toast.LENGTH_LONG);
                        } else if (result == 3) {
                            Toast.makeText(OrderActivity.this, "下單金額超過帳戶餘額", Toast.LENGTH_LONG);
                        } else if (result == 4) {
                            Toast.makeText(OrderActivity.this, "帳戶鎖定無法下單", Toast.LENGTH_LONG);
                        } else {
                            Toast.makeText(OrderActivity.this, "系統限制不可下單", Toast.LENGTH_LONG);
                        }
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
                    if (name.contains(mName))
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
