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

package com.umeng.simplify.ui.dialogs;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.TextView;

import com.umeng.comm.core.beans.CommConfig;
import com.umeng.comm.core.beans.Topic;
import com.umeng.comm.core.listeners.Listeners.FetchListener;
import com.umeng.comm.core.nets.responses.TopicResponse;
import com.umeng.comm.core.nets.uitls.NetworkUtils;
import com.umeng.comm.core.utils.ResFinder;
import com.umeng.common.ui.dialogs.PickerDialog;
import com.umeng.common.ui.widgets.RefreshGvLayout;
import com.umeng.common.ui.widgets.RefreshLayout;
import com.umeng.simplify.ui.adapters.SelectTopicAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * 发布Feed时选择话题的Dialog
 */
public class SelectTopicDialog extends PickerDialog<Topic> {
    /**
     * 话题下一页url地址。每次从server获取列表时，都能够拿到该url，因此不cache到DB
     */
    private Drawable drawableLine;
    private TextView allBtn, focusBtn;

    private List<Topic> mFocusTopics = new ArrayList();
    private List<Topic> mAllTopics = new ArrayList();

    private String mFocusTopicsUrl;
    private String mAllTopicsUrl;

    private int mCurrentTab;
    private boolean isLoading;


    protected RefreshGvLayout mRefreshGvLayout;
    protected GridView gridView;
    public SelectTopicDialog(Context context) {
        this(context, 0);
        setContentView(this.createContentView());
    }

    public SelectTopicDialog(Context context, int theme) {
        super(context, theme);
        setContentView(this.createContentView());
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        loadDataFromServer();
    }
    protected View createContentView() {
        int layout = ResFinder.getLayout("umeng_simplify_topicpick_layout");
        int listViewResId = ResFinder.getId("umeng_comm_topicgrid");


        int backBtnResId = ResFinder.getId("topicpic_back");

        int refreshLayoutResId = ResFinder.getId("umeng_comm_at_friend_listview");

        mRootView = LayoutInflater.from(getContext()).inflate(
                layout, null, false);


        mRefreshGvLayout = (RefreshGvLayout) mRootView
                .findViewById(refreshLayoutResId);
        mRefreshGvLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

            @Override
            public void onRefresh() {
                loadDataFromServer();
            }
        });

        mRefreshGvLayout.setOnLoadListener(new RefreshLayout.OnLoadListener() {

            @Override
            public void onLoad() {
                loadMore();
            }
        });

        gridView = mRefreshGvLayout.findRefreshViewById(listViewResId);
        setupAdater();
        setupLvOnItemClickListener();


        mRootView.findViewById(backBtnResId).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDataListener != null) {
                    mDataListener.onComplete(mSelectedItem);
                }
                SelectTopicDialog.this.dismiss();
            }
        });


        return mRootView;
    }
    @Override
    public void onClick(View v) {
        if (mDataListener != null) {
            mDataListener.onComplete(mSelectedItem);
        }
        this.dismiss();
    }
    @Override
    protected void setupAdater() {
        mAdapter = new SelectTopicAdapter(getContext());
        gridView.setAdapter(mAdapter);
//        topicButtonGroup.setVisibility(View.VISIBLE);
        String title = ResFinder.getString("umeng_comm_topic");
//        mTitleTextView.setText(title);
//        gridView.setFooterDividersEnabled(true);
//        gridView.setOverscrollFooter(null);
    }

    @Override
    protected void setupLvOnItemClickListener() {
        gridView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                pickItemAtPosition(position);
            }
        });
    }

    @Override
    public void loadDataFromServer() {
        if(mCurrentTab == 0){
            loadAllDataFromServer();
        }else {
            loadFocusDataFromServer();
        }
    }

    public void loadAllDataFromServer() {
        if (isLoading) {
            return;
        }
        isLoading = true;
        mSdkImpl.fetchTopics(new FetchListener<TopicResponse>() {

            @Override
            public void onStart() {
                mRefreshGvLayout.setRefreshing(true);
            }

            @Override
            public void onComplete(TopicResponse response) {
                isLoading = false;
                mRefreshGvLayout.setRefreshing(false);
                if (NetworkUtils.handleResponseAll(response)) {
                    return;
                }
                mAllTopicsUrl = response.nextPageUrl;
                handleResultData(response.result, true, false);
            }
        });
    }

    public void loadFocusDataFromServer() {
        if (isLoading) {
            return;
        }
        isLoading = true;
        String uid = CommConfig.getConfig().loginedUser.id;
        mSdkImpl.fetchFollowedTopics(uid, new FetchListener<TopicResponse>() {

            @Override
            public void onStart() {
                mRefreshGvLayout.setRefreshing(true);
            }

            @Override
            public void onComplete(TopicResponse response) {
                isLoading = false;
                mRefreshGvLayout.setRefreshing(false);
                if (NetworkUtils.handleResponseAll(response)) {
                    return;
                }
                mFocusTopicsUrl = response.nextPageUrl;
                handleResultData(response.result, true, true);
            }
        });
    }

    @Override
    public void loadMore() {
        if (isLoading) {
            return;
        }
        if (mCurrentTab == 0) {
            loadMoreAllTopics();
        } else {
            loadMoreFocusTopics();
        }
    }

    private void loadMoreFocusTopics() {
        if (TextUtils.isEmpty(mFocusTopicsUrl)) {
            mRefreshGvLayout.setLoading(false);
            return;
        }
        isLoading = true;
        mSdkImpl.fetchNextPageData(mFocusTopicsUrl, TopicResponse.class,
                new FetchListener<TopicResponse>() {

                    @Override
                    public void onStart() {
                    }

                    @Override
                    public void onComplete(TopicResponse response) {
                        isLoading = false;
                        mRefreshGvLayout.setLoading(false);
                        if (NetworkUtils.handleResponseAll(response)) {
                            return;
                        }
                        mFocusTopicsUrl = response.nextPageUrl;
                        handleResultData(response.result, false, true);
                    }
                });
    }

    private void loadMoreAllTopics() {
        if (TextUtils.isEmpty(mAllTopicsUrl)) {
            mRefreshGvLayout.setLoading(false);
            return;
        }
        isLoading = true;
        mSdkImpl.fetchNextPageData(mAllTopicsUrl, TopicResponse.class,
                new FetchListener<TopicResponse>() {

                    @Override
                    public void onStart() {
                    }

                    @Override
                    public void onComplete(TopicResponse response) {
                        isLoading = false;
                        mRefreshGvLayout.setLoading(false);
                        if (NetworkUtils.handleResponseAll(response)) {
                            return;
                        }
                        mAllTopicsUrl = response.nextPageUrl;
                        handleResultData(response.result, false, false);
                    }
                });
    }

    @Override
    protected void pickItemAtPosition(int position) {
        super.pickItemAtPosition(position);
        mSelectedItem = null;
    }

    private void handleResultData(List<Topic> topics, boolean isRefresh, boolean isFocusTopics) {
        if (topics == null || topics.isEmpty()) {
            return;
        }

        if (isFocusTopics) {
            if (isRefresh) {
                mFocusTopics.clear();
            }
            mFocusTopics.addAll(topics);
        } else {
            if (isRefresh) {
                mAllTopics.clear();
            }
            mAllTopics.addAll(topics);
        }

        mAdapter.getDataSource().clear();
        if (mCurrentTab == 0) {
            mAdapter.addData(mAllTopics);
        } else {
            mAdapter.addData(mFocusTopics);
        }
    }
}
