package com.umeng.common.ui.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.widget.TextView;

import com.umeng.comm.core.utils.ResFinder;


/**
 * Created by wangfei on 16/3/24.
 */
public class CustomCommomDialog extends Dialog {
    public CustomCommomDialog(Context context, String strMessage) {
        this(context, ResFinder.getResourceId(ResFinder.ResType.STYLE, "CommonProgressDialog"), strMessage);
    }

    public CustomCommomDialog(Context context, int theme, String strMessage) {
        super(context, theme);
        this.setContentView(ResFinder.getResourceId(ResFinder.ResType.LAYOUT,"umeng_comm_waitdialog"));
        this.getWindow().getAttributes().gravity = Gravity.CENTER;
        TextView tvMsg = (TextView) this.findViewById(ResFinder.getResourceId(ResFinder.ResType.ID,"tv_loadingmsg"));
        if (tvMsg != null) {
            tvMsg.setText(strMessage);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {

        if (!hasFocus) {
            dismiss();
        }
    }

}
