package com.dut.dutfinace.provider;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

import java.util.List;

public abstract class BaseContentProvider extends ContentProvider {

    private static final String TAG = "BaseContentProvider";
    public final static String FIELD_ID = "_id";
    public final static String FIELD_EXPIRE = "_expire";

    public final static int EXPIRED_DEFAULT = 1000 * 60 * 5;

    protected SQLiteOpenHelper m_db;

    @Override
    public int delete(Uri uri, String whereClause, String[] whereArgs) {
        return m_db.getWritableDatabase().delete(getTable(uri), whereClause, whereArgs);
    }

    @Override
    public String getType(Uri uri) {
        return this.getClass().getName();
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        long id = m_db.getWritableDatabase().insertWithOnConflict(getTable(uri), null, values, SQLiteDatabase.CONFLICT_REPLACE);
        return Uri.withAppendedPath(uri, String.valueOf(id));
    }

    @Override
    public Cursor query(Uri uri, String[] columns, String selection, String[] selectionArgs, String orderBy) {
        String table = getTable(uri);
        if (orderBy == null)
            orderBy = FIELD_ID + " ASC";
        Cursor c = m_db.getReadableDatabase().query(table, columns, selection, selectionArgs, null, null, orderBy);
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    @Override
    public int update(Uri uri, ContentValues values, String whereClause, String[] whereArgs) {
        int result =  m_db.getWritableDatabase().update(getTable(uri), values, whereClause, whereArgs);
        return result;
    }

    protected String getTable(Uri uri) {
        List<String> paths = uri.getPathSegments();
        if (paths != null && paths.size() > 0)
            return paths.get(0);
        return null;
    }

    public static Uri getProviderUri(String authority, String table) {
        Uri.Builder ub = new Uri.Builder()
                .scheme("content")
                .authority(authority)
                .appendPath(table);
        return ub.build();
    }
}
