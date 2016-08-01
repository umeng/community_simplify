package com.umeng.common.ui.activities;

import android.app.Activity;
import android.os.Bundle;

import com.umeng.comm.core.constants.Constants;
import com.umeng.comm.core.utils.ResFinder;

/**
 * Created by wangfei on 16/7/7.
 */
public class BaseActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.setTheme(ResFinder.getStyle(Constants.theme));
        super.onCreate(savedInstanceState);
    }
}
