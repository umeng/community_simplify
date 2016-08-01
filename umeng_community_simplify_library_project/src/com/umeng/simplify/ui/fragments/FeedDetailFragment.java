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
 * The above copyright notice and this permission notice shall be included in˛
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

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.umeng.comm.core.beans.CommUser;
import com.umeng.comm.core.beans.Comment;
import com.umeng.comm.core.beans.FeedItem;
import com.umeng.comm.core.beans.Like;
import com.umeng.comm.core.constants.Constants;
import com.umeng.comm.core.constants.HttpProtocol;
import com.umeng.comm.core.listener.Listeners.LoginOnViewClickListener;
import com.umeng.comm.core.receiver.BaseBroadcastReceiver;
import com.umeng.comm.core.utils.CommonUtils;
import com.umeng.comm.core.utils.ResFinder;
import com.umeng.comm.core.utils.ToastMsg;
import com.umeng.common.ui.activities.BaseFragmentActivity;
import com.umeng.simplify.ui.presenter.impl.CommentPresenter;
import com.umeng.common.ui.widgets.SoftListenerView;
import com.umeng.simplify.ui.adapters.FeedCommentAdapter;
import com.umeng.simplify.ui.adapters.viewholders.FeedDetailHeaderViewHolder;
import com.umeng.common.ui.fragments.CommentEditFragment;
import com.umeng.common.ui.mvpview.MvpCommentView;
import com.umeng.common.ui.mvpview.MvpFeedDetailView;
import com.umeng.common.ui.mvpview.MvpLikeView;
import com.umeng.simplify.ui.presenter.impl.FeedDetailPresenter;
import com.umeng.common.ui.util.BroadcastUtils;
import com.umeng.common.ui.util.ViewFinder;
import com.umeng.common.ui.widgets.RefreshLayout;
import com.umeng.common.ui.widgets.RefreshLvLayout;

import java.util.List;


/**
 * 该类是某条Feed的详情页面,使用FeedItemViewParser解析单项的Feed数据.Feed相关的信息显示在评论列表的Header中,
 * 该页面会展示该条Feed的Like用户以及评论。
 */
public class FeedDetailFragment extends CommentEditFragment<List<FeedItem>, FeedDetailPresenter>
        implements MvpCommentView, MvpLikeView, MvpFeedDetailView, FeedCommentAdapter.OnCommentItemClickListener {


    private FeedDetailHeaderViewHolder mFeedViewHolder;

    // 由于开发者可能直接使用Fragment，在退出登录的时候，我们需要回到该Activity
    protected String mContainerClass = null;

    private TextView mLikeTextView;
    private View mLikeView;

    private SoftListenerView mRootView;

    /**
     * 页面下部的Like、评论、转发三个视图的根布局
     */
    View mActionsLayout;

    private View mActionBtnContainer;
    private View mLikeBtn;
    private View mSendBtn;
//    private View mFavoriteBtn;
    private EditText mInputView;

    private boolean mSoftKeyBoardState;

    /**
     * 下拉刷新布局,评论ListView的parent
     */
    RefreshLvLayout mRefreshLayout;
    /**
     * 评论ListView
     */
    private ListView mCommentListView;
    /**
     * 评论ListView Adapter
     */
    FeedCommentAdapter mCommentAdapter;

    private LoginReceiver mLoginReceiver;

    /**
     * 是否弹出评论编辑键盘
     */
    public boolean mIsShowComment;

    /**
     * 创建一个FeedDetailFragment对象，在feedDetailActivity使用
     *
     * @param feedItem
     * @return
     */

//    private List<Comment> mSentComments = new LinkedList<Comment>();

    public static FeedDetailFragment newFeedDetailFragment(FeedItem feedItem) {
        FeedDetailFragment fragment = new FeedDetailFragment();
        fragment.mFeedItem = feedItem;
        return fragment;
    }

    @Override
    protected int getFragmentLayout() {
        return ResFinder.getLayout("umeng_simplify_feed_detail_fragment");
    }

    @Override
    protected FeedDetailPresenter createPresenters() {
        mCommentPresenter = new CommentPresenter(this, mFeedItem);
        mCommentPresenter.attach(getActivity());

        FeedDetailPresenter presenter = new FeedDetailPresenter(this, this, this, mFeedItem);
        presenter.setLoadLikeDataIsEnable(false);
        return presenter;
    }

    @Override
    protected void initWidgets() {
        super.initWidgets();

        View headerView = initFeedContentView();
        initRefreshLayout(headerView);
        initActionsView();
        mRootView = findViewById(ResFinder.getId("umeng_comm_feed_detail_root"));
        mRootView.setSoftListener(new SoftListenerView.SoftListener() {
            @Override
            public void onSoftChange(SoftListenerView.SoftState softState, int softHeight) {
                switch (softState) {
                    case HIDE:
                        mSoftKeyBoardState = false;
                        if(!TextUtils.isEmpty(mInputView.getText().toString())){
                            return;
                        }
                        mRootView.post(new Runnable() {
                            @Override
                            public void run() {
                                hideSendBtn();
                            }
                        });
                        clearCommentData();
                        break;

                    case SHOW:
                        mSoftKeyBoardState = true;
                        break;
                }
            }
        });

        mActionBtnContainer = findViewById(ResFinder.getId("umeng_simplify_btn_container"));
        mSendBtn = findViewById(ResFinder.getId("umeng_simplify_send_btn"));
        mSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String content = mInputView.getText().toString();
                if (!checkCommentData(content)) {
                    return;
                }
                postComment(content);
                clearCommentData();
            }
        });

