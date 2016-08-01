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

import android.content.Intent;
import android.text.TextUtils;

import com.umeng.comm.core.beans.CommConfig;
import com.umeng.comm.core.beans.CommUser;
import com.umeng.comm.core.beans.Comment;
import com.umeng.comm.core.beans.FeedItem;
import com.umeng.comm.core.beans.Like;
import com.umeng.comm.core.utils.CommonUtils;
import com.umeng.common.ui.mvpview.MvpFeedView;
import com.umeng.common.ui.util.BroadcastUtils;
import com.umeng.common.ui.util.Filter;

import java.util.List;

/**
 * Feed列表相关的Presenter，该Presenter从网络、数据库中读取Feed，如果数据是从网络上获取的，那么需要将数据存储到数据库中。
 * 在获取数据后会通过MvpFeedView的{@link MvpFeedView#getBindDataSource()}
 * 函数获取到列表的数据集,然后对新获取的数据进行去重、排序，再将新获取到的数据添加到列表的数据集合中，最后调用
 * {@link MvpFeedView#notifyDataSetChanged()} 函数更新对应的列表视图。
 *
 * @author mrsimple
 */
public class FeedListPresenter extends BaseFeedListPresenter implements Filter<FeedItem> {

    /**
     * 当前登录的用户
     */
    private CommUser mUser = CommConfig.getConfig().loginedUser;

    public FeedListPresenter(MvpFeedView view) {
        super(view);
    }

    public FeedListPresenter(MvpFeedView view, boolean isRegisterReceiver) {
        super(view, isRegisterReceiver);
    }

    /**
     * 设置当前presenter对应的用户，默认为登录用户</br>
     *
     * @param user
     */
    public void setCommUser(CommUser user) {
        mUser = user;
    }

    @Override
    protected void registerBroadcast() {
        super.registerBroadcast();
        // registerLogout Receiver
        BroadcastUtils.registerUserBroadcast(mContext, mReceiver);
        BroadcastUtils.registerFeedBroadcast(mContext, mReceiver);
        BroadcastUtils.registerFeedUpdateBroadcast(mContext, mReceiver);
    }

    @Override
    protected void unRegisterBroadcast() {
        super.unRegisterBroadcast();
        BroadcastUtils.unRegisterBroadcast(mContext, mReceiver);
    }

    /**
     * 数据同步处理，包括用户更新，发送feed，删除feed，收藏feed，更新feed</br>
     */
    protected BroadcastUtils.DefalutReceiver mReceiver = new BroadcastUtils.DefalutReceiver() {
        public void onReceiveUser(Intent intent) {
            BroadcastUtils.BROADCAST_TYPE type = getType(intent);
            CommUser user = getUser(intent);
            if (type == BroadcastUtils.BROADCAST_TYPE.TYPE_USER_UPDATE) {// 更新用户信息
                updatedUserInfo(user);
                return;
            }
            if (!isMyFeedList()) {// 如果不是登录用户，则不remove feed
                return;
            }

            if (type == BroadcastUtils.BROADCAST_TYPE.TYPE_USER_CANCEL_FOLLOW) {
                // 预留一个hook函数,当取消对某个用户的关注，移除主页上该用户的feed。其他页面不进行操作
                onCancelFollowUser(user);
            }
        }

        public void onReceiveFeed(Intent intent) {// 发送or删除时
            FeedItem feedItem = getFeed(intent);
            if (feedItem == null) {
                return;
            }
            BroadcastUtils.BROADCAST_TYPE type = getType(intent);
            if (BroadcastUtils.BROADCAST_TYPE.TYPE_FEED_POST == type) {
                postFeedComplete(feedItem);
            } else if (BroadcastUtils.BROADCAST_TYPE.TYPE_FEED_DELETE == type) {
                deleteFeedComplete(feedItem);
            } else if (BroadcastUtils.BROADCAST_TYPE.TYPE_FEED_FAVOURITE == type) {
                dealFavourite(feedItem);
            }
            mFeedView.notifyDataSetChanged();
        }

        // 更新Feed的相关数据。包括like、comment、forward数量修改
        public void onReceiveUpdateFeed(Intent intent) {
            if (mFeedView != null) {
                FeedItem item = getFeed(intent);
                List<FeedItem> items = mFeedView.getBindDataSource();
                for (FeedItem feed : items) {
                    if (feed.id.equals(item.id)) {
                        // feed = item;
                        feed.isLiked = item.isLiked;
                        feed.likeCount = item.likeCount;
                        feed.likes = item.likes;
                        feed.commentCount = item.commentCount;
                        feed.comments = item.comments;
                        feed.forwardCount = item.forwardCount;
                        feed.isCollected = item.isCollected;
                        feed.category = item.category;
                        break;
                    }
                }
                // 此处不可直接调用adapter.notifyDataSetChanged，其他地方在notifyDataSetChanged（）方法中又逻辑处理
                mFeedView.notifyDataSetChanged();
            }
        }
    };

