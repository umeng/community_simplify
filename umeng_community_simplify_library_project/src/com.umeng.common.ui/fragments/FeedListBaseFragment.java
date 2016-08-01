package com.umeng.common.ui.fragments;

import android.app.Dialog;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewStub;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.umeng.comm.core.beans.CommConfig;
import com.umeng.comm.core.beans.CommUser;
import com.umeng.comm.core.beans.FeedItem;
import com.umeng.comm.core.imageloader.UMImageLoader;
import com.umeng.comm.core.sdkmanager.ImageLoaderManager;
import com.umeng.comm.core.utils.DeviceUtils;
import com.umeng.comm.core.utils.ResFinder;
import com.umeng.common.ui.adapters.CommonAdapter;
import com.umeng.common.ui.dialogs.CustomCommomDialog;
import com.umeng.common.ui.mvpview.MvpFeedView;
import com.umeng.common.ui.presenter.impl.FeedListPresenter;
import com.umeng.common.ui.util.Filter;
import com.umeng.common.ui.widgets.BaseView;
import com.umeng.common.ui.widgets.RefreshLayout;
import com.umeng.common.ui.widgets.RefreshLvLayout;

import java.util.List;

/**
 * Created by wangfei on 16/1/18.
 */
public abstract class FeedListBaseFragment<P extends FeedListPresenter, T extends CommonAdapter> extends
        BaseFragment<List<FeedItem>, P> implements MvpFeedView {
    /**
     * ImageLoader
     */
    protected UMImageLoader mImageLoader = ImageLoaderManager.getInstance().getCurrentSDK();
    /**
     * 下拉刷新, 上拉加载的布局, 包裹了Feeds ListView
     */
    protected RefreshLvLayout mRefreshLayout;
    /**
     * feeds ListView
     */
    protected ListView mFeedsListView;
    /**
     * 消息流适配器
     */
    protected T mFeedLvAdapter;
    /**
     * title的文本TextView
     */
    protected TextView mTitleTextView;
    /**
     * 过滤掉某些关键字的filter
     */
    protected Filter<FeedItem> mFeedFilter;
    /**
     * 当前登录的用户
     */
    protected CommUser mUser = CommConfig.getConfig().loginedUser;
    /**
     * 发表feed的button
     */
    protected ImageView mPostBtn;

    protected ViewStub mDaysView;

    protected LinearLayout mLinearLayout;
    protected Dialog mProcessDialog;

    private BaseView mBaseView;

//    protected boolean isVisit = true;

    @Override
    protected int getFragmentLayout() {
        return ResFinder.getLayout("umeng_comm_feeds_frgm_layout");
    }

    @Override
    protected void initWidgets() {
        super.initWidgets();
        // 初始化视图
        initViews();
        // 初始化Feed Adapter
        initAdapter();
        // 请求中的状态
        mRefreshLayout.setRefreshing(true);
        mProcessDialog = new CustomCommomDialog(getActivity(), ResFinder.getString("umeng_comm_logining"));
        mBaseView = (BaseView) mRootView.findViewById(ResFinder.getId("umeng_comm_baseview"));
        if (mBaseView != null) {
            mBaseView.setEmptyViewText(ResFinder.getString("umeng_comm_no_feed"));
            mBaseView.hideEmptyView();
        }
    }

    /**
     * 初始化feed流 页面显示相关View
     */
    protected void initViews() {
        // 初始化刷新相关View跟事件
        initRefreshView();
        mPostBtn = mViewFinder.findViewById(ResFinder.getId("umeng_comm_new_post_btn"));
        mPostBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPresenter != null) {
                    mPresenter.postFeed();
                }
            }
        });
        mLinearLayout = mViewFinder.findViewById(ResFinder.getId("umeng_comm_ll"));
    }

    /**
     * 初始化下拉刷新试图, listview
     */
    protected void initRefreshView() {
        // 下拉刷新, 上拉加载的布局
        mRefreshLayout = mViewFinder.findViewById(ResFinder.getId("umeng_comm_swipe_layout"));
        // 下拉刷新时执行的回调
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

            @Override
            public void onRefresh() {
                // 加载最新的feed
                mPresenter.loadDataFromServer();
            }
        });

        // 上拉加载更多
        mRefreshLayout.setOnLoadListener(new RefreshLayout.OnLoadListener() {

            @Override
            public void onLoad() {
                loadMoreFeed();
            }
        });

        // 滚动监听器, 滚动停止时才加载图片
        mRefreshLayout.addOnScrollListener(new AbsListView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                    mImageLoader.resume();
                } else {
                    mImageLoader.pause();
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                                 int totalItemCount) {

            }
        });

        int feedListViewResId = ResFinder.getId("umeng_comm_feed_listview");
        // feed列表 listview
        mFeedsListView = mRefreshLayout.findRefreshViewById(feedListViewResId);
        // 添加footer
        mRefreshLayout.setDefaultFooterView();
        // 关闭动画缓存
        mFeedsListView.setAnimationCacheEnabled(false);
        // 开启smooth scrool bar
        mFeedsListView.setSmoothScrollbarEnabled(true);
    }

    /**
     *
     */
    protected void showPostButtonWithAnim() {
    }

    protected abstract void deleteInvalidateFeed(FeedItem feedItem);

    protected abstract void updateAfterDelete(FeedItem feedItem);

    /**
     * 加载更多数据</br>
     */
    protected void loadMoreFeed() {
        // 没有网络的情况下从数据库加载
        if (!DeviceUtils.isNetworkAvailable(getActivity())) {
            mPresenter.loadDataFromDB();
            mRefreshLayout.setLoading(false);
            return;
        }
        mPresenter.fetchNextPageData();
//        if(isCanLoadMore()){
//            mPresenter.fetchNextPageData();
//        }else{
//            onRefreshEnd();
//        }
    }

    protected abstract T createListViewAdapter();

    /**
     * 初始化适配器
     */
    protected abstract void initAdapter();

    @Override
    public void onResume() {
        super.onResume();
        onBaseResumeDeal();
    }

    /**
     * 基本的 OnResume处理逻辑</br>
     */
    protected void onBaseResumeDeal() {
        mFeedsListView.postDelayed(new Runnable() {

            @Override
            public void run() {
                if (mImageLoader != null) {
                    // 启动加载数据
                    mImageLoader.resume();
                }
            }
        }, 300);
    }

    /**
     * 设置feed的过滤器</br>
     *
     * @param filter
     */
    public void setFeedFilter(Filter<FeedItem> filter) {
        mFeedFilter = filter;
    }

    /**
     * 主动调用加载数据。 【注意】该接口仅仅在退出登录时，跳转到FeedsActivity清理数据后重新刷新数据</br>
     */
    public void loadDataFromServer() {
        if (mPresenter != null) {
            mPresenter.loadDataFromServer();
        }
    }

    @Override
    public void clearListView() {
        if (mFeedLvAdapter != null) {
            mFeedLvAdapter.getDataSource().clear();
            mFeedLvAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onRefreshStart() {
        //the fragment has detached with activity
        if (!this.isAdded()) {
            return;
        }
        mRefreshLayout.setProgressViewOffset(false, 0,
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getResources().getDisplayMetrics()));
        mRefreshLayout.setRefreshing(true);
    }

    @Override
    public void onRefreshEnd() {
        if (!isAdded()) {
            return;
        }
        mRefreshLayout.setRefreshing(false);
        mRefreshLayout.setLoading(false);
        if (mBaseView != null && mFeedLvAdapter != null) {
            if (mFeedLvAdapter.isEmpty()) {
                mBaseView.showEmptyView();
            } else {
                mBaseView.hideEmptyView();
            }
        }
    }

    @Override
    public abstract List<FeedItem> getBindDataSource();

    @Override
    public abstract void notifyDataSetChanged();

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

    @Override
    public void showProgressBar() {
        if (!isAdded()) {
            return;
        }
        if (mProcessDialog != null) {
            mProcessDialog.show();
        }
    }

    @Override
    public void hideProgressBar() {
        if (!isAdded()) {
            return;
        }
        if (mProcessDialog != null) {
            mProcessDialog.hide();
        }
    }

    @Override
    public void showLoginView() {
        if (!isAdded()) {
            return;
        }
    }

    @Override
    public void hideLoginView() {
        if (!isAdded()) {
            return;
        }
    }

    @Override
    public void showVisitView() {
        if (!isAdded()) {
            return;
        }
        mRefreshLayout.disposeLoginTipsView(true);
    }

    @Override
    public void hideVisitView() {
        if (!isAdded()) {
            return;
        }
        mRefreshLayout.disposeLoginTipsView(false);
    }

    @Override
    public void gotoPostFeedActivity() {
        if (!isAdded()) {
            return;
        }
    }

    @Override
    public void scrollToTop() {
        if (!isAdded()) {
            return;
        }
        mFeedsListView.setSelection(0);
    }
}
