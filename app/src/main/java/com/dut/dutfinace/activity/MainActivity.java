package com.dut.dutfinace.activity;

import android.content.ContentValues;
import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
   
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
  
import android.widget.TextView;

import com.dut.dutfinace.AccountUtils;
import com.dut.dutfinace.Const;
import com.dut.dutfinace.HistoryFragment;
import com.dut.dutfinace.JSONBuilder;
import com.dut.dutfinace.ProfileFragment;
import com.dut.dutfinace.R;
import com.dut.dutfinace.TradeFragment;
import com.dut.dutfinace.URLBuilder;
import com.dut.dutfinace.network.AsyncResponseParser;
import com.dut.dutfinace.provider.MainProvider;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class MainActivity extends NetStatusActivity {

    private SectionsPagerAdapter mSectionsPagerAdapter;

    private ViewPager mViewPager;
    private final OkHttpClient mClient = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        // customTabs(tabLayout);
        tabLayout.setupWithViewPager(mViewPager);
        tabLayout.getTabAt(2).setIcon(R.mipmap.ic_history);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_logout) {
            logout();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void logout() {
        String json = new JSONBuilder().setParameter(
                "usersys_id", AccountUtils.getSysId(this),
                "session_id", AccountUtils.getToken(this)).build();

        RequestBody body = RequestBody.create(Const.JSON, json);
        String url = new URLBuilder(this).host(R.string.host).path("DUT", "api", "Logout").toString();
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        mClient.newCall(request).enqueue(new AsyncResponseParser(this, this) {

            @Override
            protected void parseResponse(final JSONObject obj) throws Exception {
                if (obj.optInt("session_status") == 2 || obj.optInt("isLogout") == 2) {
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new ProfileFragment();
                case 1:
                    return new TradeFragment();
                case 2:
                    return new HistoryFragment();
            }
            return null;
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.title_account);
                case 1:
                    return getString(R.string.title_trade);
                case 2:
                    return getString(R.string.title_history);
            }
            return null;
        }
    }

    private void customTabs(TabLayout tabLayout) {
        for (int i = 0; i < 3; i++) {
          //  TextView tab = (TextView) LayoutInflater.from(this).inflate(R.layout.custom_tab, null);
          //  tabLayout.getTabAt(0).setCustomView(tab);
        }
    }
}
