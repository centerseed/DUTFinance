package com.dut.dutfinace;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.dut.dutfinace.activity.OrderActivity;

public class TradeFragment extends Fragment {

    LinearLayout mTarget1;
    LinearLayout mTarget2;
    LinearLayout mTarget3;
    LinearLayout mTarget4;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_trade, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mTarget1 = (LinearLayout) view.findViewById(R.id.target1);
        mTarget1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), OrderActivity.class);
                getContext().startActivity(intent);
            }
        });

        mTarget2 = (LinearLayout) view.findViewById(R.id.target2);
        mTarget2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), OrderActivity.class);
                getContext().startActivity(intent);
            }
        });

        mTarget3 = (LinearLayout) view.findViewById(R.id.target3);
        mTarget3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), OrderActivity.class);
                getContext().startActivity(intent);
            }
        });

        mTarget4 = (LinearLayout) view.findViewById(R.id.target4);
        mTarget4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), OrderActivity.class);
                getContext().startActivity(intent);
            }
        });
    }
}
