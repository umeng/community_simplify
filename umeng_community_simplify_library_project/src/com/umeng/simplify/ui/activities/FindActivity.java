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
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.umeng.comm.core.beans.CommUser;
import com.umeng.comm.core.constants.Constants;
import com.umeng.comm.core.impl.CommunitySDKImpl;
import com.umeng.comm.core.listeners.Listeners;
import com.umeng.comm.core.login.LoginListener;
import com.umeng.comm.core.utils.CommonUtils;
import com.umeng.comm.core.utils.ResFinder;
import com.umeng.common.ui.activities.FindBaseActivity;
import com.umeng.common.ui.dialogs.CustomCommomDialog;
import com.umeng.simplify.ui.fragments.FavoritesFragment;

/**
 * 发现的Activity
 */
public class FindActivity extends FindBaseActivity implements OnClickListener{

    private FavoritesFragment mFavoritesFragment;

    private ImageView mGenderImageView;

    private View notify_comment,notify_like;
    protected void initViews() {
        getLayout();

        processDialog = new CustomCommomDialog(this, ResFinder.getString("umeng_comm_logining"));
        findViewById(ResFinder.getId("umeng_comm_title_back_btn")).setOnClickListener(this);
        findViewById(ResFinder.getId("umeng_comm_mine")).setOnClickListener(this);
        findViewById(ResFinder.getId("umeng_comm_favortes")).setOnClickListener(this);
        findViewById(ResFinder.getId("user_have_login")).setOnClickListener(this);
        findViewById(ResFinder.getId("user_haveno_login")).setOnClickListener(this);
//        findViewById(ResFinder.getId("umeng_comm_setting_recommend")).setOnClickListener(this);
        findViewById(ResFinder.getId("umeng_comm_mycomment")).setOnClickListener(this);
        findViewById(ResFinder.getId("umeng_comm_mylike")).setOnClickListener(this);
        findViewById(ResFinder.getId("umeng_comm_mynotify")).setOnClickListener(this);
        findViewById(ResFinder.getId("umeng_comm_setting")).setOnClickListener(this);


        typeContainer = (LinearLayout) findViewById(ResFinder.getId("user_type_icon_container"));
        // 未读消息红点
        mMsgBadgeView = findViewById(ResFinder.getId("umeng_comm_notify_badge_view"));
        notify_comment = findViewById(ResFinder.getId("umeng_comm_notify_comment"));
        notify_like = findViewById(ResFinder.getId("umeng_comm_notify_like"));
        mMsgBadgeView.setVisibility(View.GONE);
        notify_like.setVisibility(View.GONE);
        notify_comment.setVisibility(View.GONE);
        mGenderImageView = (ImageView) findViewById(ResFinder.getId("umeng_comm_user_gender"));

//        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);

        parseIntentData();
        setupUnreadFeedMsgBadge();

        mUser = CommonUtils.getLoginUser(this);

        registerInitSuccessBroadcast();
    }

    @Override
    protected void initUserInfo() {
        super.initUserInfo();
        if (mUser.gender == CommUser.Gender.MALE) {
            mGenderImageView.setImageDrawable(ResFinder.getDrawable("umeng_male"));
        } else if (mUser.gender == CommUser.Gender.FEMALE) {
            mGenderImageView.setImageDrawable(ResFinder.getDrawable("umeng_female"));
        }

    }


    protected void setupUnreadFeedMsgBadge() {
       super.setupUnreadFeedMsgBadge();

            if (mUnReadMsg.unReadCommentsCount > 0) {
                if (CommonUtils.isLogin(this)) {
                    notify_comment.setVisibility(View.VISIBLE);
                } else {
                    notify_comment.setVisibility(View.GONE);
                }
            } else {
                notify_comment.setVisibility(View.GONE);
            }
        if (mUnReadMsg.unReadLikesCount > 0) {
            if (CommonUtils.isLogin(this)) {
                notify_like.setVisibility(View.VISIBLE);
            } else {
                notify_like.setVisibility(View.GONE);
            }
        } else {
            notify_like.setVisibility(View.GONE);
        }
        if (mUnReadMsg.unReadNotice > 0) {
            if (CommonUtils.isLogin(this)) {
                mMsgBadgeView.setVisibility(View.VISIBLE);
            } else {
                mMsgBadgeView.setVisibility(View.GONE);
            }
        } else {
            mMsgBadgeView.setVisibility(View.GONE);
        }


    }
    @Override
    protected void gotoMyFollowActivity() {
//
    }

