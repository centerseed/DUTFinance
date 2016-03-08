package com.dut.dutfinace.parser;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.dut.dutfinace.R;
import com.dut.dutfinace.network.BaseResponseParser;
import com.dut.dutfinace.provider.MainProvider;

import org.json.JSONException;
import org.json.JSONObject;

public class ProfileParser extends BaseResponseParser {
    public ProfileParser(Context c) {
        super(c);
    }

    @Override
    protected void parse(JSONObject obj) throws JSONException {
        Log.d("ProfileParser", obj.toString());

        Uri uri = MainProvider.getProviderUri(m_context.getString(R.string.auth_main_provider), MainProvider.TABLE_PROFILE);

        ContentValues values = new ContentValues();
        values.put(MainProvider.FIELD_ID, obj.optString("user_id").hashCode());
        values.put(MainProvider.FIELD_EXPIRE, obj.optInt("session_status"));
        values.put(MainProvider.FIELD_AVAILABLE_FUND, obj.optString("total"));

        m_context.getContentResolver().insert(uri, values);
        m_context.getContentResolver().notifyChange(uri, null);
    }
}
