package com.dut.dutfinace.activity;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.dut.dutfinace.R;

public abstract class ToolbarActivity extends AppCompatActivity {

    private static final String TAG = "ToolbarActivity";

    protected Toolbar m_toolbar;

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        m_toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(m_toolbar);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setHomeButtonEnabled(true);
        }
    }

    public void setToolbarTitle(final int rid) {
        ActionBar ab = getSupportActionBar();
        if (ab != null) ab.setTitle(rid);
    }

    public void setToolbarTitle(final String t) {
        ActionBar ab = getSupportActionBar();
        if (ab != null) ab.setTitle(t);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
