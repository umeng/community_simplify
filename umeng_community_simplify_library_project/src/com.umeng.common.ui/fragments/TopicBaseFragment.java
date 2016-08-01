package com.umeng.common.ui.fragments;

import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.widget.ListView;

import com.umeng.comm.core.beans.Topic;
import com.umeng.comm.core.utils.ResFinder;
import com.umeng.common.ui.adapters.BackupAdapter;
import com.umeng.common.ui.mvpview.MvpRecommendTopicView;
import com.umeng.common.ui.presenter.impl.TopicBasePresenter;
import com.umeng.common.ui.util.FontUtils;
import com.umeng.common.ui.widgets.BaseView;
import com.umeng.common.ui.widgets.RefreshLayout;
import com.umeng.common.ui.widgets.RefreshLvLayout;

import java.util.List;

public abstract class TopicBaseFragment extends BaseFragment<List<Topic>, TopicBasePresenter>
        implements MvpRecommendTopicView {

    protected BackupAdapter<Topic, ?> mAdapter;

    protected ListView mTopicListView;
    protected RefreshLvLayout mRefreshLvLayout;
    protected BaseView mBaseView;

//    private boolean isVisit = true;

    @Override
    protected void initWidgets() {
        FontUtils.changeTypeface(mRootView);
        initRefreshView(mRootView);
        initTitleView(mRootView);
        initSearchView(mRootView);
        initAdapter();
        setAdapterGotoDetail();
    }

    protected abstract void initAdapter();

    protected abstract void setAdapterGotoDetail();

    protected void initTitleView(View rootView) {
    }

    protected void initSearchView(View rootView) {
    }

    /**
     * 初始化刷新相关的view跟事件</br>
     *
     * @param rootView
     */
    protected void initRefreshView(View rootView) {
        int refreshResId = ResFinder.getId("umeng_comm_topic_refersh");
        mRefreshLvLayout = (RefreshLvLayout) rootView.findViewById(refreshResId);
        mRefreshLvLayout.setDefaultFooterView();
        mRefreshLvLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

            @Override
            public void onRefresh() {
                mPresenter.loadDataFromServer();
            }
        });
        mRefreshLvLayout.setOnLoadListener(new RefreshLayout.OnLoadListener() {
            @Override
            public void onLoad() {
                mPresenter.loadMoreData();
//                if (isCanLoadMore()) {
//                    mPresenter.loadMoreData();
//                } else {
//                    onRefreshEnd();
//                }
            }
        });

        // listview
        int listViewResId = ResFinder.getId("umeng_comm_topic_listview");
        mTopicListView = mRefreshLvLayout.findRefreshViewById(listViewResId);

        // emptyview
        mBaseView = (BaseView) rootView.findViewById(ResFinder.getId("umeng_comm_baseview"));
        mBaseView.setEmptyViewText(ResFinder.getString("umeng_comm_no_recommend_topic"));
    }

    @Override
    protected abstract int getFragmentLayout();

    @Override
    protected abstract TopicBasePresenter createPresenters();

    @Override
    public List<Topic> getBindDataSource() {
        if (mAdapter != null) {
            return mAdapter.getDataSource();
        } else
            return null;
    }

    @Override
    public void notifyDataSetChanged() {
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onRefreshStart() {
        if (mRefreshLvLayout != null) {
            mRefreshLvLayout.setRefreshing(true);
        }
    }

    @Override
    public void onRefreshEnd() {
        onRefreshEndNoOP();
        if (mAdapter != null) {
            if (mAdapter.isEmpty()) {
                mBaseView.showEmptyView();
            } else {
                mBaseView.hideEmptyView();
            }
        }
    }

    @Override
    public void onRefreshEndNoOP() {
        if (mRefreshLvLayout != null) {
            mRefreshLvLayout.setRefreshing(false);
            mRefreshLvLayout.setLoading(false);
        }
        if (mBaseView != null) {
            mBaseView.hideEmptyView();
        }
    }

//    @Override
//    public void setIsVisitBtn(boolean isVisit) {
//        if (!isAdded()) {
//            return;
//        }
//        this.isVisit = isVisit;
//        if (!isVisit && !CommonUtils.isLogin(getActivity())) {
//            mRefreshLvLayout.disposeLoginTipsView(true);
//        } else {
//            mRefreshLvLayout.disposeLoginTipsView(false);
//        }
//    }
//
//    @Override
//    public void onUserLogin() {
//        if (!isVisit && CommonUtils.isLogin(getActivity())) {
//            isVisit = true;
//            mRefreshLvLayout.disposeLoginTipsView(false);
//        }
//    }

    @Override
    public void showVisitView() {
        if(!isAdded()){
            return;
        }
        mRefreshLvLayout.disposeLoginTipsView(true);
    }

    @Override
    public void hideVisitView() {
        if(!isAdded()){
            return;
        }
        mRefreshLvLayout.disposeLoginTipsView(false);
    }

    @Override
    public void showLoginView() {

    }

    @Override
    public void hideLoginView() {

    }

//    protected boolean isCanLoadMore() {
//        boolean isLogin = (getActivity() != null) && CommonUtils.isLogin(getActivity());
//        return isVisit || isLogin;
//    }

}
