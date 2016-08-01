package com.umeng.simplify.ui.adapters.viewholders;

import android.view.View;
import android.widget.TextView;

import com.umeng.comm.core.utils.ResFinder;
import com.umeng.common.ui.adapters.viewholders.ViewHolder;

/**
 * Created by wangfei on 16/5/3.
 */
public class TopicSelectViewHolder extends ViewHolder {
    public TextView name;
    public View mTopicHolderView;
    @Override
    protected int getItemLayout() {
        return ResFinder.getLayout("umeng_topicpick_item");
    }
    @Override
    protected void initWidgets() {
        name = findViewById(ResFinder.getId("topicname"));
        mTopicHolderView = findViewById(ResFinder.getId("umeng_comm_topic_item_holder"));
    }
}
