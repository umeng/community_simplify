package com.umeng.common.ui.activities;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.umeng.comm.core.beans.CommUser;
import com.umeng.comm.core.constants.Constants;
import com.umeng.comm.core.constants.ErrorCode;
import com.umeng.comm.core.constants.StringUtil;
import com.umeng.comm.core.impl.CommunitySDKImpl;
import com.umeng.comm.core.login.LoginListener;
import com.umeng.comm.core.utils.Log;
import com.umeng.comm.core.utils.ResFinder;
import com.umeng.comm.core.utils.ToastMsg;
import com.umeng.common.ui.colortheme.ColorQueque;
import com.umeng.common.ui.dialogs.CustomCommomDialog;


/**
 * Created by wangfei on 16/5/6.
 */
public class RegisterActivity extends BaseActivity implements View.OnClickListener {
    public static LoginListener mRegisterListener;
    EditText numEt, nickEt, secretEt;
    ImageView secretShow, uploadImg;
    TextView finishBtn;
    private boolean isHidde = true;
    private String iconurl;

    Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(ResFinder.getLayout("umeng_register"));
        numEt = (EditText) findViewById(ResFinder.getId("umeng_login_num"));
        nickEt = (EditText) findViewById(ResFinder.getId("umeng_login_nicknum"));
        secretEt = (EditText) findViewById(ResFinder.getId("umeng_login_secret"));
        secretShow = (ImageView) findViewById(ResFinder.getId("umeng_secret_style"));
        uploadImg = (ImageView) findViewById(ResFinder.getId("umeng_upload_img"));
        finishBtn = (TextView) findViewById(ResFinder.getId("umeng_finish_register"));
        numEt.setOnClickListener(this);
        nickEt.setOnClickListener(this);
        secretEt.setOnClickListener(this);
        secretShow.setOnClickListener(this);
        finishBtn.setOnClickListener(this);
//        uploadImg.setOnClickListener(this);
        findViewById(ResFinder.getId("umeng_comm_setting_back")).setOnClickListener(this);
        dialog = new CustomCommomDialog(this, ResFinder.getString("umeng_comm_text_waitting"));
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == ResFinder.getId("umeng_secret_style")) {
            if (isHidde) {
                secretShow.setImageDrawable(ColorQueque.getDrawable("umeng_comm_register_showpassword"));
                secretEt.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                isHidde = false;
            } else {
                secretShow.setImageDrawable(ColorQueque.getDrawable("umeng_comm_register_hidepassword"));
                secretEt.setTransformationMethod(PasswordTransformationMethod.getInstance());
                isHidde = true;
            }
        } else if (v.getId() == ResFinder.getId("umeng_finish_register")) {
            String name = numEt.getText().toString();
            String secret = secretEt.getText().toString();
            String nickname = nickEt.getText().toString();
            CommUser user = new CommUser();
            user.id = name;
            user.name = nickname;
            Constants.USER_PASSWORD = secret;
            if (!name.contains("@")) {
                ToastMsg.showShortMsgByResName("umeng_comm_login_illuid");
                return;
            }
            if (!StringUtil.isWordAndNum(nickname) || nickname.length() < 2 || nickname.length() > 20) {
                ToastMsg.showShortMsgByResName("umeng_comm_username_ill");
                return;
            }
            if (!StringUtil.isWordAndNum(secret) || secret.length() < 6 || secret.length() > 18) {
                ToastMsg.showShortMsgByResName("umeng_comm_login_illpassword");
                return;
            }
            CommunitySDKImpl.getInstance().registerToWsq(RegisterActivity.this, user, new LoginListener() {
                @Override
                public void onStart() {
                    dialog.show();
                }

                @Override
                public void onComplete(int stCode, CommUser userInfo) {
                    dialog.dismiss();
                    if (stCode == ErrorCode.NO_ERROR||stCode == ErrorCode.ERR_CODE_USER_NAME_DUPLICATE) {
                        Log.e("xxxxxx", "finish!!!!!!!");
                        finish();
                        if (stCode == ErrorCode.NO_ERROR) {
                            ToastMsg.showShortMsgByResName("umeng_comm_register_success");
                        }
                        mRegisterListener.onComplete(stCode, userInfo);
                    }
                }
            }, secret);

        } else if (v.getId() == ResFinder.getId("umeng_comm_setting_back")) {
            if (getCurrentFocus() != null) {
                ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(
                        getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
            this.finish();
        }
    }




}
