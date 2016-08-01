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

package com.umeng.simplify.ui.adapters.viewholders;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.util.ArrayMap;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewStub;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

import com.umeng.comm.core.beans.CommUser;
import com.umeng.comm.core.beans.FeedItem;
import com.umeng.comm.core.beans.ImageItem;
import com.umeng.comm.core.beans.Like;
import com.umeng.comm.core.beans.Topic;
import com.umeng.comm.core.constants.Constants;
import com.umeng.comm.core.imageloader.ImgDisplayOption;
import com.umeng.comm.core.listeners.Listeners;
import com.umeng.comm.core.listeners.Listeners.OnResultListener;
import com.umeng.comm.core.utils.CommonUtils;
import com.umeng.comm.core.utils.ResFinder;
import com.umeng.comm.core.utils.TimeUtils;
import com.umeng.comm.core.utils.ToastMsg;
import com.umeng.common.ui.adapters.viewholders.ViewHolder;
import com.umeng.common.ui.emoji.EmojiTextView;
import com.umeng.common.ui.listener.FrinendClickSpanListener;
import com.umeng.common.ui.listener.TopicClickSpanListener;
import com.umeng.common.ui.mvpview.MvpLikeView;
import com.umeng.common.ui.presenter.impl.LikePresenter;
import com.umeng.common.ui.util.FeedViewRender;
import com.umeng.common.ui.util.ViewFinder;
import com.umeng.common.ui.widgets.NetworkImageView;
import com.umeng.common.ui.widgets.WrapperGridView;
import com.umeng.simplify.ui.activities.FeedDetailActivity;
import com.umeng.simplify.ui.activities.TopicDetailActivity;
import com.umeng.simplify.ui.activities.UserInfoActivity;
import com.umeng.simplify.ui.adapters.FeedImageAdapter;
import com.umeng.simplify.ui.presenter.impl.FeedContentPresenter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * zuhshi
 * ListView的Feed Item View解析器. ( 将试图的显示和解析解耦, 便于测试, 也便于复用. )
 */
public class FeedItemViewHolder extends ViewHolder implements MvpLikeView {

    public View mFeedTypeImgView;
    public View mFeedTopImgView;
    public View mFeedItemAnnounce;

    public EmojiTextView mFeedTextTv;
    public ViewStub mImageGvViewStub;
    public WrapperGridView mImageGv;
    public TextView mTopicTextTv;

    public NetworkImageView mProfileImgView;
    public NetworkImageView mUserMedalImgView;
    public TextView mUserNameTv;

    public TextView mLikeCountTextView;
    public TextView mCommentCountTextView;

    private View mLikeBtn;
    private View mCommentBtn;

    public TextView mTimeTv;


    protected FeedItem mFeedItem;

    protected FeedContentPresenter mPresenter;
    private LikePresenter mLikePresenter;

    private Listeners.OnItemViewClickListener mItemViewClickListener;

    private ArrayMap<String, FeedItem> mCacheArrayMap;

    private boolean isDisplayTopic = true;

    /**
     *
     *
     */
    public FeedItemViewHolder() {
    }

    public FeedItemViewHolder(ArrayMap arrayMap) {
        this.mCacheArrayMap = arrayMap;
    }

    public FeedItemViewHolder(Context context, View rootView) {
        mContext = context;
        itemView = rootView;
        mViewFinder = new ViewFinder(rootView);
        initWidgets();
    }

    @Override
    protected int getItemLayout() {
        return ResFinder.getLayout("umeng_simplify_feed_lv_item");
    }

    @Override
    protected void initWidgets() {
        inflateFromXML();
        initPresenters();
    }

    protected void initPresenters() {
        mPresenter = new FeedContentPresenter();
        mPresenter.attach(mContext);
        mLikePresenter = new LikePresenter(this);
        mLikePresenter.attach(mContext);
        mLikePresenter.setFeedItem(mFeedItem);
    }

    public boolean setupImageGridView() {
        if (mFeedItem.imageUrls != null && mFeedItem.imageUrls.size() > 0) {
            showImageGridView();
            return true;
        } else {
            hideImageGridView();
            return false;
        }
    }

