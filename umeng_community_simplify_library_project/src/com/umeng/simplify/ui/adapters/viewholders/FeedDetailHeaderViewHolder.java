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
import android.graphics.Bitmap;
import android.graphics.Point;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.umeng.comm.core.beans.CommUser;
import com.umeng.comm.core.beans.FeedItem;
import com.umeng.comm.core.beans.Topic;
import com.umeng.comm.core.constants.Constants;
import com.umeng.comm.core.imageloader.ImgDisplayOption;
import com.umeng.comm.core.imageloader.UMImageLoader;
import com.umeng.comm.core.sdkmanager.ImageLoaderManager;
import com.umeng.comm.core.utils.CommonUtils;
import com.umeng.comm.core.utils.ResFinder;
import com.umeng.comm.core.utils.TimeUtils;
//import com.umeng.simplify.ui.activites.TopicDetailActivity;
import com.umeng.simplify.ui.activities.TopicDetailActivity;
import com.umeng.simplify.ui.activities.UserInfoActivity;
import com.umeng.common.ui.dialogs.ImageBrowser;
import com.umeng.common.ui.listener.FrinendClickSpanListener;
import com.umeng.common.ui.listener.TopicClickSpanListener;
import com.umeng.common.ui.util.FeedViewRender;
import com.umeng.common.ui.util.UserTypeUtil;
import com.umeng.common.ui.util.WebViewSettingUtil;
import com.umeng.common.ui.widgets.RoundImageView;

import java.util.Date;

/**
 *
 */
