package com.umeng.common.ui.presenter.impl;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.umeng.comm.core.beans.CommConfig;
import com.umeng.comm.core.beans.Topic;
import com.umeng.comm.core.constants.Constants;
import com.umeng.comm.core.listeners.Listeners;
import com.umeng.comm.core.nets.responses.TopicResponse;
import com.umeng.comm.core.nets.uitls.NetworkUtils;
import com.umeng.comm.core.utils.CommonUtils;
import com.umeng.common.ui.mvpview.MvpRecommendTopicView;

import java.util.List;

/**
 * Created by wangfei on 15/12/1.
 */
public class FocusTopicFgPresenter extends TopicFgPresenter {


//    private boolean mIsAttach;

    public FocusTopicFgPresenter(MvpRecommendTopicView recommendTopicView) {
        super(recommendTopicView);
    }

    @Override
    public void attach(Context context) {
        super.attach(context);
//        if (!mIsAttach) {
//            registerLoginSuccessBroadcast();
//            mIsAttach = true;
//        }
    }

    @Override
    public void detach() {
//        if (mIsAttach) {
//            mContext.unregisterReceiver(mLoginReceiver);
//        }
//        mIsAttach = false;
        super.detach();
    }

//    /**
//     * 注册登录成功时的广播</br>
//     */
//    private void registerLoginSuccessBroadcast() {
//        IntentFilter filter = new IntentFilter();
//        filter.addAction(Constants.ACTION_LOGIN_SUCCESS);
//        mContext.registerReceiver(mLoginReceiver, filter);
//    }

//    private BroadcastReceiver mLoginReceiver = new BroadcastReceiver() {
//
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            loadDataFromServer();
//        }
//    };

    @Override
    public void loadDataFromServer() {
        if (CommonUtils.isLogin(mContext)) {
            mCommunitySDK.fetchFollowedTopics(CommConfig.getConfig().loginedUser.id, new Listeners.
                    FetchListener<TopicResponse>() {

                @Override
                public void onStart() {
                    mTopicView.onRefreshStart();
                }

                @Override
                public void onComplete(final TopicResponse response) {
                    // 根据response进行Toast
                    if (NetworkUtils.handleResponseAll(response)) {
                        //  如果是网络错误，其结果可能快于DB查询
                        if (CommonUtils.isNetworkErr(response.errCode)) {
                            mTopicView.onRefreshEndNoOP();
                        } else {
                            mTopicView.onRefreshEnd();
                        }
                        return;
                    }
//                    mDatabaseAPI.getTopicDBAPI().deleteAllTopics();
                    final List<Topic> results = response.result;
                    dealNextPageUrl(response.nextPageUrl, true);
                    fetchTopicComplete(results, true);
                    dealGuestMode(response.isVisit);
                    mTopicView.onRefreshEnd();
                }
            });
        }
    }

    @Override
    protected void saveTopicToDB( boolean isRefresh, List<Topic> topics) {
        String loginedUid = CommConfig.getConfig().loginedUser.id;
        if(isRefresh){
            mDatabaseAPI.getTopicDBAPI().deleteFollowedTopicByUid(loginedUid);
        }
        mDatabaseAPI.getTopicDBAPI().saveFollowedTopicsToDB(loginedUid, topics);
    }

    @Override
    public void loadDataFromDB() {
        mDatabaseAPI.getTopicDBAPI().loadTopicsFromDB(CommConfig.getConfig().loginedUser.id, mDbFetchListener);
    }

    @Override
    protected void afterUserLogout() {
        super.afterUserLogout();
        // 关注话题页面，退出登录后需要清空列表数据，否则切换用户会展示上个用户的数据
        mTopicView.getBindDataSource().clear();
        mTopicView.notifyDataSetChanged();
    }

    //    @Override
//    protected void afterUserLogin() {
//        super.afterUserLogin();
//        loadDataFromServer();
//    }
}
