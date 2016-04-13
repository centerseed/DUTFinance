package com.dut.dutfinace;

import android.content.Context;
import android.text.Html;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.TextView;

import java.text.DecimalFormat;

public class NewPriceText extends TextView {

    double mLastPrice = 0;
    Context mContext;
    DecimalFormat mPriceDf = new DecimalFormat("#.00000");
    int mDigit = 5;

    public NewPriceText(Context context) {
        super(context);
        mContext = context;
    }

    public NewPriceText(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    public NewPriceText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
    }

    public NewPriceText(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mContext = context;
    }

    public void setPrice(double price) {
        if (mLastPrice == 0) {
            mLastPrice = price;
            setTextColor(mContext.getResources().getColor(R.color.colorWhite));
            setText(mPriceDf.format(price));
            return;
        }

        String priceStr = String.valueOf(price);
        String lastPriceStr = String.valueOf(mLastPrice);
        if (priceStr.equals(lastPriceStr)) {
            setTextColor(mContext.getResources().getColor(R.color.colorWhite));
            setText(mPriceDf.format(price));
            return;
        }

        int differentPos = 0;
        if (priceStr.length() != lastPriceStr.length()) {
            if (priceStr.length() > lastPriceStr.length()) {
                differentPos = lastPriceStr.length();
            } else {
                differentPos = priceStr.length();
            }
        } else {
            for (int i = 0; i < priceStr.length(); i++) {
                if (priceStr.charAt(i) != lastPriceStr.charAt(i)) {
                    differentPos = i;
                    break;
                }
            }
        }

        if (differentPos == 0) {
            setTextColor(mContext.getResources().getColor(R.color.colorWhite));
            setText(mPriceDf.format(price));
            return;
        }

        int point = priceStr.indexOf(".");
        String subStr = priceStr.substring(point + 1);
        if (subStr.length() > mDigit) {
            priceStr = priceStr.substring(0, point + mDigit);
        }

        String prefix = priceStr.substring(0, differentPos - 1);
        String postfix = priceStr.substring(differentPos - 1, priceStr.length());

        if (subStr.length() < mDigit) {
            for (int i = 0; i < mDigit - subStr.length(); i++) {
                postfix += "0";
            }
        }



        if (price > mLastPrice) {
            String upText = "<font color=#FFFFFF>" + prefix + "</font><font color=#2aab2e>" + postfix + "</font>";
            setText(Html.fromHtml(upText));
        } else if (price < mLastPrice) {
            String downText = "<font color=#FFFFFF>" + prefix + "</font><font color=#d7502b>" + postfix + "</font>";
            setText(Html.fromHtml(downText));
        } else {
            setTextColor(mContext.getResources().getColor(R.color.colorWhite));
            setText(price + "");
        }

        mLastPrice = price;
    }

    public void setTextSize(int size) {
        this.setTextSize(TypedValue.COMPLEX_UNIT_DIP, size);
    }

    public double getPrice() {
        return mLastPrice;
    }

    public void setLastPointNum(int lastPointNum) {
        String format = "#.";
        for (int i = 0; i < lastPointNum; i++) {
            format += "0";
        }
        mPriceDf = new DecimalFormat(format);
        mDigit = lastPointNum;
    }
}
