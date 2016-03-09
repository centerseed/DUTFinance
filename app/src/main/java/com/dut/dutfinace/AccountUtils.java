package com.dut.dutfinace;

import android.content.Context;
import android.preference.PreferenceManager;

public class AccountUtils {
    public static void setAccount(Context context, String account, String password,String sysId) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString("account", account).commit();
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString("password", password).commit();
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString("sysId", sysId).commit();
    }

    public static String getAccount(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString("account", "");
    }

    public static String getPassword(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString("password", "");
    }

    public static String getSysId(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString("sysId", "");
    }

    public static void setToken(Context context, String token) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString("token", token).commit();
    }

    public static String getToken(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString("token", null);
    }

    public static void setMaxOrderFund(Context context, int max) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putInt("max_order_size", max).commit();
    }

    public static int getMaxOrderFund(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getInt("max_order_size", 10);
    }
}
