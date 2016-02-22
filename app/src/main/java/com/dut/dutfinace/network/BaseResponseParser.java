package com.dut.dutfinace.network;

import android.accounts.AccountManager;
import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Response;


public abstract class BaseResponseParser {
    public static final String TAG = "BaseResponseParser";

    protected Context m_context;
    protected JSONObject m_jsonObj;
    protected String m_String;

    public BaseResponseParser(Context c) {
        m_context = c;

    }

    public BaseResponseParser parse(Response response) throws AuthFailException {
        parseNetStatus(response);
        return this;
    }

    public JSONObject getJSONObject() {
        return m_jsonObj;
    }

    public String getString() {
        return m_String;
    }

    private void parseNetStatus(Response response) throws AuthFailException {
        if (response.code() == 200) {
            try {
                String body = response.body().string();
                if (body != null) {
                    parse(body);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else if (response.code() == 401) {
            Log.e(TAG, "Auth fail");

            throw new AuthFailException();
        } else {
            Log.e(TAG, "HTTP ERROR: " + response.message());
        }
    }

    protected abstract void parse(String s) throws JSONException;

    public class AuthFailException extends Exception {
    }
}