    private void showImageGridView() {
        if (mImageGvViewStub.getVisibility() == View.GONE) {
            mImageGvViewStub.setVisibility(View.VISIBLE);
            int imageGvResId = ResFinder.getId("umeng_comm_msg_gridview");
            mImageGv = findViewById(imageGvResId);
            mImageGv.hasScrollBar = true;
            mImageGv.setHorizontalSpacing(CommonUtils.dip2px(mContext, 5));
            mImageGv.setVerticalSpacing(CommonUtils.dip2px(mContext, 5));
        }

        mImageGv.setBackgroundColor(Color.TRANSPARENT);
        mImageGv.setVisibility(View.VISIBLE);
        // adapter
        FeedImageAdapter gridviewAdapter = new FeedImageAdapter(mContext);
        List<ImageItem> mImgs = new ArrayList<ImageItem>();
        int imgSize = mFeedItem.imageUrls.size();
        for (int i = 0; i < 3; i++) {
            if (i < imgSize) {
                mImgs.add(mFeedItem.imageUrls.get(i));
            } else {
                mImgs.add(new ImageItem());
            }
        }
        gridviewAdapter.addDatasOnly(mImgs);
        mImageGv.setAdapter(gridviewAdapter);
        mImageGv.setNumColumns(3);
        // 图片GridView
        mImageGv.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
                if (pos < mFeedItem.imageUrls.size()) {
                    mPresenter.jumpToImageBrowser(mFeedItem.imageUrls, pos);
                } else {
                    gotoFeedDetailActivity();
                }
            }
        });
    }

    private void hideImageGridView() {
        if (mImageGv != null) {
            mImageGv.setAdapter(new FeedImageAdapter(mContext));
            mImageGv.setVisibility(View.GONE);
//            mImageGvViewStub.setVisibility(View.GONE);
        }
    }

    protected void inflateFromXML() {
        int feedTypeResId = ResFinder.getId("feed_type_img_btn");
        int textResId = ResFinder.getId("umeng_comm_feed_title");
        int userNameResId = ResFinder.getId("umeng_comm_msg_user_name");
        int timeResId = ResFinder.getId("umeng_comm_msg_time_tv");
        int gvStubResId = ResFinder.getId("umeng_comm_msg_images_gv_viewstub");

        int userProfileResId = ResFinder.getId("user_portrait_img_btn");
        int userMedalResId = ResFinder.getId("user_comm_user_medal_img");

        int feedTopicResId = ResFinder.getId("umeng_comm_feed_item_topic");

        mProfileImgView = findViewById(userProfileResId);
        mUserMedalImgView = findViewById(userMedalResId);

        mTopicTextTv = findViewById(feedTopicResId);

        // 公告或者好友feed的图标
        mFeedTypeImgView = findViewById(feedTypeResId);
        // 标题
        mFeedTextTv = findViewById(textResId);
        // 发布该消息的昵称
        mUserNameTv = findViewById(userNameResId);

        // 更新时间
        mTimeTv = findViewById(timeResId);

        mFeedTopImgView = findViewById(ResFinder.getId("feed_top_img_btn"));

        /**
         * 九宫格图片的View Stub
         */
        mImageGvViewStub = findViewById(gvStubResId);
        mLikeCountTextView = findViewById(ResFinder.getId("umeng_comm_like_tv"));
        mCommentCountTextView = findViewById(ResFinder.getId("umeng_comm_comment_tv"));

        mLikeBtn = findViewById(ResFinder.getId("umeng_comm_like_btn"));
        mCommentBtn = findViewById(ResFinder.getId("umeng_comm_comment_btn"));

        mFeedItemAnnounce = findViewById(ResFinder.getId("umeng_comm_feed_item_announce"));
    }

    /**
     * 填充消息流ListView每项的数据
     */
    protected void bindFeedItemData() {
        if (TextUtils.isEmpty(mFeedItem.id)) {
            return;
        }
        // 设置基础信息
        setBaseFeeditemInfo();
        // 设置图片
        boolean hasImg = setupImageGridView();
        showActionBar(hasImg);
    }

    /**
     * 设置feedItem的基本信息（头像，昵称，内容、位置）</br>
     */
    protected void setBaseFeeditemInfo() {
        setTypeIcon();

        setupUserIcon(mProfileImgView, mFeedItem.creator);

        // 勋章
        String medalUrl = null;
        if (mFeedItem.creator.medals != null && !mFeedItem.creator.medals.isEmpty()) {
            medalUrl = mFeedItem.creator.medals.get(0).imgUrl;
        }

        if (TextUtils.isEmpty(medalUrl) || "null".equals(medalUrl)) {
            mUserMedalImgView.setVisibility(View.GONE);
        } else {
            mUserMedalImgView.setVisibility(View.VISIBLE);
            // 由于view复用，首先要清空之前显示的img
            mUserMedalImgView.setImageResource(0);
            mUserMedalImgView.setImageUrl(medalUrl);
        }

        // 昵称
        mUserNameTv.setText(mFeedItem.creator.name);
        // 更新时间
        Date date = new Date(Long.parseLong(mFeedItem.publishTime));
        mTimeTv.setText(TimeUtils.format(date));
        // feed的文本内容
//        FeedViewRender.parseTopicsAndFriends(mFeedTextTv, mFeedItem);

        if (isDeleted()) {
            mFeedTextTv.setText(ResFinder.getString("umeng_comm_feed_deleted"));
        } else {
            if (mFeedItem.text.equals("null") || TextUtils.isEmpty(mFeedItem.text)) {
//            mFeedTextTv.setText(ResFinder.getString("umeng_comm_content_detail"));
                mFeedTextTv.setVisibility(View.GONE);
            } else {
                mFeedTextTv.setVisibility(View.VISIBLE);
                FeedItem temp = buildTempFeed();
                FeedViewRender.parseTopicsAndFriends(mFeedTextTv, temp, new TopicClickSpanListener() {
                    @Override
                    public void onClick(Topic topic) {
                        Intent intent = new Intent(mContext,
                                TopicDetailActivity.class);
                        intent.putExtra(Constants.TAG_TOPIC, topic);
                        mContext.startActivity(intent);
                    }
                }, new FrinendClickSpanListener() {
                    @Override
                    public void onClick(CommUser user) {
                        Intent intent = new Intent(mContext,
                                UserInfoActivity.class);
                        intent.putExtra(Constants.TAG_USER, user);
                        mContext.startActivity(intent);
                    }
                });
            }
        }

        mFeedTextTv.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                gotoFeedDetailActivity();
            }
        });

        String topicName = "";
        if (mFeedItem.topics != null && !mFeedItem.topics.isEmpty()) {
            topicName = mFeedItem.topics.get(0).name;
        }
        if (TextUtils.isEmpty(topicName) || !isDisplayTopic) {
            mTopicTextTv.setVisibility(View.GONE);
        } else {
            mTopicTextTv.setVisibility(View.VISIBLE);
            mTopicTextTv.setText(topicName);
            mTopicTextTv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, TopicDetailActivity.class);
                    intent.putExtra(Constants.TAG_TOPIC, mFeedItem.topics.get(0));
                    mContext.startActivity(intent);
                }
            });
        }
    }

    private void gotoFeedDetailActivity() {
        if (isDeleted()) {
            ToastMsg.showShortMsgByResName("umeng_comm_feed_spam_deleted");
            return;
        }
        Intent intent = new Intent(mContext, FeedDetailActivity.class);
        intent.putExtra(Constants.TAG_FEED, mFeedItem);
        intent.putExtra(Constants.TAG_IS_DISPLAY_TOPIC, isDisplayTopic);
        mContext.startActivity(intent);
    }


    private boolean isDeleted() {
        return (mFeedItem != null && (mFeedItem.status >= FeedItem.STATUS_SPAM
                && mFeedItem.status != FeedItem.STATUS_LOCK)
                && mFeedItem.category == FeedItem.CATEGORY.FAVORITES);
    }

    /* 1.去除空格：replace('\\s','') s.replace('\n','');*/
    private String replaceBlank(String text) {
        return text.replaceAll("\n", "");
    }

    private FeedItem buildTempFeed() {
        FeedItem temp = null;
        if (mCacheArrayMap != null) {
            temp = mCacheArrayMap.get(mFeedItem.id);
        }
        if (temp == null) {
            temp = new FeedItem();
//            temp.topics = mFeedItem.topics;
//            temp.atFriends = mFeedItem.atFriends;
            temp.text = replaceBlank(mFeedItem.text);
            if (mCacheArrayMap != null) {
                mCacheArrayMap.put(mFeedItem.id, temp);
            }
        }
        return temp;
    }

    private void showActionBar(boolean hasImg) {
        mLikeCountTextView.setSelected(mFeedItem.isLiked);
        mLikeCountTextView.setText(CommonUtils.getLimitedCount(mFeedItem.likeCount));
        mCommentCountTextView.setText(CommonUtils.getLimitedCount(mFeedItem.commentCount));
        like(mFeedItem.id, mFeedItem.isLiked);
        mLikeBtn.setOnClickListener(new com.umeng.comm.core.listener.Listeners.LoginOnViewClickListener() {
            @Override
            protected void doAfterLogin(View v) {
                mLikePresenter.setFeedItem(mFeedItem);
                if (mFeedItem.isLiked) {
                    mLikePresenter.postUnlike(mFeedItem.id);
                } else {
                    mLikePresenter.postLike(mFeedItem.id);
                }
            }
        });
    }

    /**
     * 设置feed 类型的icon</br>
     */
    protected void setTypeIcon() {
        if (mFeedItem.isTop == 1) {
            mFeedTopImgView.setVisibility(View.VISIBLE);
        } else {
            mFeedTopImgView.setVisibility(View.GONE);
        }

        if (mFeedItem.tag == 1) {
            mFeedTypeImgView.setVisibility(View.VISIBLE);
        } else {
            mFeedTypeImgView.setVisibility(View.GONE);
        }

        if (mFeedItemAnnounce != null) {
            if (mFeedItem.type == FeedItem.ANNOUNCEMENT_FEED) {
                mFeedItemAnnounce.setVisibility(View.VISIBLE);
            } else {
                mFeedItemAnnounce.setVisibility(View.GONE);
            }
        }
    }

    /**
     * 设置用户头像
     *
     * @param userIconImageView 用户头像的SquareImageView
     * @param user              用户头像的url
     */
    private void setupUserIcon(final NetworkImageView userIconImageView,
                               final CommUser user) {
        if (user == null || userIconImageView == null) {
            return;
        }

        ImgDisplayOption option = ImgDisplayOption.getOptionByGender(user.gender);
        userIconImageView.setImageUrl(user.iconUrl, option);
        userIconImageView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // 跳转用户中心前检查是否登录
                mPresenter.gotoUserInfoActivity(user);
            }
        });
    }

    public void setFeedItem(FeedItem feedItem) {
        mFeedItem = feedItem;
        mPresenter.setFeedItem(mFeedItem);
        bindFeedItemData();
    }

    public FeedItem getFeedItem() {
        return mFeedItem;
    }

    public void setOnUpdateListener(OnResultListener listener) {
        mLikePresenter.setResultListener(listener);
    }

    @Override
    public void like(String id, boolean isLiked) {
        if(!mFeedItem.id.equals(id)){
            return;
        }
        mFeedItem.isLiked = isLiked;
        mLikeCountTextView.setSelected(mFeedItem.isLiked);
    }

    @Override
    public void updateLikeView(String nextUrl) {
        mLikeCountTextView.setText(CommonUtils.getLimitedCount(mFeedItem.likeCount));
    }

    @Override
    public void loadMoreLike(List<Like> likes) {

    }

    public void setOnItemViewClickListener(final int position, final Listeners.OnItemViewClickListener<FeedItem> listener) {
        mItemViewClickListener = listener;
        mCommentBtn.setOnClickListener(new com.umeng.comm.core.listener.Listeners.LoginOnViewClickListener() {

            @Override
            protected void doAfterLogin(View v) {
                if (mItemViewClickListener != null) {
                    mItemViewClickListener.onItemClick(position, mFeedItem);
                }
            }
        });
    }

    public void setIsDisplayTopic(boolean isDisplayTopic) {
        this.isDisplayTopic = isDisplayTopic;
    }
}
