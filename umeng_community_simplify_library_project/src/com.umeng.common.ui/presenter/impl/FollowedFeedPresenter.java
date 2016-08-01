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

import com.umeng.comm.core.beans.FeedItem;
import com.umeng.common.ui.mvpview.MvpFeedView;

import java.util.List;

/**
 * 关注的Feed Presenter</br>
 */
public class FollowedFeedPresenter extends FeedListPresenter {

    /**
     * @param view
     */
    public FollowedFeedPresenter(MvpFeedView view) {
        super(view);
    }

    public FollowedFeedPresenter(MvpFeedView view, boolean isRegisterReceiver) {
        super(view, isRegisterReceiver);
    }

    @Override
    public void loadDataFromServer() {
//        mCommunitySDK.fetchLastestFeeds(mRefreshListener);
        mCommunitySDK.fetchMyFollowedFeeds(mRefreshListener);
    }

    @Override
    public void loadDataFromDB() {
        mDatabaseAPI.getFeedDBAPI().loadFollowedFeeds(false, mDbFetchListener);
    }

    @Override
    protected void saveDataToDB(List<FeedItem> newFeedItems) {
        mDatabaseAPI.getFeedDBAPI().saveFollowedFeeds(false, newFeedItems);
    }

    @Override
    protected void fetchDataFromServerByLogin() {
//        super.fetchDataFromServerByLogin();
        mCommunitySDK.fetchMyFollowedFeeds(mLoginRefreshListener);
    }

    @Override
    protected void afterUserLogout() {
        super.afterUserLogout();
        // 清空feed列表数据
        mFeedView.getBindDataSource().clear();
        mFeedView.notifyDataSetChanged();
    }

    @Override
    public boolean isAddToFeedList() {
        return true;
    }
}
