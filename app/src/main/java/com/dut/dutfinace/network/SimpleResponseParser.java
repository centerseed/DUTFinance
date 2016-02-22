package com.dut.dutfinace.network;


import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

public class SimpleResponseParser extends BaseResponseParser {
    public SimpleResponseParser(Context c) {
        super(c);
    }

    @Override
    protected void parse(String s) {
        try {
            m_jsonObj = new JSONObject(s);
            m_String = s;
        } catch (JSONException e) {
            e.printStackTrace();
            m_jsonObj = new JSONObject(); // Dummy
        }
    }
}
