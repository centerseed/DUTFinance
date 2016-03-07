package com.dut.dutfinace.network;

import android.content.Context;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

abstract public class AsyncResponseParser implements Callback {

    protected Context m_context;
    protected String m_token;
    protected PostProcess m_postProcess;

    public AsyncResponseParser(Context c, String token) {
        m_context = c;
        m_token = token;
    }

    @Override
    public void onFailure(Call call, IOException e) {

    }

    @Override
    public void onResponse(Call call, Response response) throws IOException {
        if (null == response) return;

        if (response.code() == 401) {
            onAuthFail();
        }

        if (response.code() == 200) {
            try {
                JSONObject object = new JSONObject(response.body().string());
                if (null != object) parseResponse(object);
                if (null != m_postProcess) m_postProcess.onPostProcess();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void onAuthFail() {
    }

    public void setPostProcess(PostProcess process) {
        m_postProcess = process;
    }

    protected abstract void parseResponse(JSONObject jsonObject) throws Exception;

    public interface PostProcess {
        void onPostProcess();
    }
}
