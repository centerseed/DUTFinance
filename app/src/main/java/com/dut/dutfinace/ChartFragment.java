package com.dut.dutfinace;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.dut.dutfinace.provider.MainProvider;
import com.github.mikephil.charting.charts.CandleStickChart;

public class ChartFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String ARG_CURRENCY_ID = "currency_id";
    public static final String ARG_CURRENCY_INTERVAL = "currency_interval";

    private CandleStickChart mChart;
    private SeekBar mSeekBarX, mSeekBarY;
    private TextView tvX, tvY;
    Uri mUri;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mUri = MainProvider.getProviderUri(getString(R.string.auth_main_provider), MainProvider.TABLE_CHART);
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        getLoaderManager().initLoader(0, null, this);
    }

    private int getChartID() {
        return getArguments().getInt(ARG_CURRENCY_ID, 0);
    }

    private String getChartInterval() {
        return getArguments().getString(ARG_CURRENCY_INTERVAL);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        CursorLoader cl = new CursorLoader(getActivity());
        cl.setUri(mUri);
        cl.setSelection(MainProvider.FIELD_CURRENCY_ID + "=? AND " + MainProvider.FIELD_CHART_INTERVAL + "=?");
        cl.setSelectionArgs(new String[]{getChartID() + "", getChartInterval()});
        return cl;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
