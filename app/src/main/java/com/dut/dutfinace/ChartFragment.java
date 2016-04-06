package com.dut.dutfinace;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Paint;
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

import com.dut.dutfinace.activity.LoginActivity;
import com.dut.dutfinace.dummy.DummyData;
import com.dut.dutfinace.network.AsyncResponseParser;
import com.dut.dutfinace.provider.MainProvider;
import com.github.mikephil.charting.charts.CandleStickChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.CandleData;
import com.github.mikephil.charting.data.CandleDataSet;
import com.github.mikephil.charting.data.CandleEntry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class ChartFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, AsyncResponseParser.NetError {

    public static final String ARG_CURRENCY_NAME = "currency_name";
    public static final String ARG_CURRENCY_ID = "currency_id";
    public static final String ARG_CURRENCY_INTERVAL = "currency_interval";

    private CandleStickChart mChart;
    YAxis mYAxis;
    XAxis mXAxis;

    private SeekBar mSeekBarX, mSeekBarY;
    private TextView tvX, tvY;
    private TextView mName;
    Uri mUri;

    String mBarType = "1";
    private final OkHttpClient mClient = new OkHttpClient();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mUri = MainProvider.getProviderUri(getString(R.string.auth_main_provider), MainProvider.TABLE_CHART);
        return inflater.inflate(R.layout.fragment_chart, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mName = (TextView) view.findViewById(R.id.name);
        mName.setText(getCurrencyName());
        mChart = (CandleStickChart) view.findViewById(R.id.chart);
        mChart.setNoDataText("載入中...");
    }

    @Override
    public void onResume() {
        super.onResume();
        getLoaderManager().initLoader(0, null, this);

        getChartData();

        // Dummy
        // draw(DummyData.getDummyBarData());
    }

    private int getChartID() {
        return getArguments().getInt(ARG_CURRENCY_ID, 0);
    }

    private String getChartInterval() {
        return getArguments().getString(ARG_CURRENCY_INTERVAL);
    }

    private String getCurrencyName() {
        return getArguments().getString(ARG_CURRENCY_NAME);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        CursorLoader cl = new CursorLoader(getActivity());
        cl.setUri(mUri);
        cl.setSelection(MainProvider.FIELD_ID + "=? AND " + MainProvider.FIELD_CHART_INTERVAL + "=?");
        cl.setSelectionArgs(new String[]{(getCurrencyName() + mBarType).hashCode() + "", mBarType});
        return cl;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null && data.moveToFirst()) {
            draw(data.getString(data.getColumnIndex(MainProvider.FIELD_CHART_DATA)));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    private void draw(String data) {
        setXAxis();
        setYAxis();
        mChart.getLegend().setEnabled(false);

        setData(data);
        mChart.invalidate();
        mChart.postDelayed(new Runnable() {
            @Override
            public void run() {
                mChart.setAutoScaleMinMaxEnabled(true);
            }
        }, 500);
    }

    private void setXAxis() {
        mXAxis = mChart.getXAxis();
        mXAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        mXAxis.setSpaceBetweenLabels(2);
        mXAxis.setDrawGridLines(false);
        mXAxis.setTextColor(getResources().getColor(R.color.colorWhite));
    }

    private void setYAxis() {
        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.setEnabled(false);

        mYAxis = mChart.getAxisRight();
        mYAxis.setEnabled(true);
        mYAxis.setTextColor(getResources().getColor(R.color.colorWhite));
    }

    private void setData(String data) {
        try {
            JSONArray array = new JSONArray(data);
            if (array.length() == 0) return;

            ArrayList<CandleEntry> yVals = new ArrayList<CandleEntry>();
            ArrayList<String> xVals = new ArrayList<String>();

            float max = 0;
            float min = Float.MAX_VALUE;

            for (int i = 0; i < array.length(); i++) {
                String raw = array.getJSONObject(i).getString("BarDataDetail");
                String details[] = raw.split(",");
                float high = Float.valueOf(details[2]);
                float low = Float.valueOf(details[3]);
                float open = Float.valueOf(details[1]);
                float close = Float.valueOf(details[4]);

                if (max < high) max = high;
                if (min > low) min = low;

                yVals.add(new CandleEntry(i, high, low, open, close));
                xVals.add(details[0].substring(0, details[0].length() - 2));
            }

            CandleDataSet set1 = new CandleDataSet(yVals, "Data Set");
            set1.setAxisDependency(YAxis.AxisDependency.RIGHT);
//        set1.setColor(Color.rgb(80, 80, 80));
            set1.setShadowColor(Color.LTGRAY);
            set1.setShadowWidth(0.7f);
            set1.setDecreasingColor(Color.RED);
            set1.setDecreasingPaintStyle(Paint.Style.FILL);
            set1.setIncreasingColor(Color.rgb(122, 242, 84));
            set1.setIncreasingPaintStyle(Paint.Style.FILL);
            set1.setNeutralColor(Color.BLUE);
            set1.setValueTextSize(0);
            //set1.setHighlightLineWidth(1f);

            CandleData candleData = new CandleData(xVals, set1);
            mYAxis.setSpaceTop(10);
            mYAxis.setSpaceBottom(10);
            mChart.setData(candleData);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }

    private void getChartData() {
        String json = new JSONBuilder().setParameter(
                "usersys_id", AccountUtils.getSysId(getContext()),
                "session_id", AccountUtils.getToken(getContext()),
                "currencyName", getCurrencyName(),
                "barDataType", mBarType).build();

        RequestBody body = RequestBody.create(Const.JSON, json);
        String url = new URLBuilder(getContext()).host(R.string.host).path("DUT", "api", "BarData").toString();

      //  String url = new URLBuilder(getContext()).host("1.34.243.17").path("DUT", "api", "BarData").toString();
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        mClient.newCall(request).enqueue(new AsyncResponseParser(getContext(), this) {

            @Override
            protected void parseResponse(final JSONObject obj) throws Exception {

                JSONArray array = obj.getJSONArray("BarData");
                if (array == null) return;

                ContentValues values = new ContentValues();
                values.put(MainProvider.FIELD_ID, (getCurrencyName() + mBarType).hashCode());
                values.put(MainProvider.FIELD_CURRENCY_NAME, getCurrencyName());
                values.put(MainProvider.FIELD_CHART_INTERVAL, mBarType);
                values.put(MainProvider.FIELD_CHART_DATA, array.toString());

                m_context.getContentResolver().insert(mUri, values);
                m_context.getContentResolver().notifyChange(mUri, null);
            }
        });
    }

    @Override
    public void onNetError(IOException e) {

    }

    @Override
    public void onResponseError(int error) {

    }
}
