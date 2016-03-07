package com.dut.dutfinace;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Owner on 2016/3/7.
 */
public class JSONBuilder {
    private JSONObject mObject;

    public JSONBuilder() {
        mObject = new JSONObject();
    }

    public JSONBuilder setParameter(String... params) {
        for (int i = 0; i < params.length; i += 2)
            if (params[i] != null && params[i + 1] != null)
                try {
                    mObject.put(params[i], params[i + 1]);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
        return this;
    }

    public String build() {
        return mObject.toString();
    }
}
