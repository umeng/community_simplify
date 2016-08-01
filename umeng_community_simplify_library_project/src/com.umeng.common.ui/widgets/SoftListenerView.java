package com.umeng.common.ui.widgets;

import android.annotation.TargetApi;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.umeng.comm.core.utils.CommonUtils;

public class SoftListenerView extends LinearLayout {

    private SoftListener mListener;
    private int mHeightChangeRange;

    private int mNormalHeight;

    public enum SoftState {
        SHOW, HIDE
    }

    public interface SoftListener {
        public void onSoftChange(SoftState softState, int softHeight);
    }

    @TargetApi(11)
    public SoftListenerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public SoftListenerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SoftListenerView(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        // 80px
        mHeightChangeRange = CommonUtils.dip2px(context, 40);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (oldh == 0) {
            mNormalHeight = h;
        } else if (mListener != null) {
            int distance = h - oldh;
            if (Math.abs(distance) > mHeightChangeRange) {
                if (distance > 0) {
                    mListener.onSoftChange(SoftState.HIDE, 0);
                } else {
                    mListener.onSoftChange(SoftState.SHOW, mNormalHeight - h);
                }
            }
        }
    }

    public void setSoftListener(SoftListener l) {
        this.mListener = l;
    }
}
