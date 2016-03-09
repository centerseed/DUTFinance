package com.dut.dutfinace;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dut.dutfinace.activity.OrderActivity;
import com.dut.dutfinace.network.AsyncResponseParser;
import com.dut.dutfinace.provider.MainProvider;

import org.json.JSONArray;
import org.json.JSONObject;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class TradeFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    LinearLayout mTarget1;
    LinearLayout mTarget2;
    LinearLayout mTarget3;
    LinearLayout mTarget4;

    TextView mCurrency1;
    TextView mCurrency2;
    TextView mCurrency3;
    TextView mCurrency4;

    Uri mUri;
    private final OkHttpClient mClient = new OkHttpClient();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mUri = MainProvider.getProviderUri(getString(R.string.auth_main_provider), MainProvider.TABLE_CURRENCY);
        return inflater.inflate(R.layout.fragment_trade, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mTarget1 = (LinearLayout) view.findViewById(R.id.target1);
        mTarget2 = (LinearLayout) view.findViewById(R.id.target2);
        mTarget3 = (LinearLayout) view.findViewById(R.id.target3);
        mTarget4 = (LinearLayout) view.findViewById(R.id.target4);

        mCurrency1 = (TextView) view.findViewById(R.id.currency1);
        mCurrency2 = (TextView) view.findViewById(R.id.currency2);
        mCurrency3 = (TextView) view.findViewById(R.id.currency3);
        mCurrency4 = (TextView) view.findViewById(R.id.currency4);
    }

    @Override
    public void onResume() {
        super.onResume();
        getLoaderManager().initLoader(0, null, this);

        String json = new JSONBuilder().setParameter(
                "usersys_id", AccountUtils.getSysId(getContext()),
                "session_id", AccountUtils.getToken(getContext())).build();

        RequestBody body = RequestBody.create(Const.JSON, json);
        String url = new URLBuilder(getContext()).host(R.string.host).path("DUT", "api", "currencyType").toString();
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        mClient.newCall(request).enqueue(new AsyncResponseParser(getContext()) {

            @Override
            protected void parseResponse(final JSONObject obj) throws Exception {
                Uri uri = MainProvider.getProviderUri(m_context.getString(R.string.auth_main_provider), MainProvider.TABLE_CURRENCY);

                int sessionStatus = obj.optInt("session_status");
                if (sessionStatus == 2) return;

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
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        CursorLoader cl = new CursorLoader(getActivity());
        cl.setUri(mUri);
        return cl;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor != null && cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                String name = cursor.getString(cursor.getColumnIndex(MainProvider.FIELD_CURRENCY_NAME));
                if (cursor.getPosition() == 0)  {
                    mCurrency1.setText(name);
                    mCurrency1.setTag(cursor.getString(cursor.getColumnIndex(MainProvider.FIELD_CURRENCY_ID)));
                    mTarget1.setOnClickListener(new clickListener(name));
                }
                if (cursor.getPosition() == 1) {
                    mCurrency2.setText(name);
                    mCurrency2.setTag(cursor.getString(cursor.getColumnIndex(MainProvider.FIELD_CURRENCY_ID)));
                    mTarget2.setOnClickListener(new clickListener(name));
                }
                if (cursor.getPosition() == 2) {
                    mCurrency3.setText(name);
                    mCurrency3.setTag(cursor.getString(cursor.getColumnIndex(MainProvider.FIELD_CURRENCY_ID)));
                    mTarget3.setOnClickListener(new clickListener(name));
                }
                if (cursor.getPosition() == 3) {
                    mCurrency4.setText(name);
                    mCurrency4.setTag(cursor.getString(cursor.getColumnIndex(MainProvider.FIELD_CURRENCY_ID)));
                    mTarget4.setOnClickListener(new clickListener(name));
                }

                cursor.moveToNext();
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    public class clickListener implements View.OnClickListener {
        String  mName;

        public clickListener(String name) {
            mName = name;
        }

        @Override
        public void onClick(View view) {
            Intent intent = new Intent(getActivity(), OrderActivity.class);
            intent.putExtra(OrderActivity.ARG_TARGET_ID, (String)view.getTag());
            intent.putExtra(OrderActivity.ARG_TARGET_NAME, mName);
            getContext().startActivity(intent);
        }
    }
}