//        mFavoriteBtn = findViewById(ResFinder.getId("umeng_simplify_favorite_btn"));

        mLikeBtn = findViewById(ResFinder.getId("umeng_simplify_like_btn"));
        mLikeBtn.setSelected(mFeedItem.isLiked);
        mLikeBtn.setOnClickListener(
                new LoginOnViewClickListener() {
                    @Override
                    protected void doAfterLogin(View v) {
                        mPresenter.setFeedItem(mFeedItem);
                        if (mFeedItem.isLiked) {
                            mPresenter.postUnlike(mFeedItem.id);
                        } else {
                            mPresenter.postLike(mFeedItem.id);
                        }
                    }
                });

        mInputView = findViewById(ResFinder.getId("umeng_comm_reply_comment"));
        mInputView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSendBtn();
            }
        });

        mInputView.requestFocus();
        mInputView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    showSendBtn();
                }
            }
        });

        mInputView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // 用于处理粘贴的逻辑，粘贴不会弹起输入法，需要改变按钮状态
                if(!TextUtils.isEmpty(mInputView.getText().toString())){
                    showSendBtn();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


//        findViewById(ResFinder.getId("umeng_comm_reply_comment")).setOnClickListener(
//                new LoginOnViewClickListener() {
//
//                    @Override
//                    protected void doAfterLogin(View v) {
//                        showCommentLayout();
//                    }
//                });
//        findViewById(ResFinder.getId("umeng_comm_reply_comment")).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
////                showCommentLayout();
//            }
//        });


        BroadcastUtils.registerFeedBroadcast(getActivity(), mReceiver);
        mLoginReceiver = new LoginReceiver();
        getActivity().registerReceiver(mLoginReceiver, new IntentFilter(Constants.ACTION_LOGIN_SUCCESS));
    }

    @Override
    protected void setupOthers() {
        mContainerClass = getActivity().getClass().getName();
        // 填充数据
        setFeedItemData(mFeedItem);
    }

    public void updateFeedItem(FeedItem feedItem) {
        if (mFeedItem != null && feedItem != null) {
            mFeedItem.title = feedItem.title;
            mFeedItem.text = feedItem.text;
            mFeedItem.publishTime = feedItem.publishTime;
            mFeedItem.likeCount = feedItem.likeCount;
            mFeedItem.commentCount = feedItem.commentCount;
            mFeedItem.forwardCount = feedItem.forwardCount;
            mFeedItem.imageUrls = feedItem.imageUrls;
            mFeedItem.category = feedItem.category;
            mFeedItem.isRecommended = feedItem.isRecommended;
            mFeedItem.isLiked = feedItem.isLiked;
            mFeedItem.isCollected = feedItem.isCollected;
            mFeedItem.isTop = feedItem.isTop;
            mFeedItem.ban_user = feedItem.ban_user;
            mFeedItem.tag = feedItem.tag;
            mFeedItem.permission = feedItem.permission;
            mFeedItem.creator.medals = feedItem.creator.medals;
        } else {
            mFeedItem = feedItem;
        }
        setFeedItemData(mFeedItem);
    }

    @Override
    public void onStart() {
        super.onStart();
        boolean isScroll = getArguments().getBoolean(Constants.TAG_IS_SCROLL, false);
        if (!isScroll) {
            return;
        }
        if (mIsShowComment) {
//            showCommentLayout();
            showInputView();
        }
    }

    /**
     * 加载Feed详情页面的Feed视图，该视图添加到评论列表的Header中
     *
     * @return
     */
    private View initFeedContentView() {
        ViewFinder headerViewFinder = new ViewFinder(getActivity(),
                ResFinder.getLayout("umeng_simplify_feed_detail_header"));

        // 显示Feed的Header View
        View headerView = headerViewFinder.findViewById(ResFinder
                .getId("umeng_comm_content_layout"));
        // 构造FeedItemViewHolder用于对Feed进行控制
        mFeedViewHolder = new FeedDetailHeaderViewHolder(getActivity(), headerView);
        mFeedViewHolder.setFeedItem(mFeedItem);
        boolean isDisplayTopic = getArguments().getBoolean(Constants.TAG_IS_DISPLAY_TOPIC, true);
        mFeedViewHolder.setIsDisplayTopic(isDisplayTopic);

        View feedTextView = headerViewFinder.findViewById(ResFinder.getId("umeng_comm_msg_text"));
        if(feedTextView != null){
            feedTextView.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if(event.getAction() == MotionEvent.ACTION_DOWN && mSoftKeyBoardState){
                        ((BaseFragmentActivity) getActivity()).hideInputMethod(mInputView);
                    }
                    return false;
                }
            });
        }

        final View feedTextWeb = headerViewFinder.findViewById(ResFinder.getId("umeng_comm_msg_textweb"));
        if(feedTextWeb != null){
            feedTextWeb.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if(event.getAction() == MotionEvent.ACTION_DOWN && mSoftKeyBoardState){
                        ((BaseFragmentActivity) getActivity()).hideInputMethod(mInputView);
                    }
                    return false;
                }
            });
        }

        // Like数量TextView
        mLikeTextView = headerViewFinder.findViewById(ResFinder.getId("umeng_comm_like_count_tv"));
        mLikeView = headerViewFinder.findViewById(ResFinder.getId("umeng_comm_like_btn"));
        mLikeView.setSelected(mFeedItem.isLiked);
        mLikeView.setOnClickListener(
                new LoginOnViewClickListener() {
                    @Override
                    protected void doAfterLogin(View v) {
                        mPresenter.setFeedItem(mFeedItem);
                        if (mFeedItem.isLiked) {
                            mPresenter.postUnlike(mFeedItem.id);
                        } else {
                            mPresenter.postLike(mFeedItem.id);
                        }
                    }
                });

