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

package com.umeng.simplify.ui.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.umeng.comm.core.beans.Topic;
import com.umeng.comm.core.utils.CommonUtils;
import com.umeng.comm.core.utils.ResFinder;
import com.umeng.common.ui.adapters.CommonAdapter;
import com.umeng.simplify.ui.adapters.viewholders.TopicSelectViewHolder;


/**
 * 地理位置和好友选择的Adapter基类
 */
public class SelectTopicAdapter extends CommonAdapter<Topic, TopicSelectViewHolder> {

    protected String mFeedsStr = "";
    protected String mFansStr = "";
    protected final String DIVIDER = " / ";

    protected int mTopicColor = 0;
    protected int mTopicIcon = 0;

    private int mBottomMargin;

    /**
     * 推荐话题的显示样式跟推荐用户的样式相同
     *
     * @param context
     */
    public SelectTopicAdapter(Context context) {
        super(context);
        mTopicColor = ResFinder.getColor("umeng_comm_text_topic_light_color");
        mFeedsStr = ResFinder.getString("umeng_comm_feeds_num");
        mFansStr = ResFinder.getString("umeng_comm_fans_num");
        mTopicColor = ResFinder.getColor("umeng_topic_title");
        mTopicIcon = ResFinder.getResourceId(ResFinder.ResType.DRAWABLE, "umeng_comm_topic_icon");
        mBottomMargin = CommonUtils.dip2px(context, 65);
    }

    @Override
    protected TopicSelectViewHolder createViewHolder() {
        return new TopicSelectViewHolder();
    }

    @Override
    protected void setItemData(int position, final TopicSelectViewHolder viewHolder, View rootView) {
        final Topic topic = getItem(position);
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams)viewHolder.mTopicHolderView.getLayoutParams();
        if(position == getCount() - 1){
            params.bottomMargin = mBottomMargin;
        }else{
            params.bottomMargin = 0;
        }
        viewHolder.name.setLayoutParams(params);
        viewHolder.name.setText(topic.name);
    }

    /**
     * 构建feed数量、粉丝数量的字符串
     *
     * @param topic
     * @return
     */
    protected String buildMsgFansStr(Topic topic) {
        StringBuilder builder = new StringBuilder(mFeedsStr);
        builder.append(topic.feedCount);
        builder.append(DIVIDER).append(mFansStr);
        builder.append(topic.fansCount);
        return builder.toString();
    }
}

