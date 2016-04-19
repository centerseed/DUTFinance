package com.dut.dutfinace;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.dut.dutfinace.activity.RecordActivity;
import com.dut.dutfinace.adapter.HistoryAdapter;
import com.dut.dutfinace.network.AsyncResponseParser;
import com.dut.dutfinace.provider.MainProvider;

import org.json.JSONArray;
import org.json.JSONObject;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class HistoryFragment extends SyncFragment implements LoaderManager.LoaderCallbacks<Cursor>, HistoryAdapter.OnClickListener {

    Uri mUri;
    HistoryAdapter mAdapter;
    TextView mProfitTitle;
    TextView mProfit;
    protected RecyclerView mRecyclerView;
    private final OkHttpClient mClient = new OkHttpClient();

    int mInterval = 1;
    RadioGroup mRadioGroup;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mUri = MainProvider.getProviderUri(getString(R.string.auth_main_provider), MainProvider.TABLE_HISTORY);
        mAdapter = new HistoryAdapter(getContext(), null);
        mAdapter.setOnClickListener(this);
        return inflater.inflate(R.layout.fragment_history, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        getLoaderManager().initLoader(0, null, this);

        onSync();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mProfitTitle = (TextView) view.findViewById(R.id.profitTitle);
        mProfit = (TextView) view.findViewById(R.id.profit);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler);
        if (mRecyclerView != null) {
            mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
            mRecyclerView.setAdapter(mAdapter);
        }

        mRadioGroup = (RadioGroup) view.findViewById(R.id.radioGroup);
        mRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.day) {
                    mInterval = 1;
                    mProfitTitle.setText("本日收益：");
                }
                if (checkedId == R.id.week) {
                    mInterval = 2;
                    mProfitTitle.setText("本週收益：");
                }
                if (checkedId == R.id.month) {
                    mInterval = 3;
                    mProfitTitle.setText("本月收益：");
                }
                onSync();
            }
        });
        mProfitTitle.setText("本日收益：");
    }

    @Override
    void onSync() {
        runRefresh();
        mAdapter.swapCursor(null);
        String json = new JSONBuilder().setParameter(
                "usersys_id", AccountUtils.getSysId(getContext()),
                "session_id", AccountUtils.getToken(getContext()),
                "history_type", mInterval + "").build();

        RequestBody body = RequestBody.create(Const.JSON, json);
        String url = new URLBuilder(getContext()).host(R.string.host).path("DUT", "api", "History").toString();
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        mClient.newCall(request).enqueue(new AsyncResponseParser(getContext(), this) {

            @Override
            protected void parseResponse(final JSONObject obj) throws Exception {

                int sessionStatus = obj.optInt("session_status");
                if (sessionStatus == 2) return;
                m_context.getContentResolver().delete(mUri, MainProvider.FIELD_ID + ">=?", new String[]{"0"});

                JSONArray array = obj.getJSONArray("HistoryList");

                for (int i = 0; i < array.length(); i++) {
                    JSONObject object = array.getJSONObject(i);
                    ContentValues values = new ContentValues();
                    values.put(MainProvider.FIELD_ID, object.optInt("currencysys_id") + object.optInt("invest_id"));
                    values.put(MainProvider.FIELD_INVEST_ID, object.optInt("invest_id"));
                    values.put(MainProvider.FIELD_CURRENCY_ID, object.optInt("currencysys_id"));
                    values.put(MainProvider.FIELD_INVEST_AMOUNT, object.optInt("invest_amount"));
                    values.put(MainProvider.FIELD_INVEST_TYPE, object.optString("change"));
                    values.put(MainProvider.FIELD_START_TIME, object.optString("start_time"));
                    values.put(MainProvider.FIELD_START_PRICE, object.optDouble("start_price"));
                    values.put(MainProvider.FIELD_END_TIME, object.optString("end_time"));
                    values.put(MainProvider.FIELD_END_PRICE, object.optDouble("end_price"));
                    values.put(MainProvider.FIELD_INVEST_RESULT, object.optString("invest_result"));

                    m_context.getContentResolver().insert(mUri, values);
                }

                m_context.getContentResolver().notifyChange(mUri, null);
            }
        });
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        CursorLoader cl = new CursorLoader(getActivity());
        cl.setUri(mUri);
        return cl;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        stopRefresh();
        mProfit.setText("0");
        mProfit.setTextColor(getContext().getResources().getColor(R.color.colorPrimaryDark));

        if (mAdapter != null && cursor != null && cursor.moveToFirst()) {
            mAdapter.swapCursor(cursor);

            double profit = 0;
            while (!cursor.isAfterLast()) {
                String result = cursor.getString(cursor.getColumnIndex(MainProvider.FIELD_INVEST_RESULT));
                double amount = cursor.getInt(cursor.getColumnIndex(MainProvider.FIELD_INVEST_AMOUNT));
                if (result.equals("1")) amount = amount * 0.75;
                if (result.equals("2")) amount = amount * -1;
                if (result.equals("3")) amount = 0;
                profit += amount;
                cursor.moveToNext();
            }

            if (profit > 0) mProfit.setTextColor(getContext().getResources().getColor(R.color.colorUp));
            else if (profit < 0) mProfit.setTextColor(getContext().getResources().getColor(R.color.colorDown));
            else mProfit.setTextColor(getContext().getResources().getColor(R.color.colorPrimaryDark));
            mProfit.setText(profit + "");
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (mAdapter != null) mAdapter.swapCursor(null);
    }

    @Override
    public void onClick(int investID, String name) {
        Intent intent = new Intent(getActivity(), RecordActivity.class);
        intent.putExtra(RecordActivity.ARG_INVEST_ID, investID);
        intent.putExtra(RecordActivity.ARG_CURRENCY_NAME, name);
        startActivity(intent);
    }
}