//        mCommentCountTextView = headerViewFinder.findViewById(ResFinder
//                .getId("umeng_comm_comment_count_tv"));
//        headerViewFinder.findViewById(ResFinder.getId("umeng_comm_comment_btn")).setOnClickListener(
//                new LoginOnViewClickListener() {
//
//                    @Override
//                    protected void doAfterLogin(View v) {
//                        showCommentLayout();
//                    }
//                });
        return headerViewFinder.getRootView();
    }

    private void initRefreshLayout(View headerView) {
        mRefreshLayout = mViewFinder.findViewById(ResFinder
                .getId("umeng_comm_feed_refresh_layout"));

        mRefreshLayout.setRefreshing(false);
        mRefreshLayout.setEnabled(false);
        // 添加footer
        if (mFeedItem.commentCount > Constants.COUNT) {
            mRefreshLayout.setDefaultFooterView();
        }
        mRefreshLayout.setOnRefreshListener(new OnRefreshListener() {

            @Override
            public void onRefresh() {
                mRefreshLayout.setRefreshing(false);
            }
        });
        mRefreshLayout.setOnLoadListener(new RefreshLayout.OnLoadListener() {
            @Override
            public void onLoad() {
                mPresenter.loadMoreComments();
            }
        });

        headerView.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN && mSoftKeyBoardState){
                    ((BaseFragmentActivity) getActivity()).hideInputMethod(mInputView);
                }
                return false;
            }
        });


        mCommentListView = mViewFinder.findViewById(ResFinder.getId("umeng_comm_comments_list"));
        mCommentListView.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN && mSoftKeyBoardState){
                    ((BaseFragmentActivity) getActivity()).hideInputMethod(mInputView);
                }
                return false;
            }
        });

