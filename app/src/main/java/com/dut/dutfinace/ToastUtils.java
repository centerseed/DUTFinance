package com.dut.dutfinace;

import android.content.Context;
import android.widget.Toast;

public class ToastUtils {
    public static void showNetErrorToast(Context c, String err) {
        Toast.makeText(c, err, Toast.LENGTH_SHORT).show();
    }

    public static void showNetErrorToast(Context c, int response) {
        String err = "";
        switch (response) {
            case 400:
                err = "請求失敗";
                break;
            case 404:
                err = "找不到資源";
                return;
            case 500:
                err = "伺服器內部錯誤";
                return;
            default:
                err = "未知的錯誤";
        }
        Toast.makeText(c, err, Toast.LENGTH_SHORT).show();
    }
}
