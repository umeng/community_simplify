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

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.umeng.comm.core.beans.BaseBean;
import com.umeng.comm.core.beans.CommUser;
import com.umeng.comm.core.utils.CommonUtils;
import com.umeng.comm.core.utils.ResFinder;
import com.umeng.common.ui.activities.BaseFragmentActivity;
import com.umeng.simplify.ui.fragments.CommentTabFragment;
import com.umeng.simplify.ui.fragments.LikedMeFragment;
import com.umeng.simplify.ui.fragments.NotificationFragment;
import com.umeng.simplify.ui.fragments.PostedFeedsFragment;

public class NewMsgActivity extends BaseFragmentActivity implements View.OnClickListener {
    private int style = 0;//0 comment 1 likeme 3 notify
    private CommentTabFragment commentTabFragment;
    private LikedMeFragment likedMeFragment;
    private NotificationFragment notificationFragment;
    private PostedFeedsFragment postedFeedsFragment;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(ResFinder.getLayout("umeng_simplify_my"));
        setFragmentContainerId(ResFinder.getId("umeng_comm_my_msg_fragment"));
        if (getIntent() == null) {
            return;
        }
        int backButtonResId = ResFinder.getId("umeng_comm_title_back_btn");
        findViewById(backButtonResId).setOnClickListener(this);
        style = getIntent().getIntExtra("style", 0);
        entryFragment(style);
    }

    private void entryFragment(int style) {
        switch (style) {
            case 0:
                ((TextView) findViewById(ResFinder.getId("umeng_comm_title_tv"))).setText(ResFinder.getString("umeng_comm_mycomment"));
                if (commentTabFragment == null) {
                    commentTabFragment = new CommentTabFragment();
                    commentTabFragment.setUserInfoClassName(UserInfoActivity.class);
                    commentTabFragment.setTopicDetailClassName(TopicDetailActivity.class);
                    commentTabFragment.setFeedDetailClassName(FeedDetailActivity.class);
                }
                showFragment(commentTabFragment);
                break;
            case 1:
                ((TextView) findViewById(ResFinder.getId("umeng_comm_title_tv"))).setText(ResFinder.getString("umeng_comm_likeme"));
                if (likedMeFragment == null) {
                    likedMeFragment = new LikedMeFragment();
                    likedMeFragment.setUserInfoClassName(UserInfoActivity.class);
                    likedMeFragment.setTopicDetailClassName(TopicDetailActivity.class);
                    likedMeFragment.setFeedDetailClassName(FeedDetailActivity.class);
                }
                showFragment(likedMeFragment);
                break;
            case 2:
                ((TextView) findViewById(ResFinder.getId("umeng_comm_title_tv"))).setText(ResFinder.getString("umeng_comm_notify"));
                if (notificationFragment == null) {
                    notificationFragment = new NotificationFragment();
                    notificationFragment.setUserInfoClassName(UserInfoActivity.class);
                }
                showFragment(notificationFragment);
                break;
            case 3:
                ((TextView) findViewById(ResFinder.getId("umeng_comm_title_tv"))).setText(ResFinder.getString("umeng_comm_user_activity"));
                postedFeedsFragment = PostedFeedsFragment.newInstance();
                CommUser user = CommonUtils.getLoginUser(NewMsgActivity.this);
                postedFeedsFragment.setCurrentUser(user);
                postedFeedsFragment.setOnDeleteListener(new PostedFeedsFragment.OnDeleteListener() {

                    @Override
                    public void onDelete(BaseBean item) {

                    }
                });
                showFragment(postedFeedsFragment);
                break;
        }
    }

    @Override
    public void onClick(View v) {
        finish();

    }


}
