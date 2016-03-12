package com.dut.dutfinace.adapter;

import android.content.Context;
import android.database.Cursor;
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
    public HistoryAdapter(Context context, Cursor c) {
        super(context, c);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, Cursor cursor) {
        HistoryViewHolder vh = (HistoryViewHolder) viewHolder;
        vh.name.setText(cursor.getString(cursor.getColumnIndex(MainProvider.FIELD_INVEST_ID)));
        vh.startTime.setText(cursor.getString(cursor.getColumnIndex(MainProvider.FIELD_START_TIME)));
        vh.rate.setText(cursor.getString(cursor.getColumnIndex(MainProvider.FIELD_END_PRICE)));
        vh.result.setText(cursor.getString(cursor.getColumnIndex(MainProvider.FIELD_INVEST_RESULT)).equals("1") ? "獲利" : "虧損");
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
        }
    }
}