//        mCommentListView.setOnItemClickListener(new Listeners.OnItemClickLoginListener() {
//
//            @Override
//            protected void doAfterLogin(View v, int position) {
//                if (position == 0) {
//                    return;
//                }
//                final int realPosition = position - 1;
//                Comment comment = mCommentAdapter.getItem(realPosition);
//                mCommentPresenter.clickCommentItem(realPosition, comment);
//            }
//        });

        // 添加header
        mCommentListView.addHeaderView(headerView, null, false);
    }

    private void showCommentLayoutWitdCommentId() {
        // 从消息通知的评论页面进来该页面时需要弹出评论框
        if (mFeedItem.extraData.containsKey(HttpProtocol.COMMENT_ID_KEY)) {
            String commentId = mFeedItem.extraData.getString(HttpProtocol.COMMENT_ID_KEY);
            if (TextUtils.isEmpty(commentId)) {
                return;
            }
            int position = getCommentPosition(commentId);
            if (position >= 0) {
                mFeedItem.extraData.remove(HttpProtocol.COMMENT_ID_KEY);
                final Comment item = mCommentAdapter.getItem(position);
//                if (item.creator.id.equals(CommConfig.getConfig().loginedUser.id)) {
//                    ToastMsg.showShortMsgByResName("umeng_comm_do_not_reply_yourself");
//                } else {
                showCommentLayout(position, item);
//                }
            }
        }
    }

    private int getCommentPosition(String commentId) {
        List<Comment> feedComments = mFeedItem.comments;
        for (int i = 0; i < feedComments.size(); i++) {
            final Comment item = feedComments.get(i);
            if (commentId.equals(item.id)) {
                return i;
            }
        }
        return -1;
    }

    private void initActionsView() {
        mActionsLayout = mViewFinder.findViewById(ResFinder.getId("umeng_comm_action_layout"));

    }

    private void setupLikeView(boolean isLiked) {
        mLikeView.setSelected(isLiked);
        mLikeBtn.setSelected(isLiked);
    }

    @Override
    public void showCommentLayout(int realPosition, Comment comment) {
        String hintString = getReplyPrefix(comment.creator);
        mReplyUser = comment.creator;
        mReplyCommentId = comment.id;
        mInputView.setHint(hintString);
        showInputView();
    }

    private void showSendBtn(){
        mActionBtnContainer.setVisibility(View.GONE);
        mSendBtn.setVisibility(View.VISIBLE);
    }

    private void hideSendBtn(){
        mSendBtn.setVisibility(View.GONE);
        mActionBtnContainer.setVisibility(View.VISIBLE);
    }

    private void showInputView(){
        mInputView.requestFocus();
        showSendBtn();
        ((BaseFragmentActivity) getActivity()).showInputMethod(mInputView);
    }

    @Override
    protected void showCommentLayout() {}

    @Override
    protected void hideCommentLayout() {}

    @Override
    protected void clearCommentData() {
        mReplyUser = null;
        mReplyCommentId = "";
        mInputView.setText("");
        mInputView.setHint(ResFinder.getString("umeng_simplify_comment_hint"));
        ((BaseFragmentActivity) getActivity()).hideInputMethod(mInputView);
    }

    /**
     * 填充消息流ListView每项的数据
     */
    private void setFeedItemData(final FeedItem feedItem) {
        if (TextUtils.isEmpty(feedItem.id)) {
            return;
        }
        // 设置Feed Content View
        mFeedViewHolder.setFeedItem(feedItem);
        setLikeCount(feedItem.likeCount);
        setCommentCount(feedItem.commentCount);
        setupLikeView(feedItem.isLiked);
        // 初始化评论列表
        initCommentListView(feedItem);
    }

    private void setCommentCount(int count) {
        count = count < 0 ? 0 : count;
        mFeedItem.commentCount = count;
    }

    private void setLikeCount(int count) {
        count = count < 0 ? 0 : count;
        mFeedItem.likeCount = count;
        mLikeTextView.setText(String.valueOf(CommonUtils.getLimitedCount(count)));
    }

    private void initCommentListView(FeedItem item) {
        mCommentAdapter = new FeedCommentAdapter(getActivity());
        mCommentAdapter.addDatasOnly(item.comments);
        mCommentAdapter.setCommentItemClickListener(this);
//        mCommentAdapter.setPostOwnerId(mFeedItem.creator.id);
        mCommentListView.setAdapter(mCommentAdapter);
    }

    /**
     * 获取显示在EditText中显示的评论文本。不如：回复XXX</br>
     *
     * @return hint
     */
    private String getReplyPrefix(CommUser replyUser) {
        if (replyUser == null) {
            return "";
        }
        String replyText = ResFinder.getString("umeng_comm_reply");
        String colon = ResFinder.getString("umeng_comm_colon");
        return replyText + replyUser.name + colon;
    }

    @Override
    public void postCommentSuccess(Comment comment, CommUser replyUser) {
        super.postCommentSuccess(comment, replyUser);
        mReplyCommentId = "";
        mReplyUser = null;
        if (mCommentAdapter == null) {
            return;
        }
        mInputView.setText("");
        mInputView.setHint(ResFinder.getString("umeng_simplify_comment_hint"));

        if(!mSoftKeyBoardState){
            hideSendBtn();
        }

        comment.replyUser = replyUser;
        mCommentAdapter.addToFirst(comment);
        mCommentListView.setSelection(mCommentListView.getHeaderViewsCount());

        // 存储数据
        mFeedItem.comments.add(0, comment);
        setCommentCount(mFeedItem.commentCount);

//        mSentComments.add(mSentComments.size(), comment);
//        mFeedItem.comments.add(mFeedItem.comments.size(), comment);
//        mCommentAdapter.getDataSource().add(mCommentAdapter.getCount(), comment);
//        mCommentAdapter.notifyDataSetChanged();
//        mCommentListView.setSelection(mCommentListView.getBottom());
//
//        setCommentCount(mFeedItem.commentCount);
    }

    @Override
    public void loadMoreComment(List<Comment> comments) {
        mRefreshLayout.setLoading(false);
//        appendComments(comments);
        comments.removeAll(mCommentAdapter.getDataSource());
        mCommentAdapter.addData(comments);
        // 存储数据
        mFeedItem.comments.addAll(comments);
    }

    @Override
    public void loadMoreLike(List<Like> likes) {

    }

