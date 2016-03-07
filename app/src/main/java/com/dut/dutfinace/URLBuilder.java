package com.dut.dutfinace;

import android.content.Context;
import android.net.Uri;

import java.net.MalformedURLException;
import java.net.URL;

public class URLBuilder {

    private Context m_context;
    private Uri.Builder m_builder;

    public URLBuilder(Context context) {
        m_context = context;
    }

    public URLBuilder host(int hostStringResourceId) {
        m_builder = Uri.parse(m_context.getString(hostStringResourceId)).buildUpon();
        return this;
    }

    public URLBuilder path(String... paths) {
        for (String p : paths)
            if (p != null)
                m_builder.appendPath(p);
        return this;
    }

    public URLBuilder query(String... querys) {
        for (int i = 0; i < querys.length; i += 2)
            if (querys[i] != null && querys[i + 1] != null)
                m_builder.appendQueryParameter(querys[i], querys[i + 1]);
        return this;
    }

    public URL build() {
        try {
            return new URL(m_builder.toString());
        } catch (MalformedURLException e) {
            return null;
        }
    }

    @Override
    public String toString() {
        return m_builder.build().toString();
    }
}