public class FeedDetailHeaderViewHolder implements
        UMImageLoader.ImageLoadingListener {

    private RoundImageView mPortrait;
    private TextView mUserNameTv;
    private TextView mTopicTv;
    private TextView mFeedTextTv;
    private LinearLayout mUserTypeIcon;

    private LinearLayout mImageContainer;
    private TextView mTimeTv;

    private View mFeedContentContainer;
    private WebView mContentWeb;

//    private TextView mLikeTv;
//    private View mLikeBtn;

    private UMImageLoader mImageLoader;
    private ImgDisplayOption mDisplayOption = new ImgDisplayOption();

    private int mScreenWidth;
    private boolean mIsAddImg;

    private ImageBrowser mImageBrowser;

    private Context mContext;

    private FeedItem mFeedItem;

    private boolean isDisplayTopic;

    public FeedDetailHeaderViewHolder(Context context, View rootView) {
        this.mContext = context;
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        mScreenWidth = wm.getDefaultDisplay().getWidth();
        mScreenWidth -= CommonUtils.dip2px(mContext, 20);
        mDisplayOption.mLoadingResId = ResFinder.getResourceId(ResFinder.ResType.DRAWABLE,
                "umeng_comm_not_found");
        mDisplayOption.mLoadFailedResId = ResFinder.getResourceId(ResFinder.ResType.DRAWABLE,
                "umeng_comm_not_found");
        mDisplayOption.mDefaultImageSize = new Point(600, 600);
        mImageBrowser = new ImageBrowser(mContext);

        inflateFromXML(rootView);
    }


    public void setFeedItem(FeedItem feedItem) {
        this.mFeedItem = feedItem;
        setUserInfo();
        setFeedInfo();
    }

    private void setUserInfo() {
        mImageLoader = ImageLoaderManager.getInstance().getCurrentSDK();
        // 用户头像
        ImgDisplayOption option = ImgDisplayOption.getOptionByGender(mFeedItem.creator.gender);
        if (!TextUtils.isEmpty(mFeedItem.creator.iconUrl)) {
            mImageLoader.displayImage(mFeedItem.creator.iconUrl, mPortrait, option);
        } else {
            mPortrait.setImageResource(option.mLoadingResId);
        }

        mPortrait.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(mContext, UserInfoActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(Constants.TAG_USER, mFeedItem.creator);
                mContext.startActivity(intent);
            }
        });

        // 昵称
        mUserNameTv.setText(mFeedItem.creator.name);
        UserTypeUtil.SetUserType(mContext, mFeedItem.creator, mUserTypeIcon);

        // 更新时间
        Date date = new Date(Long.parseLong(mFeedItem.publishTime));
        mTimeTv.setText(TimeUtils.format(date));

        String topicStr = null;
        if (mFeedItem.topics != null && !mFeedItem.topics.isEmpty()) {
            topicStr = mFeedItem.topics.get(0).name;
        }
        if (TextUtils.isEmpty(topicStr) || !isDisplayTopic) {
            mTopicTv.setVisibility(View.GONE);
        } else {
            mTopicTv.setText(topicStr);
            mTopicTv.setVisibility(View.VISIBLE);
            mTopicTv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, TopicDetailActivity.class);
                    intent.putExtra(Constants.TAG_TOPIC, mFeedItem.topics.get(0));
                    mContext.startActivity(intent);
                }
            });
        }
    }

    private void setFeedInfo() {
        if (mFeedItem.media_type == 1) {
            WebViewSettingUtil.SetWebiew(mContentWeb, 1, mFeedItem, null, null, mContext);
            mFeedContentContainer.setVisibility(View.GONE);
            mContentWeb.setVisibility(View.VISIBLE);
            return;
        }

        mContentWeb.setVisibility(View.GONE);
        mFeedContentContainer.setVisibility(View.VISIBLE);

        // 内容为空时Text隐藏布局,这种情况出现在转发时没有文本的情况
        if (mFeedItem.media_type == 0) {
            if (TextUtils.isEmpty(mFeedItem.text)) {
                mFeedTextTv.setVisibility(View.GONE);
            } else {
                mFeedTextTv.setVisibility(View.VISIBLE);
            }
        }

        FeedItem tempFeed = new FeedItem();
        tempFeed.text = mFeedItem.text;
        // feed的文本内容
        FeedViewRender.parseTopicsAndFriends(mFeedTextTv, tempFeed, new TopicClickSpanListener() {
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

        setupImageGridView();
    }

    public boolean setupImageGridView() {
        if (mIsAddImg) {
            return true;
        }
        mIsAddImg = true;
        if (mFeedItem.imageUrls == null || mFeedItem.imageUrls.size() <= 0) {
            mImageContainer.setVisibility(View.GONE);
            return true;
        }
        for (int i = 0; i < mFeedItem.imageUrls.size(); i++) {
            final int position = i;
            ImageView imageView = new ImageView(mContext);
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mImageBrowser.setImageList(mFeedItem.imageUrls, position);
                    mImageBrowser.show();
                }
            });
            mImageContainer.addView(imageView);
            imageView.setVisibility(View.GONE);
            mImageLoader.displayImage(mFeedItem.imageUrls.get(i).originImageUrl,
                    imageView, mDisplayOption, this);
        }
        mImageContainer.setVisibility(View.VISIBLE);
        return true;
    }

    protected void inflateFromXML(View rootView) {
//        int feedTypeResId = ResFinder.getId("feed_type_img_btn");
//        int titleResId = ResFinder.getId("feed_detail_title");
//        int timeResId = ResFinder.getId("umeng_comm_msg_time_tv");
//        int userNameResId = ResFinder.getId("umeng_comm_msg_user_name");
//
//        int textResId = ResFinder.getId("umeng_comm_msg_text");
//        int PortraitResId = ResFinder.getId("user_portrait_img_btn");
//
//        // 公告或者好友feed的图标
//        mFeedTypeImgView = findViewById(feedTypeResId);
//
//        mFeedTextTv = findViewById(textResId);
//        // 发布该消息的昵称
//        mUserNameTv = findViewById(userNameResId);
//
//        mPortrait = findViewById(PortraitResId);
//
//        mTimeTv = findViewById(timeResId);
//
//        mTitleView = findViewById(titleResId);
//
//        mImageContainer = findViewById(ResFinder.getId("umeng_comm_msg_images_gv_viewstub"));
//
//        mUserTypeIcon = findViewById(ResFinder.getId("user_type_icon_container"));
//        mContentWeb = findViewById(ResFinder.getId("umeng_comm_msg_textweb"));


        int mUserAvatarResId = ResFinder.getId("user_portrait_img_btn");
        int mUserNameResId = ResFinder.getId("umeng_comm_msg_user_name");
        int mUserMeadlResId = ResFinder.getId("user_type_icon_container");
        int mTopicResId = ResFinder.getId("umeng_comm_feed_item_topic");
        int mFeedPublishTimeResId = ResFinder.getId("umeng_comm_msg_time_tv");

        int mFeedTextResId = ResFinder.getId("umeng_comm_msg_text");
        int mFeedImgContainerResId = ResFinder.getId("umeng_comm_msg_images_gv_viewstub");
        int mFeedContanierResId = ResFinder.getId("umeng_comm_feed_detail_content_container");
        int mFeedWebViewResId = ResFinder.getId("umeng_comm_msg_textweb");

//        int mLikeCountResId = ResFinder.getId("umeng_comm_like_count_tv");
//        int mLikeCountBtnResId = ResFinder.getId("umeng_comm_like_btn");

        mPortrait = (RoundImageView) rootView.findViewById(mUserAvatarResId);
        mUserNameTv = (TextView) rootView.findViewById(mUserNameResId);
        mUserTypeIcon = (LinearLayout) rootView.findViewById(mUserMeadlResId);
        mTopicTv = (TextView) rootView.findViewById(mTopicResId);
        mTimeTv = (TextView) rootView.findViewById(mFeedPublishTimeResId);

        mFeedTextTv = (TextView) rootView.findViewById(mFeedTextResId);
        mImageContainer = (LinearLayout) rootView.findViewById(mFeedImgContainerResId);
        mFeedContentContainer = rootView.findViewById(mFeedContanierResId);
        mContentWeb = (WebView) rootView.findViewById(mFeedWebViewResId);

//        mLikeBtn = rootView.findViewById(mLikeCountBtnResId);
//        mLikeTv = (TextView) rootView.findViewById(mLikeCountResId);
    }

    private void adjustImgViewSize(int w, int h, ImageView v) {
        LinearLayout.LayoutParams mImageViewLayoutParams;
        int imgHeight = (int) (h * mScreenWidth / ((float) w));
        if (imgHeight > mScreenWidth * 3) {
            imgHeight = mScreenWidth * 3;
        }
        mImageViewLayoutParams = new LinearLayout.LayoutParams(mScreenWidth, imgHeight);
        mImageViewLayoutParams.bottomMargin = CommonUtils.dip2px(mContext, 8);
        v.setScaleType(ImageView.ScaleType.CENTER_CROP);
        v.setLayoutParams(mImageViewLayoutParams);
        v.setVisibility(View.VISIBLE);
    }

    @Override
    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
        adjustImgViewSize(loadedImage.getWidth(), loadedImage.getHeight(), (ImageView) view);
        ((ImageView) view).setImageBitmap(loadedImage);
    }

    @Override
    public void onLoadingFailed(String imageUri, View view) {
    }

    @Override
    public void onLoadingStarted(String imageUri, View view) {

    }

    public void setIsDisplayTopic(boolean isDisplayTopic){
        this.isDisplayTopic = isDisplayTopic;
    }
}
