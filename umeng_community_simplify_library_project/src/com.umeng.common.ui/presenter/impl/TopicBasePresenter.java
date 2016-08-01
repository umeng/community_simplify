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

package com.umeng.common.ui.presenter.impl;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.TextUtils;
import android.widget.ToggleButton;

import com.umeng.comm.core.beans.CommConfig;
import com.umeng.comm.core.beans.CommUser;
import com.umeng.comm.core.beans.Topic;
import com.umeng.comm.core.constants.Constants;
import com.umeng.comm.core.constants.ErrorCode;
import com.umeng.comm.core.db.ctrl.impl.DatabaseAPI;
import com.umeng.comm.core.listeners.Listeners.FetchListener;
import com.umeng.comm.core.listeners.Listeners.SimpleFetchListener;
import com.umeng.comm.core.nets.Response;
import com.umeng.comm.core.nets.responses.LoginResponse;
import com.umeng.comm.core.nets.responses.TopicResponse;
import com.umeng.comm.core.nets.uitls.NetworkUtils;
import com.umeng.comm.core.utils.CommonUtils;
import com.umeng.comm.core.utils.ToastMsg;
import com.umeng.common.ui.mvpview.MvpRecommendTopicView;
import com.umeng.common.ui.presenter.BaseFragmentPresenter;
import com.umeng.common.ui.util.BroadcastUtils;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class TopicBasePresenter extends BaseFragmentPresenter<List<Topic>> {

    protected MvpRecommendTopicView mTopicView;

    private String mNextPageUrl = "";

    private boolean isRegisterReceiver = true;

    private boolean isGuestMode = true;

    public TopicBasePresenter(MvpRecommendTopicView topicView) {
        this.mTopicView = topicView;
    }

    public TopicBasePresenter(MvpRecommendTopicView topicView, boolean isRegisterReceiver) {
        this.mTopicView = topicView;
        this.isRegisterReceiver = isRegisterReceiver;
    }

    @Override
    public void attach(Context context) {
        super.attach(context);
        BroadcastUtils.registerTopicBroadcast(mContext, mReceiver);
        BroadcastUtils.registerUserBroadcast(mContext, mReceiver);
        if (isRegisterReceiver) {
            registerLoginSuccessBroadcast();
        }
        // 启动页面是判断是否登录，关注列表页
        if (!CommonUtils.isLogin(mContext)) {
            mTopicView.showLoginView();
        }else {
            mTopicView.hideLoginView();
        }
    }

    @Override
    public void detach() {
        BroadcastUtils.unRegisterBroadcast(mContext, mReceiver);
        if (isRegisterReceiver) {
            mContext.unregisterReceiver(mLoginReceiver);
        }
        super.detach();
    }

    protected final FetchListener<TopicResponse> mRefreshListener = new FetchListener<TopicResponse>() {
        @Override
        public void onStart() {
            mTopicView.onRefreshStart();
        }

        @Override
        public void onComplete(TopicResponse response) {
            // 根据response进行Toast
            if (NetworkUtils.handleResponseAll(response)) {
                if (response.errCode == ErrorCode.NO_ERROR && CommonUtils.isListEmpty(response.result)) {
                    mTopicView.getBindDataSource().clear();
                    mTopicView.notifyDataSetChanged();
                }
                mTopicView.onRefreshEnd(); // [注意]:不可移动，该方法的回调会决定是否显示空视图
                return;
            }
            dealNextPageUrl(response.nextPageUrl, true);

            List<Topic> results = response.result;
            fetchTopicComplete(results, true);

            dealGuestMode(response.isVisit);
            mTopicView.onRefreshEnd();
        }
    };

    protected final SimpleFetchListener<List<Topic>> mDbFetchListener = new SimpleFetchListener<List<Topic>>() {
        @Override
        public void onComplete(List<Topic> response) {
            if (mTopicView.getBindDataSource().isEmpty() && response != null) {
                mTopicView.getBindDataSource().addAll(response);
                mTopicView.notifyDataSetChanged();
                mTopicView.onRefreshEnd();
            }
        }
    };

    @Override
    public final void loadMoreData() {
        if (!isCanLoadMore()) {
            mTopicView.onRefreshEnd();
            return;
        }

        if (TextUtils.isEmpty(mNextPageUrl)) {
            mTopicView.onRefreshEnd();
            return;
        }
        mCommunitySDK.fetchNextPageData(mNextPageUrl, TopicResponse.class, mLoadMoreListener);
    }

    private final FetchListener<TopicResponse> mLoadMoreListener = new FetchListener<TopicResponse>() {
        @Override
        public void onStart() {
        }

        @Override
        public void onComplete(TopicResponse response) {
            mTopicView.onRefreshEnd();
            if (NetworkUtils.handleResponseAll(response)) {
                return;
            }
            final List<Topic> results = response.result;
            dealNextPageUrl(response.nextPageUrl, false);
            fetchTopicComplete(results, false);
        }
    };

    protected final void dealNextPageUrl(String url, boolean fromRefresh) {
        if (fromRefresh && TextUtils.isEmpty(mNextPageUrl)) {
            mNextPageUrl = url;
        } else if (!fromRefresh) {
            mNextPageUrl = url;
        }
    }

    protected final void fetchTopicComplete(List<Topic> topics, boolean isRefresh) {
        // 过滤已经存在的数据
        final List<Topic> newTopics = filterTopics(topics);
        if (newTopics != null && newTopics.size() > 0) {
            // 添加新话题
            List<Topic> dataSource = mTopicView.getBindDataSource();
            if (isRefresh) {
                dataSource.addAll(0, newTopics);
            } else {
                dataSource.addAll(newTopics);// 加载更多的数据追加到尾部
            }
            mTopicView.notifyDataSetChanged();
            saveTopicToDB(isRefresh, topics);
        }
    }

    /**
     * 移除重复的话题</br>
     *
     * @param dest 目标话题列表。
     * @return
     */
    protected List<Topic> filterTopics(List<Topic> dest) {
        List<Topic> src = mTopicView.getBindDataSource();
        src.removeAll(dest);
        return dest;
    }

    /**
     * 保存topic数据
     *
     * @param isRefresh
     * @param topics
     */
    protected void saveTopicToDB(boolean isRefresh, List<Topic> topics) {
        saveDataToDB(topics);
    }

    /**
     * 保存全部话题，登录用户关注的话题
     *
     * @param topics
     */
    @Override
    protected final void saveDataToDB(List<Topic> topics) {
        List<Topic> tempList = new ArrayList();
        for (Topic topicItem : topics) {
            if (topicItem.isFocused) {
                tempList.add(topicItem);
            }
        }
        CommUser user = CommConfig.getConfig().loginedUser;
        DatabaseAPI.getInstance().getTopicDBAPI().saveFollowedTopicsToDB(user.id, tempList);
    }


    protected BroadcastUtils.DefalutReceiver mReceiver = new BroadcastUtils.DefalutReceiver() {
        public void onReceiveTopic(Intent intent) {
            Topic topic = getTopic(intent);
            if (topic != null) {
                Topic originTopic = findTopicById(topic.id);
                originTopic.isFocused = topic.isFocused;
                mTopicView.notifyDataSetChanged();
            }
        }

        @Override
        public void onReceiveUser(Intent intent) {
            BroadcastUtils.BROADCAST_TYPE type = getType(intent);
            if (type == BroadcastUtils.BROADCAST_TYPE.TYPE_USER_LOGOUT) {
                afterUserLogout();
            }
        }
    };

    protected Topic findTopicById(String id) {
        List<Topic> dataSource = mTopicView.getBindDataSource();
        for (Topic topic : dataSource) {
            if (topic.id.equals(id)) {
                return topic;
            }
        }
        return new Topic();
    }

    /**
     * 关注某个话题</br>
     *
     * @param topic 话题的id
     */
    public void followTopic(final Topic topic, final ToggleButton toggleButton) {
        mCommunitySDK.followTopic(topic, new SimpleFetchListener<Response>() {

            @Override
            public void onComplete(Response response) {
                toggleButton.setClickable(true);
                if (NetworkUtils.handleResponseComm(response)) {
                    toggleButton.setChecked(false);
                    ToastMsg.showShortMsgByResName("umeng_comm_topic_follow_failed");
                    return;
                }
                if (response.errCode == ErrorCode.NO_ERROR) {
                    topic.isFocused = true;
                    topic.fansCount += 1;
                    mTopicView.notifyDataSetChanged();
                    // 存储到数据
                    updateTopicFollowedState(topic);
                    BroadcastUtils.sendTopicFollowBroadcast(mContext, topic);
                } else if (response.errCode == ErrorCode.ORIGIN_TOPIC_DELETE_ERR_CODE) {
                    // 在数据库中删除该话题并Toast
                    deleteTopic(topic);
                    ToastMsg.showShortMsgByResName("umeng_comm_topic_has_deleted");
                } else if (response.errCode == ErrorCode.ERROR_TOPIC_FOCUSED) {
                    ToastMsg.showShortMsgByResName("umeng_comm_topic_has_focused");
                    toggleButton.setChecked(true);
                } else {
                    toggleButton.setChecked(false);
                    ToastMsg.showShortMsgByResName("umeng_comm_topic_follow_failed");
                }
                toggleButton.setClickable(true);
            }
        });
    }

    /**
     * 取消关注某个话题</br>
     *
     * @param topic
     */
    public void cancelFollowTopic(final Topic topic, final ToggleButton toggleButton) {
        mCommunitySDK.cancelFollowTopic(topic,
                new SimpleFetchListener<Response>() {

                    @Override
                    public void onComplete(Response response) {
                        toggleButton.setClickable(true);
                        if (NetworkUtils.handleResponseComm(response)) {
                            ToastMsg.showShortMsgByResName("umeng_comm_topic_cancel_failed");
                            toggleButton.setChecked(true);
                            return;
                        }
                        if (response.errCode == ErrorCode.ORIGIN_TOPIC_DELETE_ERR_CODE) {
                            // 在数据库中删除该话题并Toast
                            deleteTopic(topic);
                            ToastMsg.showShortMsgByResName("umeng_comm__topic_has_deleted");
                            return;
                        }

                        if (response.errCode == ErrorCode.NO_ERROR) {
                            topic.isFocused = false;
                            topic.fansCount -= 1;
                            mTopicView.notifyDataSetChanged();
                            // 将该记录从数据库中移除
                            DatabaseAPI.getInstance().getTopicDBAPI().deleteFollowedTopicByTopicId(topic.id);
                            BroadcastUtils.sendTopicCancelFollowBroadcast(mContext, topic);
                            updateTopicFollowedState(topic);
                        } else if (response.errCode == ErrorCode.ERROR_TOPIC_NOT_FOCUSED) {
                            ToastMsg.showShortMsgByResName("umeng_comm_topic_has_not_focused");
                            toggleButton.setChecked(false);
                        } else {
                            toggleButton.setChecked(true);
                            ToastMsg.showShortMsgByResName("umeng_comm_topic_cancel_failed");
                        }
                    }
                });
    }

    /**
     * 检测是否登录并执行关注/取消关注操作</br>
     *
     * @param topic
     * @param toggleButton
     * @param isFollow
     */
    public void checkLoginAndExecuteOp(final Topic topic, final ToggleButton toggleButton,
                                       final boolean isFollow) {
        CommonUtils.checkLoginAndFireCallback(mContext, new SimpleFetchListener<LoginResponse>() {

            @Override
            public void onComplete(LoginResponse response) {
                if (response.errCode != ErrorCode.NO_ERROR) {
                    toggleButton.setChecked(!toggleButton.isChecked());
                    return;
                }
                if (isFollow) {
                    followTopic(topic, toggleButton);
                } else {
                    cancelFollowTopic(topic, toggleButton);
                }
            }
        });
    }

    private void updateTopicFollowedState(Topic topic) {
        String currentUid = CommConfig.getConfig().loginedUser.id;
        if (topic.isFocused) {
            mDatabaseAPI.getTopicDBAPI().saveFollowedTopicToDB(currentUid, topic);
        } else {
            mDatabaseAPI.getTopicDBAPI().deleteFollowedTopicByUid(currentUid);
        }
    }

    /**
     * 删除话题。包括删除关系表跟话题本身，以及从adapter中删除</br>
     *
     * @param topic
     */
    private void deleteTopic(Topic topic) {
        DatabaseAPI.getInstance().getTopicDBAPI().deleteTopicFromDB(topic.id);
        // 从adapter删除该条topic
        mTopicView.getBindDataSource().remove(topic);
        mTopicView.notifyDataSetChanged();
    }

    /**
     * 注册登录成功时的广播</br>
     */
    private void registerLoginSuccessBroadcast() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.ACTION_LOGIN_SUCCESS);
        mContext.registerReceiver(mLoginReceiver, filter);
    }

    private BroadcastReceiver mLoginReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            afterUserLogin();
        }
    };

    /**
     * 处理用户登录后的逻辑<br>
     */
    protected void afterUserLogin() {
        dealGuestMode(true);
        mTopicView.hideLoginView();
    }

    /**
     * 处理用户退出登录后的逻辑<br>
     */
    protected void afterUserLogout() {
        // 显示登录view
        mTopicView.showLoginView();
        updateFollowStateAfterLogout();
    }

    /**
     * 处理访客/非访客模式子类，刷新之后调用此方法，将访客/非访客模式通知到父类</br>
     *
     * @param guestMode
     */
    protected final void dealGuestMode(boolean guestMode) {
        this.isGuestMode = guestMode;
        if (isCanLoadMore()) {
            mTopicView.hideVisitView();
        } else {
            mTopicView.showVisitView();
        }
    }

    /**
     * 是否能够加载更多，访客/非访客模式</br>
     *
     * @return
     */
    protected final boolean isCanLoadMore() {
        // 访客模式或者已登录
        boolean isLogin = (mContext != null) && CommonUtils.isLogin(mContext);
        return isGuestMode || isLogin;
    }

    /**
     * 退出登录后，设置为非关注状态
     */
    private void updateFollowStateAfterLogout() {
        int size = mTopicView.getBindDataSource().size();
        for (int i = 0; i < size; i++) {
            mTopicView.getBindDataSource().get(i).isFocused = false;
        }
        mTopicView.notifyDataSetChanged();
    }

}