    /**
     * 更新feed的用户信息</br>
     *
     * @param user
     */
    private void updatedUserInfo(CommUser user) {
        if (mUser.equals(user)) {
            mUser = user;
        }
        List<FeedItem> feedItems = mFeedView.getBindDataSource();
        for (FeedItem feed : feedItems) {
            updateFeedContent(feed, user);
        }
        mFeedView.notifyDataSetChanged();
    }

    /**
     * 用户信息修改以后更新feed的用户信息
     *
     * @param user
     */
    private void updateFeedContent(FeedItem feed, CommUser user) {
        if (isMyFeed(feed)) {
            feed.creator = user;
        }
        // 更新like的创建者信息
        updateLikeCreator(feed.likes, user);
        // 更新评论信息
        updateCommentCreator(feed.comments, user);
        // 更新at好友的creator
        updateAtFriendCreator(feed.atFriends, user);
        // 转发类型的feed
        if (feed.sourceFeed != null) {
            updateFeedContent(feed.sourceFeed, user);
        }
    }

    /**
     * 判断该Feed是否来源于特定用户</br>
     *
     * @param feedItem
     * @return
     */
    private boolean isMyFeed(FeedItem feedItem) {
        CommUser user = CommConfig.getConfig().loginedUser;
        if (user == null || TextUtils.isEmpty(user.id)) {
            return false;
        }
        return feedItem.creator.id.equals(user.id);
    }

    /**
     * 更新赞的creator</br>
     *
     * @param likes
     * @param user
     */
    private void updateLikeCreator(List<Like> likes, CommUser user) {
        for (Like likeItem : likes) {
            if (likeItem.creator.id.equals(user.id)) {
                likeItem.creator = user;
            }
        }
    }

    /**
     * 更新评论的creator</br>
     *
     * @param comments
     * @param user
     */
    private void updateCommentCreator(List<Comment> comments, CommUser user) {
        for (Comment commentItem : comments) {
            if (commentItem.creator.id.equals(user.id)) {
                commentItem.creator = user;
            }
        }
    }

    /**
     * 更新at的creator</br>
     *
     * @param friends
     * @param user
     */
    private void updateAtFriendCreator(List<CommUser> friends, CommUser user) {
        for (CommUser item : friends) {
            if (item.id.equals(user.id)) {
                item = user;
            }
        }
    }

    /**
     * hook mothed
     * 取消用户关注时触发
     *
     * @param user
     */
    protected void onCancelFollowUser(CommUser user) {
    }

    /**
     * hook mothed
     * 处理收藏逻辑
     *
     * @param feedItem
     */
    protected void dealFavourite(FeedItem feedItem) {
    }

    /**
     * 判断是不是当前登录用户的feed列表
     *
     * @return
     */
    protected final boolean isMyFeedList() {
        return CommonUtils.isMyself(mUser);
    }

    // TODO 此处对于invalidate需要重构
    private void postFeedComplete(FeedItem feedItem) {
        if (isAddToFeedList()) {
            mFeedView.getBindDataSource().add(feedItem);
            sortFeedItems(mFeedView.getBindDataSource());
            mFeedView.notifyDataSetChanged();
            mFeedView.scrollToTop();
        }
        updateForwardCount(feedItem, 1);
    }

    /**
     * feed发送成功之后，是否添加到列表中</br>
     *
     * @return
     */
    protected boolean isAddToFeedList() {
        return false;
    }

    /**
     * 处理删除feed的逻辑（不区分列表）
     *
     * @param feedItem
     */
    private void deleteFeedComplete(FeedItem feedItem) {
        if (isReomveFeedOnDeleteComplete()) {
            mFeedView.getBindDataSource().remove(feedItem);
        }

        int len = mFeedView.getBindDataSource().size();
        for (int i = 0; i < len; i++) {
            FeedItem item = mFeedView.getBindDataSource().get(i);
            if (!isReomveFeedOnDeleteComplete()) {
                if (item.id.equals(feedItem.id)) { // 如果feed没有移除，则将feed状态设置为已删除的状态
                    item.status = FeedItem.STATUS_DELETE;
                }
                item.sourceFeed = null;
                item.sourceFeedId = null;
            }

            if (item.sourceFeed != null && item.sourceFeed.id.equals(feedItem.id)) {
                item.sourceFeed.status = FeedItem.STATUS_DELETE;
            }
        }
        mFeedView.notifyDataSetChanged();
        updateForwardCount(feedItem, -1);
    }

    protected boolean isReomveFeedOnDeleteComplete() {
        return true;
    }

    /**
     * 更新转发数</br>
     *
     * @param item
     */
    private void updateForwardCount(FeedItem item, int count) {
        if (TextUtils.isEmpty(item.sourceFeedId)) {
            return;
        }
        List<FeedItem> items = mFeedView.getBindDataSource();
        for (FeedItem feedItem : items) {
            if (feedItem.id.equals(item.sourceFeedId)) {
                feedItem.forwardCount = feedItem.forwardCount + count;
                mFeedView.notifyDataSetChanged();
                break;
            }
        }
    }
}
