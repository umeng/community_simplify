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

package com.umeng.simplify.ui.presenter.impl;

import android.text.TextUtils;

import com.umeng.comm.core.beans.Comment;
import com.umeng.comm.core.beans.FeedItem;
import com.umeng.comm.core.constants.ErrorCode;
import com.umeng.comm.core.listeners.Listeners.SimpleFetchListener;
import com.umeng.comm.core.nets.responses.CommentResponse;
import com.umeng.comm.core.nets.uitls.NetworkUtils;
import com.umeng.comm.core.utils.CommonUtils;
import com.umeng.comm.core.utils.ToastMsg;
import com.umeng.common.ui.mvpview.MvpCommentView;
import com.umeng.common.ui.mvpview.MvpFeedDetailView;
import com.umeng.common.ui.mvpview.MvpLikeView;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;


/**
 * Feed详情页的Presenter
 *
 * @author mrsimple
 */
public class FeedDetailPresenter extends com.umeng.common.ui.presenter.impl.FeedDetailPresenter {



    public FeedDetailPresenter(MvpFeedDetailView feedDetailView,
                               MvpLikeView likeView, MvpCommentView commentView, FeedItem feedItem) {
        super(feedDetailView, likeView, commentView, feedItem);

    }

    /**
     * 只看楼主评论列表</br>
     */
    public void loadCommentsByuserFromServer() {
            mCommunitySDK.fetchFeedCommentsByuser(mFeedItem.id, mFeedItem.creator.id, Comment.CommentOrder.DESC,
                    new SimpleFetchListener<CommentResponse>() {
                        @Override
                        public void onComplete(CommentResponse response) {
                            mCommentView.onRefreshEnd();
                            if (NetworkUtils.handleResponseComm(response)) {
                                mDetailView.showOwnerComment(false);
                                return;
                            }

                            if (response.errCode == ErrorCode.ERR_CODE_FEED_UNAVAILABLE) {
                                ToastMsg.showShortMsgByResName("umeng_comm_discuss_feed_unavailable");
                                mDetailView.showOwnerComment(false);
                                return;
                            }
                            if (response.errCode != ErrorCode.NO_ERROR) {
                                ToastMsg.showShortMsgByResName("umeng_comm_load_failed");
                                mDetailView.showOwnerComment(false);
                                return;
                            }
                            mNextPageUrl = response.nextPageUrl;
//                    if (TextUtils.isEmpty(mNextPageUrl) && mUpdateNextPageUrl.get()) {
//                        mNextPageUrl = response.nextPageUrl;
//                        mUpdateNextPageUrl.set(false);
//                    }
                            List<Comment> comments = response.result;
                            mFeedItem.comments.clear();
                            mFeedItem.comments.addAll(comments);
                            if (mFeedItem.commentCount == 0) {
                                mFeedItem.commentCount = mFeedItem.comments.size();
                            }
                            sortComments();
                            mCommentView.updateCommentView();
                            mDetailView.showOwnerComment(true);
                        }
                    });


    }
    /**
     * 获取feed的评论列表</br>
     */
    public void loadCommentsFromServer() {
        mCommunitySDK.fetchFeedComments(mFeedItem.id, Comment.CommentOrder.DESC,
                new SimpleFetchListener<CommentResponse>() {
                    @Override
                    public void onComplete(CommentResponse response) {
                        mCommentView.onRefreshEnd();
                        if (NetworkUtils.handleResponseComm(response)) {
                            mDetailView.showAllComment(false);
                            return;
                        }

                        if (response.errCode == ErrorCode.ERR_CODE_FEED_UNAVAILABLE) {
                            ToastMsg.showShortMsgByResName("umeng_comm_discuss_feed_unavailable");
                            mDetailView.showAllComment(false);
                            return;
                        }
                        if (response.errCode != ErrorCode.NO_ERROR) {
                            ToastMsg.showShortMsgByResName("umeng_comm_load_failed");
                            mDetailView.showAllComment(false);
                            return;
                        }
                        mNextPageUrl = response.nextPageUrl;
//                if (TextUtils.isEmpty(mNextPageUrl) && mUpdateNextPageUrl.get()) {
//                    mNextPageUrl = response.nextPageUrl;
//                    mUpdateNextPageUrl.set(false);
//                }
                        List<Comment> comments = response.result;
                        mFeedItem.comments.clear();
                        mFeedItem.comments.addAll(comments);
                        if (mFeedItem.commentCount == 0) {
                            mFeedItem.commentCount = mFeedItem.comments.size();
                        }
                        sortComments();
                        mCommentView.updateCommentView();
                        mDetailView.showAllComment(true);
                        saveCommentsToDB(response.result);

                    }
                });
    }

    public void loadMoreComments() {
        if (TextUtils.isEmpty(mNextPageUrl)) {
            mCommentView.onRefreshEnd();
            return;
        }

        mCommunitySDK.fetchNextPageData(mNextPageUrl, CommentResponse.class,
                new SimpleFetchListener<CommentResponse>() {

                    @Override
                    public void onComplete(CommentResponse response) {
                        if (NetworkUtils.handleResponseComm(response)) {
                            return;
                        }
                        if (response.errCode == ErrorCode.NO_ERROR) {
                            mNextPageUrl = response.nextPageUrl;
                            mCommentView.loadMoreComment(response.result);
                            saveCommentsToDB(response.result);
                        } else {
                            ToastMsg.showShortMsgByResName("umeng_comm_request_failed");
                        }
                        mCommentView.onRefreshEnd();
                    }
                });
    }

    /**
     * 保存评论数据到数据库</br>
     *
     * @param comments
     */
    private void saveCommentsToDB(List<Comment> comments) {
        if (CommonUtils.isListEmpty(comments)) {
            return;
        }
        mDatabaseAPI.getFeedDBAPI().saveFeedToDB(mFeedItem); // 由于comment数据变化，此时需要级联更新对应的feed数据
        mDatabaseAPI.getCommentAPI().saveCommentsToDB(mFeedItem);

    }

    protected void sortComments() {
        Collections.sort(mFeedItem.comments, mComparator);
    }

    protected Comparator<Comment> mComparator = new Comparator<Comment>() {

        @Override
        public int compare(Comment lhs, Comment rhs) {
            if (rhs != null && rhs.createTime != null && lhs != null && lhs.createTime != null) {
                return rhs.createTime.compareTo(lhs.createTime);
            }
            return (lhs == null || lhs.createTime == null) ? -1 : 1;
        }
    };
}
