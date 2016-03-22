package com.dut.dutfinace;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dut.dutfinace.provider.MainProvider;

import java.text.DecimalFormat;

public class PriceTextView extends LinearLayout {
    View mView;
    TextView mPrice;
    TextView mPostfix;
    Context mContext;
    DecimalFormat mPriceDf = new DecimalFormat("#.000");

    double mLastPrice = 0;

    public PriceTextView(Context context) {
        super(context);
        mContext = context;
        initView(context);
    }

    public PriceTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        initView(context);
    }

    public PriceTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        initView(context);
    }

    private void initView(Context context) {
        mContext = context;
        LayoutInflater mInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mView = mInflater.inflate(R.layout.price_view, this, true);
        mPrice = (TextView) mView.findViewById(R.id.prefix);
        mPostfix = (TextView) mView.findViewById(R.id.postfix);
    }

    public void setPrice(double price) {
        int color = R.color.colorWhite;
        if (price >= mLastPrice) color = mContext.getResources().getColor(R.color.colorUp);
        if (price < mLastPrice) color = mContext.getResources().getColor(R.color.colorDown);
        mPostfix.setTextColor(color);

        mPrice.setText(mPriceDf.format(price));
        int post = (int) (price * 100000 % 100);
        if (post < 10)
            mPostfix.setText("0" + post);
        else
            mPostfix.setText(post + "");

        mLastPrice = price;
    }

    public double getPrice() {
        return mLastPrice;
    }

    public void setTextSize(int size) {
        mPrice.setTextSize(TypedValue.COMPLEX_UNIT_DIP, size);
        mPostfix.setTextSize(TypedValue.COMPLEX_UNIT_DIP, size);
    }
}
