package com.dut.dutfinace.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.dut.dutfinace.AccountUtils;
import com.dut.dutfinace.CustomCircleProgressBar;
import com.dut.dutfinace.R;

public class OrderActivity extends ToolbarActivity {

    public static final String ARG_TARGET_NAME = "target_name";
    public static final String ARG_TARGET_ID = "target_id";
    public static final String ARG_SIDE = "side";

    public static final int SIDE_UP = 0;
    public static final int SIDE_DOWN = 0;

    TextView mCurrency;
    EditText mAmount;
    String mName;
    String mId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activiy_order);
        mCurrency = (TextView) findViewById(R.id.currency);
        mAmount = (EditText) findViewById(R.id.amount);

        mName = getIntent().getStringExtra(ARG_TARGET_NAME);
        mId = getIntent().getStringExtra(ARG_TARGET_ID);
        mCurrency.setText(mName);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onNetError() {

    }

    public void onLookUp(View v) {
        showOrderDialog(0);
    }

    public void onLookDown(View v) {
        showOrderDialog(1);
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

    private void makeOrder(int amount, int side) {
        Intent intent = new Intent(OrderActivity.this, CountdownActivity.class);
        intent.putExtra(ARG_TARGET_NAME, mName);
        intent.putExtra(ARG_TARGET_ID, mId);
        intent.putExtra(ARG_SIDE, side);
        startActivity(intent);
        OrderActivity.this.finish();
    }
}