    @Override
    protected void gotoMyPicActivity() {

    }

    @Override
    protected void gotoNotificationActivity() {

    }


    protected void gotoMine() {

    }

    public void onClick(View v) {
        int id = v.getId();
        if (id == ResFinder.getId("umeng_comm_title_back_btn")) { // 返回事件
            finish();
        } else if (id == ResFinder.getId("umeng_comm_mine")) {  //我的动态
            if (!CommonUtils.isLogin(FindActivity.this)) {
                CommunitySDKImpl.getInstance().login(FindActivity.this, new LoginListener() {
                    @Override
                    public void onStart() {
                        processDialog.show();
                    }

                    @Override
                    public void onComplete(int stCode, CommUser userInfo) {

                        if (stCode == 0) {
//                            showFriendsFragment();
                            showmy();
                        }
                        processDialog.dismiss();
                    }
                });
            } else {
                showmy();

            }
        } else if (id == ResFinder.getId("umeng_comm_favortes")) { // 我的收藏

            if (!CommonUtils.isLogin(FindActivity.this)) {
                CommunitySDKImpl.getInstance().login(FindActivity.this, new LoginListener() {
                    @Override
                    public void onStart() {
                        processDialog.show();
                    }

                    @Override
                    public void onComplete(int stCode, CommUser userInfo) {

                        processDialog.dismiss();
                        if (stCode == 0) {
                            showFavoritesFeed();
                        }
                    }
                });
            } else {
                showFavoritesFeed();
            }
        } else if (id == ResFinder.getId("umeng_comm_mycomment")) { // 评论
            if (!CommonUtils.isLogin(FindActivity.this)) {
                CommunitySDKImpl.getInstance().login(FindActivity.this, new LoginListener() {
                    @Override
                    public void onStart() {
                        processDialog.show();
                    }

                    @Override
                    public void onComplete(int stCode, CommUser userInfo) {

                        processDialog.dismiss();
                        if (stCode == 0) {
                            showComment();
                        }
                    }
                });
            } else {
                showComment();
            }
        } else if (id == ResFinder.getId("umeng_comm_mylike")) { // 赞我的
            if (!CommonUtils.isLogin(FindActivity.this)) {
                CommunitySDKImpl.getInstance().login(FindActivity.this, new LoginListener() {
                    @Override
                    public void onStart() {
                        processDialog.show();
                    }

                    @Override
                    public void onComplete(int stCode, CommUser userInfo) {

                        processDialog.dismiss();
                        if (stCode == 0) {
                            showLikeMe();
                        }
                    }
                });
            } else {
                showLikeMe();
            }
        } else if (id == ResFinder.getId("umeng_comm_mynotify")) { // 通知
            if (!CommonUtils.isLogin(FindActivity.this)) {
                CommunitySDKImpl.getInstance().login(FindActivity.this, new LoginListener() {
                    @Override
                    public void onStart() {
                        processDialog.show();
                    }

                    @Override
                    public void onComplete(int stCode, CommUser userInfo) {

                        processDialog.dismiss();
                        if (stCode == 0) {
                            showNotify();
                        }
                    }
                });
            } else {
                showNotify();
            }
        } else if (id == ResFinder.getId("umeng_comm_setting")) {
            if (!CommonUtils.isLogin(FindActivity.this)) {
                CommunitySDKImpl.getInstance().login(FindActivity.this, new LoginListener() {
                    @Override
                    public void onStart() {
                        processDialog.show();
                    }

                    @Override
                    public void onComplete(int stCode, CommUser userInfo) {

                        processDialog.dismiss();
                        if (stCode == 0) {
                            Intent setting = new Intent(FindActivity.this, SettingActivity.class);
                            setting.putExtra(Constants.TYPE_CLASS, mContainerClass);
                            startActivity(setting);
                        }
                    }
                });
            } else {
                Intent setting = new Intent(FindActivity.this, SettingActivity.class);
                setting.putExtra(Constants.TYPE_CLASS, mContainerClass);
                startActivity(setting);

            }

        } else if (id == ResFinder.getId("user_haveno_login")) { // 个人中心
            CommunitySDKImpl.getInstance().login(FindActivity.this, new LoginListener() {
                @Override
                public void onStart() {
                    processDialog.show();
                }

                @Override
                public void onComplete(int stCode, CommUser userInfo) {
                    initUserInfo();
                    processDialog.dismiss();
                }
            });
        }
    }

