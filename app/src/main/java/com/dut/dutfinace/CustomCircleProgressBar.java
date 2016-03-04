package com.dut.dutfinace;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;

import com.mikhaellopez.circularprogressbar.CircularProgressBar;

public class CustomCircleProgressBar extends CircularProgressBar {
    public CustomCircleProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setProgressWithAnimation(float progress, int duration) {
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(this, "progress", progress);
        objectAnimator.setDuration(duration);
        objectAnimator.start();
    }
}
