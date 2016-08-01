package com.umeng.simplify.ui.presenter.impl;


import com.umeng.comm.core.beans.FeedItem;
import com.umeng.comm.core.db.ctrl.impl.DatabaseAPI;
import com.umeng.comm.core.listeners.Listeners;
import com.umeng.comm.core.nets.responses.FeedsResponse;
import com.umeng.comm.core.nets.uitls.NetworkUtils;
import com.umeng.common.ui.mvpview.MvpFeedView;
import com.umeng.common.ui.presenter.impl.FeedListPresenter;

import java.util.Comparator;
import java.util.List;

/**
 * Created by umeng on 12/1/15.
 */
public class HottestFeedPresenter extends FeedListPresenter {

    private int HOT_DAYS = 1;

    public HottestFeedPresenter(MvpFeedView view) {
        super(view);
    }

    public HottestFeedPresenter(MvpFeedView view, boolean isRegisterReceiver) {
        super(view, isRegisterReceiver);
    }

    @Override
    protected void saveDataToDB(List<FeedItem> newFeedItems) {
        if (isShowTopFeeds) {
            mDatabaseAPI.getFeedDBAPI().saveHotFeedToDB(HOT_DAYS, newFeedItems);
        } else {
            mDatabaseAPI.getFeedDBAPI().saveHotFeedToDBExcludeTop(HOT_DAYS, newFeedItems);
        }
    }

    @Override
    public void loadDataFromDB() {
        if (isShowTopFeeds) {
            DatabaseAPI.getInstance().getFeedDBAPI().loadHotFeeds(HOT_DAYS,
                    new Listeners.SimpleFetchListener<List<FeedItem>>() {
                        @Override
                        public void onComplete(List<FeedItem> response) {
                            mFeedView.clearListView();
                            mDbFetchListener.onComplete(response);
                        }
                    });
        } else {
            DatabaseAPI.getInstance().getFeedDBAPI().loadHotFeedsExcludeTop(HOT_DAYS,
                    new Listeners.SimpleFetchListener<List<FeedItem>>() {
                        @Override
                        public void onComplete(List<FeedItem> response) {
                            mFeedView.clearListView();
                            mDbFetchListener.onComplete(response);
                        }
                    });
        }
    }

    @Override
    public void loadDataFromServer() {
        setIsNeedRemoveOldFeeds();
//        mCommunitySDK.fetchTopFeeds(new Listeners.FetchListener<FeedsResponse>() {
//            @Override
//            public void onStart() {
//                mFeedView.onRefreshStart();
//                if(mTopFeeds != null){
//                    mTopFeeds.clear();
//                }
//            }
//
//            @Override
//            public void onComplete(FeedsResponse response) {
//                if (response.errCode == ErrorCode.NO_ERROR) {
//                    mTopFeeds = response.result;
//                    for (int i = 0; i < mTopFeeds.size(); i++) {
//                        mTopFeeds.get(i).isTop = 1;
//                    }
//                }
//                loadHotFeed();
//            }
//        });
        loadHotFeed();
    }

    private void loadHotFeed() {
        mCommunitySDK.fetchHotestFeeds(new Listeners.FetchListener<FeedsResponse>() {
            @Override
            public void onStart() {

            }

            @Override
            public void onComplete(FeedsResponse response) {
                // 根据response进行Toast
                if (NetworkUtils.handleResponseAll(response) && (mTopFeeds == null || mTopFeeds.isEmpty())) {
                    mFeedView.onRefreshEnd();
                    return;
                }

                mFeedView.clearListView();
                mNextPageUrl = response.nextPageUrl;

                final List<FeedItem> newFeedItems = response.result;

                beforeDeliveryFeeds(response);

                addTopFeedToHeader(newFeedItems);
                // 更新数据
                appendFeedItemsToHeader(newFeedItems);

                // 保存加载的数据。如果该数据存在于DB中，则替换成最新的，否则Insert一条新纪录
                saveDataToDB(newFeedItems);
                dealGuestMode(response.isVisit);
                mFeedView.onRefreshEnd();
            }
        }, 0);
    }


    @Override
    protected void fetchDataFromServerByLogin() {
    }

    @Override
    protected Comparator<FeedItem> getFeedCompartator() {
        return null;
    }
}