    protected void gotoFeedNewMsgActivity() {
//        Intent intent = new Intent(FindActivity.this, NewMsgActivity.class);
//        intent.putExtra(Constants.USER, mUser);
//        startActivity(intent);
    }

    protected void getLayout() {
        setContentView(ResFinder.getLayout("umeng_simplify_find_layout"));
    }

    /**
     * 跳转到用户中心Activity</br>
     */
    protected void gotoUserInfoActivity() {

    }

    /**
     * 显示附件推荐Feed</br>
     */
    protected void showNearbyFeed() {

    }

    /**
     * 显示实时内容的Fragment</br>
     */
    protected void showRealTimeFeed() {

    }

    /**
     * 显示收藏Feed</br>
     */
    protected void showFavoritesFeed() {
        if (mFavoritesFragment == null) {
            mFavoritesFragment = FavoritesFragment.newFavoritesFragment();
            mFavoritesFragment.setOnResultListener(new Listeners.OnResultListener() {

                @Override
                public void onResult(int status) {
                    showFindPage();
                }
            });
        }
        showCommFragment(mFavoritesFragment);
    }

    /**
     * 显示评论</br>
     */
    protected void showComment() {
        mUnReadMsg.unReadCommentsCount = 0;
        Intent intent = new Intent(FindActivity.this, NewMsgActivity.class);
        intent.putExtra("style", 0);
        mUnReadMsg.unReadTotal =  mUnReadMsg.unReadCommentsCount
                + mUnReadMsg.unReadLikesCount + mUnReadMsg.unReadNotice ;
//        Log.e("xxxxxx", "!!!!!!!!yoyal1=" + mUnReadMsg.unReadTotal+"  "+mUnReadMsg.unReadCommentsCount+mUnReadMsg.unReadLikesCount+mUnReadMsg.unReadNotice);
        startActivity(intent);
    }


    protected void showLikeMe() {
        mUnReadMsg.unReadLikesCount = 0;
        Intent intent = new Intent(FindActivity.this, NewMsgActivity.class);
        intent.putExtra("style", 1);
        mUnReadMsg.unReadTotal =  mUnReadMsg.unReadCommentsCount
                + mUnReadMsg.unReadLikesCount + mUnReadMsg.unReadNotice;
//        Log.e("xxxxxx", "!!!!!!!!yoyal2=" + mUnReadMsg.unReadTotal+"  "+mUnReadMsg.unReadCommentsCount+mUnReadMsg.unReadLikesCount+mUnReadMsg.unReadNotice);
        startActivity(intent);
    }

    protected void showNotify() {
        mUnReadMsg.unReadNotice = 0;
        Intent intent = new Intent(FindActivity.this, NewMsgActivity.class);
        intent.putExtra("style", 2);
        mUnReadMsg.unReadTotal =  mUnReadMsg.unReadCommentsCount
                + mUnReadMsg.unReadLikesCount + mUnReadMsg.unReadNotice;
//        Log.e("xxxxxx", "!!!!!!!!yoyal3=" + mUnReadMsg.unReadTotal+"  "+mUnReadMsg.unReadCommentsCount+mUnReadMsg.unReadLikesCount+mUnReadMsg.unReadNotice);
        startActivity(intent);
    }

    protected void showmy() {
        Intent intent = new Intent(FindActivity.this, NewMsgActivity.class);
        intent.putExtra("style", 3);

        startActivity(intent);
    }

    /**
     * 显示推荐话题的Dialog</br>
     */
    protected void showRecommendTopic() {

    }

    /**
     * 隐藏发现页面，显示fragment</br>
     *
     * @param fragment
     */
    protected void showCommFragment(Fragment fragment) {
        findViewById(ResFinder.getId("umeng_comm_find_base")).setVisibility(View.GONE);
        int container = ResFinder.getId("container");
        findViewById(container).setVisibility(View.VISIBLE);
        setFragmentContainerId(container);
        showFragmentInContainer(container, fragment);
    }

    /**
     * 隐藏fragment，显示发现页面</br>
     */
    protected void showFindPage() {
        super.showFindPage();
        findViewById(ResFinder.getId("umeng_comm_find_base")).setVisibility(
                View.VISIBLE);
        findViewById(ResFinder.getId("container")).setVisibility(View.GONE);
    }

    /**
     * 显示朋友圈Fragment</br>
     */
    protected void showFriendsFragment() {

    }

    /**
     * 显示推荐用户fragment</br>
     */
    protected void showRecommendUserFragment() {

    }

    @Override
    protected void showNearByUser() {

    }


}
