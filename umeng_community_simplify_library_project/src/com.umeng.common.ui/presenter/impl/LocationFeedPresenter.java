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

import android.location.Location;
import android.util.Log;

import com.umeng.comm.core.beans.FeedItem;
import com.umeng.comm.core.nets.responses.FeedsResponse;
import com.umeng.common.ui.mvpview.MvpFeedView;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 用于获取周边Feed的Presenter
 */
public class LocationFeedPresenter extends FeedListPresenter {

    private Location mLocation;

    /**
     * @param feedViewInterface
     */
    public LocationFeedPresenter(MvpFeedView feedViewInterface, Location location) {
        super(feedViewInterface);
        this.mLocation = location;
    }

    @Override
    public void loadDataFromServer() {
        if (mLocation == null) {
            Log.d("", "####obtain location is null...");
            return;
        }
        mCommunitySDK.fetchNearByFeed(mLocation, mRefreshListener);
    }

    @Override
    protected void beforeDeliveryFeeds(FeedsResponse response) {
        isNeedRemoveOldFeeds.set(false);
    }

    @Override
    public void loadDataFromDB() {
    }

    private Comparator<FeedItem> mComparator = new Comparator<FeedItem>() {

        @Override
        public int compare(FeedItem lhs, FeedItem rhs) {
            return lhs.distance - rhs.distance;
        }
    };

    @Override
    protected Comparator<FeedItem> getFeedCompartator() {
        return mComparator;
    }
}
