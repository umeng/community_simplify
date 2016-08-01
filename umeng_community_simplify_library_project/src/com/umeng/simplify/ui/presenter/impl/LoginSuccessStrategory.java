package com.umeng.simplify.ui.presenter.impl;

import android.content.Context;
import android.content.Intent;

import com.umeng.comm.core.beans.CommConfig;
import com.umeng.comm.core.beans.CommUser;
import com.umeng.comm.core.constants.Constants;
import com.umeng.comm.core.constants.ErrorCode;
import com.umeng.comm.core.login.AbsLoginResultStrategy;
import com.umeng.comm.core.nets.responses.LoginResponse;
import com.umeng.comm.core.utils.CommonUtils;
import com.umeng.simplify.ui.activities.SettingActivity;

/**
 * Created by wangfei on 16/5/9.
 */
public class LoginSuccessStrategory extends AbsLoginResultStrategy {
    @Override
    public void onLoginResult(Context context, CommUser user, LoginResponse response,int loginStyle) {
        boolean isUserNameInvalid = isUserNameInvalid(response);
        if (response.isFirstTimeLogin
                && response.errCode == ErrorCode.NO_ERROR
                || isUserNameInvalid) {

            gotoUpdateUserPage(context, user, isUserNameInvalid,loginStyle);
        }
    }
    private void gotoUpdateUserPage(Context context, CommUser user, boolean isUserNameInvalid, int loginStyle) {

        Intent intent = new Intent(context, SettingActivity.class);
        intent.putExtra(Constants.USER_SETTING, true);
        intent.putExtra(Constants.REGISTER_USERNAME_INVALID, isUserNameInvalid);
        intent.putExtra(Constants.USER, isUserNameInvalid ? user
                : CommConfig.getConfig().loginedUser);
        intent.putExtra(Constants.LOGIN_STYLE, loginStyle);

        context.startActivity(intent);
    }
    protected boolean isUserNameInvalid(LoginResponse response) {
        return CommonUtils.checkUserNameStatus(response.errCode);
    }
}
