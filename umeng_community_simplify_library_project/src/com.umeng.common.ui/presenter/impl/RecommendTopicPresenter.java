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


import com.umeng.comm.core.beans.Topic;
import com.umeng.comm.core.db.ctrl.impl.DatabaseAPI;
import com.umeng.common.ui.mvpview.MvpRecommendTopicView;

import java.util.List;

/**
 *
 */
public class RecommendTopicPresenter extends TopicBasePresenter {


    public RecommendTopicPresenter(MvpRecommendTopicView topicView) {
        super(topicView);
    }

    public RecommendTopicPresenter(MvpRecommendTopicView topicView, boolean isRegisterReceiver) {
        super(topicView, isRegisterReceiver);
    }

    @Override
    public void loadDataFromServer() {
        mCommunitySDK.fetchRecommendedTopics(mRefreshListener);
    }

    @Override
    protected void saveTopicToDB(boolean isRefresh, List<Topic> topics) {
        super.saveTopicToDB(isRefresh, topics);
        if (isRefresh) {
            mDatabaseAPI.getTopicDBAPI().deleteAllRecommendTopics();
        }
        DatabaseAPI.getInstance().getTopicDBAPI().saveRecommendTopicToDB(topics);
    }

    @Override
    public void loadDataFromDB() {
        mDatabaseAPI.getTopicDBAPI().loadRecommendTopicsFromDB(mDbFetchListener);
    }
}
