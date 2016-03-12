package com.dut.dutfinace;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dut.dutfinace.adapter.HistoryAdapter;
import com.dut.dutfinace.network.AsyncResponseParser;
import com.dut.dutfinace.provider.MainProvider;

import org.json.JSONArray;
import org.json.JSONObject;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class HistoryFragment extends SyncFragment implements LoaderManager.LoaderCallbacks<Cursor>  {

    Uri mUri;
    HistoryAdapter mAdapter;
    protected RecyclerView mRecyclerView;
    private final OkHttpClient mClient = new OkHttpClient();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mUri = MainProvider.getProviderUri(getString(R.string.auth_main_provider), MainProvider.TABLE_HISTORY);
        mAdapter = new HistoryAdapter(getContext(), null);
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
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler);
        if (mRecyclerView != null) {
            mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
            mRecyclerView.setAdapter(mAdapter);
        }
    }

    @Override
    void onSync() {
        runRefresh();
        String json = new JSONBuilder().setParameter(
                "usersys_id", AccountUtils.getSysId(getContext()),
                "session_id", AccountUtils.getToken(getContext()),
                "history_type", 1 + "").build();

        RequestBody body = RequestBody.create(Const.JSON, json);
        String url = new URLBuilder(getContext()).host(R.string.host).path("DUT", "api", "History").toString();
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        mClient.newCall(request).enqueue(new AsyncResponseParser(getContext()) {

            @Override
            protected void parseResponse(final JSONObject obj) throws Exception {

                int sessionStatus = obj.optInt("session_status");
                if (sessionStatus == 2) return;

                JSONArray array = obj.getJSONArray("HistoryList");

                for (int i = 0; i < array.length(); i++) {
                    JSONObject object = array.getJSONObject(i);
                    ContentValues values = new ContentValues();
                    values.put(MainProvider.FIELD_ID, object.optInt("currencysys_id") + object.optInt("invest_id"));
                    values.put(MainProvider.FIELD_INVEST_ID,  object.optInt("invest_id"));
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
        if (mAdapter != null && cursor.moveToFirst()) {
            mAdapter.swapCursor(cursor);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (mAdapter != null) mAdapter.swapCursor(null);
    }
}
