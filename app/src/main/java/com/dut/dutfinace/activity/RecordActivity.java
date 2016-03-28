package com.dut.dutfinace.activity;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.widget.TextView;

import com.dut.dutfinace.R;
import com.dut.dutfinace.activity.ToolbarActivity;
import com.dut.dutfinace.provider.MainProvider;

public class RecordActivity extends ToolbarActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String ARG_INVEST_ID = "invest_id";
    public static final String ARG_CURRENCY_NAME = "currency_name";
    Uri mUri;

    TextView mName;
    TextView mAmount;
    TextView mType;
    TextView mStartTime;
    TextView mStartRate;
    TextView mEndTime;
    TextView mEndRate;
    TextView mResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);
        mUri = MainProvider.getProviderUri(getString(R.string.auth_main_provider), MainProvider.TABLE_HISTORY);

        mName = (TextView) findViewById(R.id.symbol);
        mAmount = (TextView) findViewById(R.id.amount);
        mType = (TextView) findViewById(R.id.type);
        mStartTime = (TextView) findViewById(R.id.startTime);
        mStartRate = (TextView) findViewById(R.id.startRate);
        mEndTime = (TextView) findViewById(R.id.endTime);
        mEndRate = (TextView) findViewById(R.id.endRate);
        mResult = (TextView) findViewById(R.id.result);

        mName.setText(getCurrencyName());
    }

    @Override
    protected void onResume() {
        super.onResume();
        getSupportLoaderManager().initLoader(0, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        CursorLoader cl = new CursorLoader(this);
        cl.setUri(mUri);
        cl.setSelection(MainProvider.FIELD_INVEST_ID + "=?");
        cl.setSelectionArgs(new String[]{getInvestId() + ""});
        return cl;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor != null && cursor.moveToFirst()) {
            mAmount.setText(cursor.getInt(cursor.getColumnIndex(MainProvider.FIELD_INVEST_AMOUNT)) + "");
            mType.setText(cursor.getInt(cursor.getColumnIndex(MainProvider.FIELD_INVEST_TYPE)) == 1 ? "看漲" : "看跌");
            mStartTime.setText(cursor.getString(cursor.getColumnIndex(MainProvider.FIELD_START_TIME)));
            mEndTime.setText(cursor.getString(cursor.getColumnIndex(MainProvider.FIELD_END_TIME)));
            mStartRate.setText(cursor.getDouble(cursor.getColumnIndex(MainProvider.FIELD_START_PRICE)) + "");
            mEndRate.setText(cursor.getDouble(cursor.getColumnIndex(MainProvider.FIELD_END_PRICE)) + "");
            String result = cursor.getString(cursor.getColumnIndex(MainProvider.FIELD_INVEST_RESULT));
            if (result.equals("1")) mResult.setText("獲利");
            if (result.equals("2")) mResult.setText("虧損");
            if (result.equals("3")) mResult.setText("平手(下單金額歸還)");
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    private String getCurrencyName() {
        return getIntent().getStringExtra(ARG_CURRENCY_NAME);
    }

    private int getInvestId() {
        return getIntent().getIntExtra(ARG_INVEST_ID, 0);
    }
}
