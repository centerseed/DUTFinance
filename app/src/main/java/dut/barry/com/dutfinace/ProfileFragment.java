package dut.barry.com.dutfinace;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import dut.barry.com.dutfinace.provider.MainProvider;

public class ProfileFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    TextView mFunds;
    TextView mNetLiqs;
    TextView mUsedDeposit;
    TextView mAvailDeposit;
    Uri mUri;

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
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
