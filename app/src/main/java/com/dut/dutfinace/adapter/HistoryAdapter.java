package com.dut.dutfinace.adapter;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dut.dutfinace.R;
import com.dut.dutfinace.provider.MainProvider;

/**
 * Created by Mac on 16/3/12.
 */
public class HistoryAdapter extends AbstractRecyclerCursorAdapter {
    Uri mCurrencyUri;
    OnClickListener mListener;

    public interface OnClickListener {
        void onClick(int investID, String name);
    }

    public HistoryAdapter(Context context, Cursor c) {
        super(context, c);
        mCurrencyUri = MainProvider.getProviderUri(context.getString(R.string.auth_main_provider), MainProvider.TABLE_CURRENCY);
    }

    public void setOnClickListener(OnClickListener listener) {
        mListener = listener;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, Cursor cursor) {
        HistoryViewHolder vh = (HistoryViewHolder) viewHolder;
        vh.name.setText(getCurrencyName(cursor.getInt(cursor.getColumnIndex(MainProvider.FIELD_CURRENCY_ID))));
        vh.startTime.setText(cursor.getString(cursor.getColumnIndex(MainProvider.FIELD_START_TIME)));
        vh.rate.setText(cursor.getString(cursor.getColumnIndex(MainProvider.FIELD_END_PRICE)));

        String result = cursor.getString(cursor.getColumnIndex(MainProvider.FIELD_INVEST_RESULT));
        if (result.equals("1"))
            vh.result.setText("獲利");
        if (result.equals("2"))
        vh.result.setText("虧損");
        if (result.equals("3"))
            vh.result.setText("平手");
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = m_inflater.inflate(R.layout.card_history, parent, false);
        HistoryViewHolder viewHolder = new HistoryViewHolder(v);
        return viewHolder;
    }

    class HistoryViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        TextView startTime;
        TextView rate;
        TextView result;

        public HistoryViewHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.name);
            startTime = (TextView) itemView.findViewById(R.id.time);
            rate = (TextView) itemView.findViewById(R.id.rate);
            result = (TextView) itemView.findViewById(R.id.result);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Cursor cursor = (Cursor) getItem(getAdapterPosition());
                    if (null != mListener) {
                        int id = cursor.getInt(cursor.getColumnIndex(MainProvider.FIELD_INVEST_ID));
                        String name = getCurrencyName(cursor.getInt(cursor.getColumnIndex(MainProvider.FIELD_CURRENCY_ID)));
                        mListener.onClick(id, name);
                    }
                }
            });
        }
    }

    private String getCurrencyName(int id) {
        Cursor c = m_context.getContentResolver().query(mCurrencyUri, null, MainProvider.FIELD_CURRENCY_ID + "=?", new String[]{id + ""}, null);
        if (c != null && c.moveToFirst()) {
            String name =  c.getString(c.getColumnIndex(MainProvider.FIELD_CURRENCY_NAME));
            return name;
        }
        return "";
    }
}
