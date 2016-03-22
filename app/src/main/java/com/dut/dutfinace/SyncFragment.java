package com.dut.dutfinace;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.dut.dutfinace.network.AsyncResponseParser;

/**
 * Created by Mac on 16/3/12.
 */
public abstract class SyncFragment extends Fragment implements AsyncResponseParser.NetError {

    protected MenuItem mRefreshItem = null;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setHasOptionsMenu(true);
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_sync, menu);

        setRefreshItem(menu.findItem(R.id.action_sync));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.action_sync){
            onSync();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    abstract void onSync();

    protected void setRefreshItem(MenuItem item) {
        mRefreshItem = item;
    }

    protected void stopRefresh() {
        if (mRefreshItem != null) {
            mRefreshItem.setActionView(null);
        }
    }

    protected void runRefresh() {
        if (mRefreshItem != null) {
            mRefreshItem.setActionView(R.layout.refresh_item);
        }
    }

    @Override
    public void onNetError() {
        Toast.makeText(getContext(), "網路或server錯誤", Toast.LENGTH_SHORT).show();
    }

}
