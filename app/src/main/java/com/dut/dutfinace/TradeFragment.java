package com.dut.dutfinace;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dut.dutfinace.activity.LoginActivity;
import com.dut.dutfinace.activity.OrderActivity;
import com.dut.dutfinace.network.AsyncResponseParser;
import com.dut.dutfinace.provider.MainProvider;
import com.dut.dutfinace.streaming.CurrencyService;
import com.dut.dutfinace.streaming.SocketClient;

import org.json.JSONArray;
import org.json.JSONObject;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class TradeFragment extends SyncFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    ImageView mTarget1;
    ImageView mTarget2;
    ImageView mTarget3;
    ImageView mTarget4;

    TextView mCurrency1;
    TextView mCurrency2;
    TextView mCurrency3;
    TextView mCurrency4;

    PriceTextView mPriceEU;
    PriceTextView mPriceGJ;
    PriceTextView mPriceGU;
    PriceTextView mPriceXU;

    Uri mUri;
    private final OkHttpClient mClient = new OkHttpClient();
    BroadcastReceiver mReceiver;
    boolean mFirstLoad = true;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mUri = MainProvider.getProviderUri(getString(R.string.auth_main_provider), MainProvider.TABLE_CURRENCY);
        return inflater.inflate(R.layout.fragment_trade, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mTarget1 = (ImageView) view.findViewById(R.id.target1);
        mTarget2 = (ImageView) view.findViewById(R.id.target2);
        mTarget3 = (ImageView) view.findViewById(R.id.target3);
        mTarget4 = (ImageView) view.findViewById(R.id.target4);

        mCurrency1 = (TextView) view.findViewById(R.id.currency1);
        mCurrency2 = (TextView) view.findViewById(R.id.currency2);
        mCurrency3 = (TextView) view.findViewById(R.id.currency3);
        mCurrency4 = (TextView) view.findViewById(R.id.currency4);

        mPriceEU = (PriceTextView) view.findViewById(R.id.priceEU);
        mPriceGJ = (PriceTextView) view.findViewById(R.id.priceGJ);
        mPriceGU = (PriceTextView) view.findViewById(R.id.priceGU);
        mPriceXU = (PriceTextView) view.findViewById(R.id.priceXU);
    }

    @Override
    void onSync() {
        runRefresh();
        String json = new JSONBuilder().setParameter(
                "usersys_id", AccountUtils.getSysId(getContext()),
                "session_id", AccountUtils.getToken(getContext())).build();

        RequestBody body = RequestBody.create(Const.JSON, json);
        String url = new URLBuilder(getContext()).host(R.string.host).path("DUT", "api", "currencyType").toString();
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        mClient.newCall(request).enqueue(new AsyncResponseParser(getContext(), this) {

            @Override
            protected void parseResponse(final JSONObject obj) throws Exception {
                Uri uri = MainProvider.getProviderUri(m_context.getString(R.string.auth_main_provider), MainProvider.TABLE_CURRENCY);

                int sessionStatus = obj.optInt("session_status");
                if (obj.optInt("session_status") == 2) {
                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                    startActivity(intent);
                    getActivity().finish();
                    return;
                }

                JSONArray array = obj.getJSONArray("currencyInfoList");
                m_context.getContentResolver().delete(uri, MainProvider.FIELD_ID + ">=?", new String[]{"0"});

                for (int i = 0; i < array.length(); i++) {
                    ContentValues values = new ContentValues();
                    values.put(MainProvider.FIELD_ID, array.getJSONObject(i).optInt("currencysys_id"));
                    values.put(MainProvider.FIELD_CURRENCY_ID, array.getJSONObject(i).optInt("currencysys_id"));
                    values.put(MainProvider.FIELD_CURRENCY_NAME, array.getJSONObject(i).optString("currencyName"));

                    m_context.getContentResolver().insert(uri, values);
                }

                m_context.getContentResolver().notifyChange(uri, null);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        mReceiver = new Receiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(CurrencyService.ACTION_UPDATE);
        getActivity().registerReceiver(mReceiver, filter);

        getLoaderManager().initLoader(0, null, this);
        onSync();
        startStreaming();
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(mReceiver);
        stopStreaming();
        savePrice();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        CursorLoader cl = new CursorLoader(getActivity());
        cl.setUri(mUri);
        return cl;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor != null && cursor.moveToFirst()) {
            stopRefresh();
            while (!cursor.isAfterLast()) {
                String name = cursor.getString(cursor.getColumnIndex(MainProvider.FIELD_CURRENCY_NAME));
                if (cursor.getPosition() == 0) {
                    mCurrency1.setText(name);
                    mCurrency1.setOnClickListener(new selectListener(name));
                    mTarget1.setTag(cursor.getString(cursor.getColumnIndex(MainProvider.FIELD_CURRENCY_ID)));
                    mTarget1.setOnClickListener(new clickListener(name));
                    mPriceEU.setPrice(PreferenceManager.getDefaultSharedPreferences(getContext()).getFloat(name, 0));

                    if (mFirstLoad) {
                        mFirstLoad = false;
                        showChart(name);
                    }
                }
                if (cursor.getPosition() == 1) {
                    mCurrency2.setText(name);
                    mCurrency2.setOnClickListener(new selectListener(name));
                    mTarget2.setTag(cursor.getString(cursor.getColumnIndex(MainProvider.FIELD_CURRENCY_ID)));
                    mTarget2.setOnClickListener(new clickListener(name));
                    mPriceGJ.setPrice(PreferenceManager.getDefaultSharedPreferences(getContext()).getFloat(name, 0));
                }
                if (cursor.getPosition() == 2) {
                    mCurrency3.setText(name);
                    mCurrency3.setOnClickListener(new selectListener(name));
                    mTarget3.setTag(cursor.getString(cursor.getColumnIndex(MainProvider.FIELD_CURRENCY_ID)));
                    mTarget3.setOnClickListener(new clickListener(name));
                    mPriceGU.setPrice(PreferenceManager.getDefaultSharedPreferences(getContext()).getFloat(name, 0));
                }
                if (cursor.getPosition() == 3) {
                    mCurrency4.setText(name);
                    mCurrency4.setOnClickListener(new selectListener(name));
                    mTarget4.setTag(cursor.getString(cursor.getColumnIndex(MainProvider.FIELD_CURRENCY_ID)));
                    mTarget4.setOnClickListener(new clickListener(name));
                    mPriceXU.setPrice(PreferenceManager.getDefaultSharedPreferences(getContext()).getFloat(name, 0));
                }

                cursor.moveToNext();
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public void onNetError() {

    }

    public class clickListener implements View.OnClickListener {
        String mName;

        public clickListener(String name) {
            mName = name;
        }

        @Override
        public void onClick(View view) {
            Intent intent = new Intent(getActivity(), OrderActivity.class);
            intent.putExtra(OrderActivity.ARG_TARGET_ID, (String) view.getTag());
            intent.putExtra(OrderActivity.ARG_TARGET_NAME, mName);
            getContext().startActivity(intent);
        }
    }

    public class selectListener implements View.OnClickListener {
        String mName;

        public selectListener(String name) {
            mName = name;
        }

        @Override
        public void onClick(View view) {
            showChart(mName);
        }
    }

    private void startStreaming() {
        Intent intent = new Intent(getActivity(), CurrencyService.class);
        intent.setAction(CurrencyService.ARG_START_CONNECT);
        getActivity().startService(intent);
    }

    private void stopStreaming() {
        Intent intent = new Intent(getActivity(), CurrencyService.class);
        intent.setAction(CurrencyService.ARG_DISCONNECT);
        getActivity().startService(intent);
    }

    private class Receiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (null == intent) return;
            if (CurrencyService.ACTION_UPDATE.equals(intent.getAction())) {
                try {
                    String name = intent.getStringExtra(SocketClient.ARG_NAME);
                    double price = intent.getDoubleExtra(SocketClient.ARG_PRICE, 0.0);

                    if (name.contains(mCurrency1.getText())) mPriceEU.setPrice(price);
                    if (name.contains(mCurrency2.getText())) mPriceGJ.setPrice(price);
                    if (name.contains(mCurrency3.getText())) mPriceGU.setPrice(price);
                    if (name.contains(mCurrency4.getText())) mPriceXU.setPrice(price);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void showChart(String name) {
        Fragment chartFragment = new ChartFragment();
        Bundle bundle = new Bundle();
        bundle.putString(ChartFragment.ARG_CURRENCY_NAME, name);

        chartFragment.setArguments(bundle);
        getChildFragmentManager().beginTransaction().replace(R.id.container, chartFragment, null).commit();
    }

    private void savePrice() {

        PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putFloat(mCurrency1.getText().toString(), (float) mPriceEU.getPrice()).commit();
        PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putFloat(mCurrency2.getText().toString(), (float) mPriceGJ.getPrice()).commit();
        PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putFloat(mCurrency3.getText().toString(), (float) mPriceGU.getPrice()).commit();
        PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putFloat(mCurrency4.getText().toString(), (float) mPriceXU.getPrice()).commit();
    }
}
