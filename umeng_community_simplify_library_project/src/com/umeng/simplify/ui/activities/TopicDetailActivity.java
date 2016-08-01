/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2015 Umeng, Inc
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.umeng.simplify.ui.activities;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.umeng.comm.core.constants.Constants;
import com.umeng.comm.core.listeners.Listeners.OnResultListener;
import com.umeng.comm.core.utils.ResFinder;
import com.umeng.comm.core.utils.ResFinder.ResType;
import com.umeng.common.ui.activities.TopicDetailBaseActivity;
import com.umeng.simplify.ui.fragments.TopicFeedFragment;

/**
 * 话题详情页
 */
public class TopicDetailActivity extends TopicDetailBaseActivity {

    /**
     * 话题详情的Fragment
     */
    private TopicFeedFragment mDetailFragment;




    @Override
    protected void initTitles() {
        mTitles = getResources().getStringArray(
                ResFinder.getResourceId(ResType.ARRAY, "umeng_simplify_topic_detail_tabs"));
    }

    @Override
    protected void initTitle() {
        findViewById(ResFinder.getId("umeng_comm_title_back_btn")).setOnClickListener(this);
        titleTextView = (TextView) findViewById(ResFinder.getId("umeng_comm_title_tv"));
        titleTextView.setText(mTopic.name);
        findViewById(ResFinder.getId("umeng_comm_title_setting_btn")).setVisibility(View.GONE);
        favouriteBtn = (ToggleButton) findViewById(ResFinder.getId("umeng_comm_favourite_btn"));
        favouriteBtn.setVisibility(View.VISIBLE);
        postBtn = findViewById(ResFinder.getId("umeng_comm_post_btn"));
        postBtn.setVisibility(View.GONE);
        setTopicStatus();
//        mPresenter.SetFavouriteButton(favouriteBtn);
//        favouriteBtn.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//
//
//                favouriteBtn.setClickable(false);
//
//
//                CommonUtils.checkLoginAndFireCallback(TopicDetailBaseActivity.this,
//                        new Listeners.SimpleFetchListener<LoginResponse>() {
//
//                            @Override
//                            public void onComplete(LoginResponse response) {
//                                favouriteBtn.setChecked(!favouriteBtn.isChecked());
//                                if (response.errCode != ErrorCode.NO_ERROR) {
//                                    favouriteBtn.setChecked(!favouriteBtn.isChecked());
//                                    return;
//                                }
//                                if (favouriteBtn.isChecked()) {
//                                    mPresenter.cancelFollowTopic(mTopic);
//                                } else {
//                                    mPresenter.followTopic(mTopic);
//                                }
//                                isClick = true;
//
////                                    favouriteBtn.setClickable(true);
//                            }
//                        });
//
//            }
//        });
        postBtn.setOnClickListener(new com.umeng.comm.core.listener.Listeners.LoginOnViewClickListener() {
            @Override
            protected void doAfterLogin(View v) {
                gotoPostFeedActivity();
            }
        });
//        favouriteBtn.setTextOff("");
//        favouriteBtn.setTextOn("");

    }

    @Override
    protected void initView() {
        super.initView();
        mIndicator.setVisibility(View.GONE);
        favouriteBtn.setVisibility(View.GONE);
    }

    @Override
    protected int getLayout() {
        return ResFinder.getLayout("umeng_simplify_topic_detail_layout");
    }

    /**
     * 跳转至发送新鲜事页面</br>
     */
    protected void gotoPostFeedActivity() {
        Intent postIntent = new Intent(TopicDetailActivity.this, PostFeedActivity.class);
        postIntent.putExtra(Constants.TAG_TOPIC, mTopic);
        startActivity(postIntent);
    }
    /**
     * 获取对应的Fragment。0：话题聚合 1：活跃用户</br>
     * 
     * @param pos
     * @return
     */
    protected Fragment getFragment(int pos) {
        if (pos == 0) {
            if (mDetailFragment == null) {
                mDetailFragment = TopicFeedFragment.newTopicFeedFrmg(mTopic);
            }
            mDetailFragment.setOnAnimationListener(mListener);
            return mDetailFragment;
        }
        return null;
    }


    private OnResultListener mListener = new OnResultListener() {

        @Override
        public void onResult(int status) {
//            if (status == 1) {// dismiss
//                mCustomAnimator.startDismissAnimation(mHeaderView);
//            } else if (status == 0) { // show
//                mCustomAnimator.startShowAnimation(mHeaderView);
//            }
        }
    };
    



}
