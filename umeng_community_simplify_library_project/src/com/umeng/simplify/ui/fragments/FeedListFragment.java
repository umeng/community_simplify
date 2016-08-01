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

package com.umeng.simplify.ui.fragments;

import android.content.Intent;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.umeng.comm.core.beans.CommUser;
import com.umeng.comm.core.beans.FeedItem;
import com.umeng.comm.core.beans.FeedItem.CATEGORY;
import com.umeng.comm.core.constants.Constants;
import com.umeng.comm.core.listeners.Listeners.OnItemViewClickListener;
import com.umeng.comm.core.utils.ResFinder;
import com.umeng.comm.core.utils.ToastMsg;
import com.umeng.common.ui.fragments.FeedListBaseFragment;
import com.umeng.common.ui.presenter.impl.FeedListPresenter;
import com.umeng.common.ui.util.BroadcastUtils;
import com.umeng.simplify.ui.activities.FeedDetailActivity;
import com.umeng.simplify.ui.activities.PostFeedActivity;
import com.umeng.simplify.ui.adapters.FeedAdapter;

import java.util.List;


/**
 * 这是Feed流列表页面，包含当前最新的消息列表.从该页面可以跳转到话题搜索页面、消息发布页面，可以浏览消息流中的图片、评论某项消息、进入某个好友的主页等.
 */
public abstract class FeedListFragment<P extends FeedListPresenter> extends FeedListBaseFragment<P, FeedAdapter> {

    private boolean isDisplayTopic = true;

    @Override
    protected int getFragmentLayout() {
        return ResFinder.getLayout("umeng_simplify_feeds_frgm_layout");
    }

    /**
     * 初始化feed流 页面显示相关View
     */
    protected void initViews() {
        // 初始化刷新相关View跟事件
        super.initViews();
        mPostBtn.setImageDrawable(ResFinder.getDrawable("umeng_simplify_new_post_btn_style"));
    }

    /**
     *
     */
    protected void showPostButtonWithAnim() {
    }

    protected void deleteInvalidateFeed(FeedItem feedItem) {
        // 将无效的feed从listview中删除
        mFeedLvAdapter.getDataSource().remove(feedItem);
        mFeedLvAdapter.notifyDataSetChanged();
    }

    protected void updateAfterDelete(FeedItem feedItem) {
        mFeedLvAdapter.getDataSource().remove(feedItem);
        mFeedLvAdapter.notifyDataSetChanged();
        // 发送删除广播
        BroadcastUtils.sendFeedDeleteBroadcast(getActivity(), feedItem);
    }

    /**
     * 加载更多数据</br>
     */
    protected void loadMoreFeed() {
        // 没有网络的情况下从数据库加载
        super.loadMoreFeed();
    }

    protected FeedAdapter createListViewAdapter() {
        return new FeedAdapter(getActivity());
    }

    /**
     * 初始化适配器
     */
    protected void initAdapter() {
        if (mFeedLvAdapter == null) {
            mFeedLvAdapter = createListViewAdapter();
            mFeedLvAdapter.setCommentClickListener(new OnItemViewClickListener<FeedItem>() {

                @Override
                public void onItemClick(int position, FeedItem item) {
//                    先进入feed详情页面，再弹出评论编辑键盘
                    Intent intent = new Intent(getActivity(), FeedDetailActivity.class);
                    intent.putExtra(Constants.TAG_FEED, item);
                    intent.putExtra(Constants.TAG_IS_COMMENT, true);
                    intent.putExtra(Constants.TAG_IS_SCROLL, true);
                    intent.putExtra(Constants.TAG_IS_DISPLAY_TOPIC, isDisplayTopic);
                    getActivity().startActivity(intent);

//                     如果评论数>0 ，则跳转到详情页面评论；否则直接显示输入法评论
//                    if (item.commentCount > 0) {
//                        Intent intent = new Intent(getActivity(), FeedDetailActivity.class);
//                        intent.putExtra(Constants.TAG_FEED, item);
//                        intent.putExtra(Constants.TAG_IS_SCROLL, true);
//                        getActivity().startActivity(intent);
//                        return;
//                    }
//                    mFeedItem = item;
//                    if (mCommentPresenter != null) {
//                        mCommentPresenter.setFeedItem(item);
//                    }
//                    showCommentLayout();
//                    addOnGlobalLayoutListener(position);
                }
            });

        }
        mRefreshLayout.setAdapter(mFeedLvAdapter);
        mFeedsListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final int realPosition = position - mFeedsListView.getHeaderViewsCount();
                final FeedItem feedItem = mFeedLvAdapter.getItem(realPosition < 0 ? 0
                        : realPosition);
                if (feedItem != null && (feedItem.status >= FeedItem.STATUS_SPAM && feedItem.status != FeedItem.STATUS_LOCK)
                        && feedItem.category == CATEGORY.FAVORITES) {
                    ToastMsg.showShortMsgByResName("umeng_comm_discuss_feed_spam_deleted");
                    return;
                }
                Intent intent = new Intent(getActivity(), FeedDetailActivity.class);
                intent.putExtra(Constants.TAG_FEED, feedItem);
                intent.putExtra(Constants.TAG_IS_DISPLAY_TOPIC, isDisplayTopic);
                startActivity(intent);
            }
        });
    }

    @Override
    public List<FeedItem> getBindDataSource() {
        return mFeedLvAdapter.getDataSource();
    }

    @Override
    public void notifyDataSetChanged() {
        mFeedLvAdapter.notifyDataSetChanged();
    }

    /**
     * 判断是否需要展示最热在推荐帖子界面
     *
     * @param isShow
     */
    protected void showHotView(boolean isShow) {
        if (mDaysView != null) {
            if (isShow) {
                mDaysView.setVisibility(View.VISIBLE);
            } else {
                mDaysView.setVisibility(View.GONE);
            }
        }
    }

    protected void setIsDisplayTopic(boolean isDisplayTopic) {
        this.isDisplayTopic = isDisplayTopic;
    }

    /**
     * 跳转至发送新鲜事页面</br>
     */
    @Override
    public void gotoPostFeedActivity() {
        super.gotoPostFeedActivity();
        Intent postIntent = new Intent(getActivity(), PostFeedActivity.class);
        startActivity(postIntent);
    }
}
