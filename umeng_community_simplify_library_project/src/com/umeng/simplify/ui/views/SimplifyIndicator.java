package com.umeng.simplify.ui.views;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.umeng.comm.core.utils.DeviceUtils;
import com.umeng.comm.core.utils.ResFinder;
import com.umeng.common.ui.widgets.MainIndicator;

/**
 * Created by wangfei on 16/4/29.
 */
public class SimplifyIndicator extends MainIndicator{
    public SimplifyIndicator(Context context) {
        this(context, null);
    }
    public SimplifyIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void highLightTextView(int position) {
        View view = getChildAt(position);
        if (view instanceof TextView) {
            ((TextView) view).setTextColor(ResFinder.getColor("umeng_comm_list_item_textcolor"));
            Drawable drawableLine = ResFinder.getDrawable("umeng_indicatorline");
            drawableLine.setBounds(0, 0,  DeviceUtils.dp2px(getContext(), 90), DeviceUtils.dp2px(getContext(), 3));
            ((TextView) view).setCompoundDrawables(null, null, null, drawableLine);
//            }
        }
    }
@Override
    protected void resetTextViewColor() {
        for (int i = 0; i < getChildCount(); i++) {
            View view = getChildAt(i);
            if (view instanceof TextView) {
                ((TextView) view).setTextColor(ResFinder.getColor("umeng_comm_text_time"));

                ((TextView) view).setCompoundDrawables(null,null,null,null);

            }
        }
    }
}
