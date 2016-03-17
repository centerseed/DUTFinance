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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.dut.dutfinace.activity.LoginActivity;
import com.dut.dutfinace.activity.MainActivity;
import com.dut.dutfinace.network.AsyncResponseParser;
import com.dut.dutfinace.provider.MainProvider;

import org.json.JSONObject;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;


public class ProfileFragment extends SyncFragment implements LoaderManager.LoaderCallbacks<Cursor>{

    TextView mFunds;
    TextView mNetLiqs;
    TextView mUsedDeposit;
    TextView mAvailDeposit;
    Uri mUri;
    private final OkHttpClient mClient = new OkHttpClient();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mUri = MainProvider.getProviderUri(getString(R.string.auth_main_provider), MainProvider.TABLE_PROFILE);
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mFunds = (TextView) view.findViewById(R.id.avail_funds);
        mNetLiqs = (TextView) view.findViewById(R.id.net_liq);
        mUsedDeposit = (TextView) view.findViewById(R.id.used_deposit);
        mAvailDeposit = (TextView) view.findViewById(R.id.avail_deposit);
    }

    @Override
    void onSync() {
        runRefresh();
        String json = new JSONBuilder().setParameter(
                "usersys_id", AccountUtils.getSysId(getContext()),
                "session_id", AccountUtils.getToken(getContext())).build();

        RequestBody body = RequestBody.create(Const.JSON, json);
        String url = new URLBuilder(getContext()).host(R.string.host).path("DUT", "api", "UserInfo").toString();
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        mClient.newCall(request).enqueue(new AsyncResponseParser(getContext()) {

            @Override
            protected void parseResponse(final JSONObject obj) throws Exception {

                AccountUtils.setMaxOrderFund(getContext(), obj.optInt("bet_max"));
                m_context.getContentResolver().delete(mUri, MainProvider.FIELD_ID + ">=?", new String[]{"0"});

                ContentValues values = new ContentValues();
                values.put(MainProvider.FIELD_ID, obj.optString("user_id").hashCode());
                values.put(MainProvider.FIELD_AVAILABLE_FUND, obj.optString("total"));

                m_context.getContentResolver().insert(mUri, values);
                m_context.getContentResolver().notifyChange(mUri, null);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        getLoaderManager().initLoader(0, null, this);

        onSync();
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
            String total = cursor.getString(cursor.getColumnIndex(MainProvider.FIELD_AVAILABLE_FUND));
            mFunds.setText(total);
            stopRefresh();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
