package com.dut.dutfinace.adapter;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;

public abstract class AbstractRecyclerCursorAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Cursor m_cursor;
    private ContentObserver m_contentObserver;
    private DataSetObserver m_datasetObserver;
    protected Context m_context;
    protected LayoutInflater m_inflater;

    public AbstractRecyclerCursorAdapter(Context context, Cursor c) {
        init(context, c);
    }

    private void init(Context context, Cursor cursor) {
        m_inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        m_cursor = cursor;
        m_context = context;
        registerObserver(m_cursor);
        setHasStableIds(true);
    }

    protected void registerObserver(Cursor cursor) {
        if (m_contentObserver == null) {
            m_contentObserver = new ContentObserver(new Handler()) {
                @Override
                public void onChange(boolean selfChange) {
                    super.onChange(selfChange);
                }
            };
        }
        if (m_datasetObserver == null) {
            m_datasetObserver = new DataSetObserver() {
                @Override
                public void onChanged() {
                    super.onChanged();
                    notifyDataSetChanged();
                }

                @Override
                public void onInvalidated() {
                    super.onInvalidated();
                    notifyDataSetChanged();
                }
            };
        }
        if (cursor != null) {
            cursor.registerContentObserver(m_contentObserver);
            cursor.registerDataSetObserver(m_datasetObserver);
        }
    }

    protected void unregisterObserver(Cursor cursor) {
        if (cursor != null) {
            cursor.unregisterContentObserver(m_contentObserver);
            cursor.unregisterDataSetObserver(m_datasetObserver);
        }
    }

    public abstract void onBindViewHolder(RecyclerView.ViewHolder viewHolder, Cursor cursor);

    @Override
    public int getItemCount() {
        if (m_cursor != null)
            return m_cursor.getCount();
        else
            return 0;
    }

    public Object getItem(int position) {
        if (m_cursor != null && m_cursor.moveToPosition(position))
            return m_cursor;
        else
            return null;
    }

    @Override
    public long getItemId(int position) {
        if (m_cursor != null && m_cursor.moveToPosition(position))
            return m_cursor.getLong(m_cursor.getColumnIndex("_id"));
        else
            return 0;
    }

    @Override
    public int getItemViewType(int position) {
            return 0;
    }

    public int getItemViewType(Cursor cursor) {
        return 0;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int i) {
        onBindViewHolder(viewHolder, (Cursor) getItem(i));
    }

    public void swapCursor(Cursor cursor) {
        unregisterObserver(m_cursor);
        m_cursor = cursor;
        registerObserver(m_cursor);
        notifyDataSetChanged();
    }

    public Cursor getCursor() {
        return m_cursor;
    }
}
