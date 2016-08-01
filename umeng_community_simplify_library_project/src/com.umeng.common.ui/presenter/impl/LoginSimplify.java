package com.umeng.common.ui.presenter.impl;

import android.content.Context;
import android.content.Intent;

import com.umeng.comm.core.constants.ErrorCode;
import com.umeng.comm.core.login.LoginListener;
import com.umeng.comm.core.login.Loginable;
import com.umeng.common.ui.activities.LoginSimplifyActivity;


/**
 * Created by wangfei on 16/5/5.
 */
public class LoginSimplify implements Loginable {
    @Override
    public void login(Context context, LoginListener listener) {
        Intent intent = new Intent(context, LoginSimplifyActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        LoginSimplifyActivity.mLoginListener = listener;
        context.startActivity(intent);
    }

    @Override
    public void logout(Context context, LoginListener listener) {
        listener.onStart();
        listener.onComplete(ErrorCode.SUCCESS, null);
    }
}
