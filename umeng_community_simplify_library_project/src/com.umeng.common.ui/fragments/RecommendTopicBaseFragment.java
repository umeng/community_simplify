package com.umeng.common.ui.fragments;

import android.content.DialogInterface;
import android.graphics.Color;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.umeng.comm.core.beans.Topic;
import com.umeng.comm.core.utils.CommonUtils;
import com.umeng.comm.core.utils.ResFinder;
import com.umeng.common.ui.adapters.BackupAdapter;
import com.umeng.common.ui.adapters.RecommendTopicAdapter;
import com.umeng.common.ui.colortheme.ColorQueque;
import com.umeng.common.ui.mvpview.MvpRecommendTopicView;
import com.umeng.common.ui.presenter.impl.RecommendTopicPresenter;
import com.umeng.common.ui.util.FontUtils;
import com.umeng.common.ui.widgets.BaseView;
import com.umeng.common.ui.widgets.RefreshLayout;
import com.umeng.common.ui.widgets.RefreshLvLayout;

import java.util.List;

/**
 * Created by wangfei on 16/1/19.
 */
public abstract class RecommendTopicBaseFragment extends TopicBaseFragment implements View.OnClickListener {

    private boolean mButtonVisiable = true;
    private Button mNextButton;

    private DialogInterface.OnDismissListener mOnDismissListener;

    @Override
    protected final RecommendTopicPresenter createPresenters() {
        return new RecommendTopicPresenter(this, true);
    }

    @Override
    protected int getFragmentLayout() {
        return ResFinder.getLayout("umeng_comm_topic_recommend");
    }

    @Override
    protected final void initTitleView(View rootView) {
        mNextButton = (Button) rootView.findViewById(ResFinder.getId("umeng_comm_save_bt"));
        mNextButton.setOnClickListener(this);
        mNextButton.setText(ResFinder.getString("umeng_comm_skip"));
        mNextButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        mNextButton.setTextColor(ColorQueque.getColor("umeng_comm_category_title_color"));
        if (!mButtonVisiable) {
            mNextButton.setVisibility(View.GONE);
            rootView.findViewById(ResFinder.getId("umeng_comm_setting_back")).setOnClickListener(this);
        } else {
            rootView.findViewById(ResFinder.getId("umeng_comm_setting_back")).setVisibility(View.GONE);
        }
        TextView textView = (TextView) rootView.findViewById(ResFinder
                .getId("umeng_comm_setting_title"));
        textView.setText(ResFinder.getString("umeng_comm_recommend_topic"));
        textView.setTextColor(Color.BLACK);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
        rootView.findViewById(ResFinder.getId("umeng_comm_title_bar_root")).setBackgroundColor(Color.WHITE);
    }

    @Override
    protected final void initAdapter() {
        RecommendTopicAdapter adapter = new RecommendTopicAdapter(getActivity());
        adapter.setFromFindPage(!mButtonVisiable);
        mAdapter = adapter;
        adapter.setFollowListener(new RecommendTopicAdapter.FollowListener<Topic>() {

            @Override
            public void onFollowOrUnFollow(Topic topic, ToggleButton toggleButton, boolean isFollow) {
                if (mNextButton.getText().equals(ResFinder.getString("umeng_comm_skip"))) {
                    mNextButton.setText(ResFinder.getString("umeng_comm_next"));
                }
                if (isFollow) {
                    mPresenter.followTopic(topic, toggleButton);
                } else {
                    mPresenter.cancelFollowTopic(topic, toggleButton);
                }
            }
        });
        mTopicListView.setAdapter(mAdapter);
    }

    /**
     * 设置保存按钮不可见。在设置页面显示推荐话题时，不需要显示</br>
     */
    public final void setSaveButtonInVisiable() {
        mButtonVisiable = false;
    }

    public final void setOnDismissListener(DialogInterface.OnDismissListener listener) {
        this.mOnDismissListener = listener;
    }

    @Override
    public final void onClick(View v) {
        int id = v.getId();
        if (id == ResFinder.getId("umeng_comm_save_bt") || id == ResFinder.getId("umeng_comm_setting_back")) {
            mOnDismissListener.onDismiss(null);
        }
    }
}
