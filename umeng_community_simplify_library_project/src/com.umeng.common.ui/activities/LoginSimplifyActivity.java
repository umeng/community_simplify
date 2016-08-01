package com.umeng.common.ui.activities;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.umeng.comm.core.beans.CommUser;
import com.umeng.comm.core.constants.ErrorCode;
import com.umeng.comm.core.constants.StringUtil;
import com.umeng.comm.core.impl.CommunitySDKImpl;
import com.umeng.comm.core.listeners.Listeners;
import com.umeng.comm.core.login.LoginListener;
import com.umeng.comm.core.nets.responses.SimpleResponse;
import com.umeng.comm.core.utils.Log;
import com.umeng.comm.core.utils.ResFinder;
import com.umeng.comm.core.utils.ToastMsg;
import com.umeng.common.ui.colortheme.ColorQueque;
import com.umeng.common.ui.dialogs.CustomCommomDialog;

/**
 * Created by wangfei on 16/5/5.
 */
public class LoginSimplifyActivity extends BaseActivity implements View.OnClickListener {
    EditText nameEd, secretEd;
    TextView forgetBtn, loginBtn, registerBtn;
    ImageView showBtn;
    public static LoginListener mLoginListener;
    private boolean isHidde = true;
    Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(ResFinder.getLayout("umeng_simplify_login"));
        nameEd = (EditText) findViewById(ResFinder.getId("umeng_login_num"));
        secretEd = (EditText) findViewById(ResFinder.getId("umeng_login_secret"));
        forgetBtn = (TextView) findViewById(ResFinder.getId("umeng_forget_secret"));
        loginBtn = (TextView) findViewById(ResFinder.getId("umeng_simplify_login"));
        registerBtn = (TextView) findViewById(ResFinder.getId("umeng_simplify_register"));
        showBtn = (ImageView) findViewById(ResFinder.getId("umeng_secret_style"));
        showBtn.setOnClickListener(this);
        forgetBtn.setOnClickListener(this);
        loginBtn.setOnClickListener(this);
        registerBtn.setOnClickListener(this);
        findViewById(ResFinder.getId("umeng_login_close")).setOnClickListener(this);
        dialog = new CustomCommomDialog(this, ResFinder.getString("umeng_comm_text_waitting"));
    }

    @Override
    public void onClick(View v) {

        if (v.getId() == ResFinder.getId("umeng_forget_secret")){
            String name = nameEd.getText().toString();
            if (TextUtils.isEmpty(name)){
                ToastMsg.showShortMsgByResName("umeng_comm_login_empty");
                return;
            }
            if (!name.contains("@")) {
                ToastMsg.showShortMsgByResName("umeng_comm_login_illuid");
                return;
            }
            CommunitySDKImpl.getInstance().forgetPWD(name, new Listeners.FetchListener<SimpleResponse>() {
                @Override
                public void onStart() {
                    dialog.show();
                }

                @Override
                public void onComplete(SimpleResponse response) {
                    dialog.dismiss();
                    Log.e("xxxxxx","errorcode="+response.errCode);
                    if (response.errCode == ErrorCode.NO_ERROR){
                        ToastMsg.showShortMsgByResName("umeng_comm_forget_success");
                    }else if (response.errCode == ErrorCode.ERR_CODE_USER_DELETED){
                        ToastMsg.showShortMsgByResName("umeng_comm_name_lost");
                    }else {
                       ToastMsg.showShortMsgByResName("umeng_comm_http_req_failed");
                    }
                }
            });
        }else if (v.getId() == ResFinder.getId("umeng_simplify_login")){

            String name = nameEd.getText().toString();
            String secret = secretEd.getText().toString();
            if (TextUtils.isEmpty(name)) {
                ToastMsg.showShortMsgByResName("umeng_comm_login_empty");
                return;
            }
            if (!name.contains("@")) {
                ToastMsg.showShortMsgByResName("umeng_comm_login_illuid");
                return;
            }
            if (TextUtils.isEmpty(secret)) {
                ToastMsg.showShortMsgByResName("umeng_comm_login_secret_empty");
                return;
            }
            if (!StringUtil.isWordAndNum(secret) || secret.length() < 6 || secret.length() > 18) {
                ToastMsg.showShortMsgByResName("umeng_comm_login_illpassword");
                return;
            }
            CommUser user = new CommUser();
            user.id = name;
            CommunitySDKImpl.getInstance().loginToWsq(LoginSimplifyActivity.this, user, new LoginListener() {
                @Override
                public void onStart() {
                    dialog.show();
                }

                @Override
                public void onComplete(int stCode, CommUser userInfo) {
                    dialog.dismiss();
                    if (stCode == ErrorCode.NO_ERROR) {

                        mLoginListener.onComplete(stCode, userInfo);
                        finish();
                    }
                }
            }, secret);
        } else if (v.getId() == ResFinder.getId("umeng_simplify_register")) {
            Intent intent = new Intent(LoginSimplifyActivity.this, RegisterActivity.class);
            RegisterActivity.mRegisterListener = mLoginListener;
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();

        } else if (v.getId() == ResFinder.getId("umeng_login_close")) {
            if (getCurrentFocus() != null) {
                ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(
                        getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
            LoginSimplifyActivity.this.finish();
        } else if (v.getId() == ResFinder.getId("umeng_secret_style")) {
            if (isHidde) {
                showBtn.setImageDrawable(ColorQueque.getDrawable("umeng_comm_register_showpassword"));
                secretEd.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                isHidde = false;
            } else {
                showBtn.setImageDrawable(ColorQueque.getDrawable("umeng_comm_register_hidepassword"));
                secretEd.setTransformationMethod(PasswordTransformationMethod.getInstance());
                isHidde = true;
            }
        }
    }
}