//    public void appendComments(List<Comment> comments) {
//        List<Comment> mRepeatComment = new ArrayList<Comment>();
//        for (int i = 0; i < mSentComments.size(); i++) {
//            Comment tempComment = mSentComments.get(i);
//            if (comments.contains(tempComment)) {
//                mRepeatComment.add(tempComment);
//            }
//        }
//        // 下一页的数据中不含有已发送的评论，直接移除后拼接
//        if (mRepeatComment.isEmpty()) {
//            for (int i = 0; i < mSentComments.size(); i++) {
//                Comment tempComment = mSentComments.get(i);
//                mCommentAdapter.getDataSource().remove(tempComment);
//                mFeedItem.comments.remove(tempComment);
//            }
//        } else {
//            // 下一页数据中含义已发送的评论，去除重复数据后拼接
//            for (int i = 0; i < mRepeatComment.size(); i++) {
//                Comment tempComment = mRepeatComment.get(i);
//                mCommentAdapter.getDataSource().remove(tempComment);
//                mFeedItem.comments.remove(tempComment);
//                mSentComments.remove(tempComment);
//            }
//        }
//        comments.addAll(mSentComments);
//    }


    @Override
    public void onRefreshEnd() {
        mRefreshLayout.setLoading(false);
        mRefreshLayout.setRefreshing(false);
    }

    @Override
    public void fetchLikesComplete(String nexturl) {
    }

    @Override
    public void fetchCommentsComplete() {
        mCommentAdapter.updateListViewData(mFeedItem.comments);
    }

    @Override
    public void like(String id, boolean isLiked) {
        setupLikeView(isLiked);
    }

    @Override
    public void updateLikeView(String nextUrl) {
        setLikeCount(mFeedItem.likeCount);
    }

    @Override
    public void updateCommentView() {
        mCommentAdapter.updateListViewData(mFeedItem.comments);
        mCommentAdapter.notifyDataSetChanged();
        showCommentLayoutWitdCommentId();
    }

    @Override
    public void onCommentDeleted(Comment comment) {
        mCommentAdapter.removeItem(comment);
        setCommentCount(mFeedItem.commentCount);
        BroadcastUtils.sendFeedUpdateBroadcast(getActivity(), mFeedItem);
    }

    @Override
    public void onDestroy() {
        BroadcastUtils.unRegisterBroadcast(getActivity(), mReceiver);
        getActivity().unregisterReceiver(mLoginReceiver);
        super.onDestroy();
    }

    private BroadcastUtils.DefalutReceiver mReceiver = new BroadcastUtils.DefalutReceiver() {
        public void onReceiveFeed(Intent intent) {
            FeedItem feedItem = getFeed(intent);
            if (feedItem == null) {
                return;
            }
            BroadcastUtils.BROADCAST_TYPE type = getType(intent);
            if (BroadcastUtils.BROADCAST_TYPE.TYPE_FEED_POST == type) {
                updateForwardCount(feedItem, 1);
            } else if (BroadcastUtils.BROADCAST_TYPE.TYPE_FEED_DELETE == type) {
                updateForwardCount(feedItem, -1);
            }
        }
    };

    private void updateForwardCount(FeedItem item, int count) {
        if (TextUtils.isEmpty(item.sourceFeedId)) {
            return;
        }
        mFeedItem.forwardCount = mFeedItem.forwardCount + count;
    }

    @Override
    public void onRefreshStart() {
    }

    private class LoginReceiver extends BaseBroadcastReceiver {
        @Override
        protected void onReceiveIntent(Context context, Intent intent) {
            if (Constants.ACTION_LOGIN_SUCCESS.equals(intent.getAction())) {
                mPresenter.loadCommentsFromServer();
            }
        }
    }

    @Override
    public void onReplyCommentClick(final int position) {
        if(!TextUtils.isEmpty(mInputView.getText().toString())){
            ToastMsg.showShortMsgByResName("umeng_simplify_comment_tips");
            return;
        }
        this.showCommentLayout(position, mCommentAdapter.getItem(position));
        mCommentListView.setSelection(position + mCommentListView.getHeaderViewsCount());
    }

    @Override
    public void onMoreBtnClick(int position) {
//        if (position == 0) {
//            return;
//        }
//        final int realPosition = position;
        if(mSoftKeyBoardState){
            ((BaseFragmentActivity) getActivity()).hideInputMethod(mInputView);
            return;
        }
        Comment comment = mCommentAdapter.getItem(position);
        mCommentPresenter.clickCommentItem(position, comment);
    }

    @Override
    public void showAllComment(boolean result) {
    }

    @Override
    public void showOwnerComment(boolean result) {
    }

    public boolean getSoftKeyBoardState(){
        return mSoftKeyBoardState;
    }

    public View getInputView(){
        return mInputView;
    }

}
