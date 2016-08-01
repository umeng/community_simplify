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

import android.content.Context;

import com.umeng.comm.core.beans.Topic;
import com.umeng.comm.core.db.ctrl.impl.DatabaseAPI;
import com.umeng.comm.core.listeners.Listeners.FetchListener;
import com.umeng.comm.core.nets.responses.TopicResponse;
import com.umeng.comm.core.nets.uitls.NetworkUtils;
import com.umeng.comm.core.utils.CommonUtils;
import com.umeng.common.ui.mvpview.MvpRecommendTopicView;
import com.umeng.common.ui.util.BroadcastUtils;

import java.util.List;

/**
 *
 */
public class TopicFgPresenter extends TopicBasePresenter {

    public TopicFgPresenter(MvpRecommendTopicView recommendTopicView) {
        super(recommendTopicView);
    }

    @Override
    public void attach(Context context) {
        super.attach(context);
        BroadcastUtils.registerTopicBroadcast(context, mReceiver);
    }

    @Override
    public void loadDataFromServer() {
        mCommunitySDK.fetchTopics(mRefreshListener);
    }

    private FetchListener<TopicResponse> mRefreshListener = new FetchListener<TopicResponse>() {

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
            final List<Topic> results = response.result;
            dealNextPageUrl(response.nextPageUrl, true);
            fetchTopicComplete(results, true);
            dealGuestMode(response.isVisit);
            mTopicView.onRefreshEnd();
        }
    };

    @Override
    public void loadDataFromDB() {
        mDatabaseAPI.getTopicDBAPI().loadTopicsFromDB(mDbFetchListener);
    }

    @Override
    protected void saveTopicToDB(boolean isRefresh, List<Topic> topics) {
        super.saveTopicToDB(isRefresh, topics);
        DatabaseAPI.getInstance().getTopicDBAPI().saveTopicsToDB(topics);
    }

    @Override
    protected void afterUserLogin() {
        super.afterUserLogin();
        loadDataFromServer();
    }
}
