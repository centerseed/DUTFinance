package com.dut.dutfinace.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MainProvider extends BaseContentProvider {

    public final static String TABLE_PROFILE = "_table_profile";
    public final static String TABLE_CURRENCY = "_table_currency";
    public final static String TABLE_HISTORY = "_table_trade_history";
    public final static String TABLE_CHART = "_table_chart";

    public final static String FIELD_AVAILABLE_FUND = "_available_fund";
    public final static String FIELD_NET_LIQ = "_net_liq";
    public final static String FIELD_USED_DEPOSIT = "_used_deposit";
    public final static String FIELD_AVAILABLE_DEPOSIT = "_available_deposit";

    public final static String FIELD_CURRENCY_NAME = "_currency_name";
    public final static String FIELD_CURRENCY_ID = "_currency_id";

    public final static String FIELD_INVEST_ID = "_invest_id";
    public final static String FIELD_INVEST_AMOUNT = "_invest_amount";
    public final static String FIELD_INVEST_TYPE = "_invest_type";
    public final static String FIELD_START_TIME = "_start_time";
    public final static String FIELD_START_PRICE = "_start_pricee";
    public final static String FIELD_END_TIME = "_end_type";
    public final static String FIELD_END_PRICE = "_end_price";
    public final static String FIELD_INVEST_RESULT = "_invest_result";

    public final static String FIELD_CHART_DATA = "_chart_data";
    public final static String FIELD_CHART_INTERVAL = "_chart_interval";

    @Override
    public boolean onCreate() {
        m_db = new MainDatabase(getContext());
        return false;
    }

    private class MainDatabase extends SQLiteOpenHelper {

        private final static int _DBVersion = 10;
        private final static String _DBName = "database.db";

        public MainDatabase(Context context) {
            super(context, _DBName, null, _DBVersion);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {

            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_PROFILE + " ( "
                    + FIELD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + FIELD_EXPIRE + " INTEGER, "
                    + FIELD_AVAILABLE_FUND + " TEXT, "
                    + FIELD_NET_LIQ + " TEXT, "
                    + FIELD_USED_DEPOSIT + " TEXT, "
                    + FIELD_AVAILABLE_DEPOSIT + " TEXT "
                    + ");");

            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_CURRENCY + " ( "
                    + FIELD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + FIELD_EXPIRE + " INTEGER, "
                    + FIELD_CURRENCY_NAME + " TEXT, "
                    + FIELD_CURRENCY_ID + " INTEGER "
                    + ");");

            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_HISTORY + " ( "
                    + FIELD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + FIELD_INVEST_ID + " INTEGER, "
                    + FIELD_CURRENCY_ID + " INTEGER, "
                    + FIELD_INVEST_AMOUNT + " INTEGER, "
                    + FIELD_INVEST_TYPE + " INTEGER, "
                    + FIELD_START_TIME + " TEXT, "
                    + FIELD_START_PRICE + " FLOAT, "
                    + FIELD_END_TIME + " TEXT, "
                    + FIELD_END_PRICE + " FLOAT, "
                    + FIELD_INVEST_RESULT + " TEXT "
                    + ");");

            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_CHART + " ( "
                    + FIELD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + FIELD_CURRENCY_NAME + " TEXT, "
                    + FIELD_CHART_DATA + " TEXT, "
                    + FIELD_CHART_INTERVAL + " TEXT "
                    + ");");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int i, int i1) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_PROFILE);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_CURRENCY);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_HISTORY);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_CHART);
            onCreate(db);
        }
    }
}
