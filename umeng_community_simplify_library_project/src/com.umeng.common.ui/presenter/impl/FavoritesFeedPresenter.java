/**
 *
 */

package com.umeng.common.ui.presenter.impl;

import com.umeng.comm.core.beans.FeedItem;
import com.umeng.comm.core.beans.FeedItem.CATEGORY;
import com.umeng.comm.core.constants.Constants;
import com.umeng.comm.core.nets.responses.FeedsResponse;
import com.umeng.common.ui.mvpview.MvpFeedView;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;


/**
 * 收藏presenter</br>
 */
public class FavoritesFeedPresenter extends FeedListPresenter {

    /**
     * @param feedViewInterface
     */
    public FavoritesFeedPresenter(MvpFeedView feedViewInterface) {
        super(feedViewInterface);
    }

    @Override
    public void loadDataFromServer() {
        mCommunitySDK.fetchFavoritesFeed(mRefreshListener);
    }

    @Override
    protected void beforeDeliveryFeeds(FeedsResponse response) {
        isNeedRemoveOldFeeds.set(false);
        for (FeedItem item : response.result) {
            item.category = CATEGORY.FAVORITES;
        }
    }

    @Override
    public void loadDataFromDB() {
        mDatabaseAPI.getFeedDBAPI().loadFavoritesFeed(mDbFetchListener);
    }

//    /**
//     * 更新feed 收藏/ 取消收藏</br>
//     *
//     * @param item 需要被更新的feedid
//     * @param category
//     */
//    public void updateFeedFavourites(FeedItem item, CATEGORY category) {
//        List<FeedItem> items = mFeedView.getBindDataSource();
//        if (category == CATEGORY.FAVORITES) {
//            items.add(item);
//            mFeedView.notifyDataSetChanged();
//            return;
//        }
//        Iterator<FeedItem> iterator = items.iterator();
//        while (iterator.hasNext()) {
//            if (iterator.next().id.equals(item.id)) {
//                iterator.remove();
//                break;
//            }
//        }
//        mFeedView.notifyDataSetChanged();
//    }

    /**
     * 处理收藏的逻辑</br>
     *
     * @param feedItem
     */
    @Override
    protected void dealFavourite(FeedItem feedItem) {
        super.dealFavourite(feedItem);
        mFeedView.getBindDataSource().add(feedItem);
        sortFeedItems(mFeedView.getBindDataSource());
        mFeedView.notifyDataSetChanged();
    }

    @Override
    protected int appendFeedItemsToHeader(List<FeedItem> feedItems) {
        if (Constants.IS_CLEAR_DATA_AFTER_REFRESH) {
            mFeedView.getBindDataSource().clear();
        }
        List<FeedItem> olds = mFeedView.getBindDataSource();
        int size = olds.size();
        olds.removeAll(feedItems);
        olds.addAll(0, feedItems);
        int news = olds.size() - size;
        sortFeedItems(olds);
        mFeedView.notifyDataSetChanged();
        return news;
    }

    /**
     * 收藏feed按照添加时间排序
     */
    private Comparator<FeedItem> mComparator = new Comparator<FeedItem>() {

        @Override
        public int compare(FeedItem lhs, FeedItem rhs) {
            return rhs.addTime.compareTo(lhs.addTime);
        }
    };

    @Override
    public List<FeedItem> doFilte(List<FeedItem> newItems) {
        return newItems;
    }

    @Override
    protected Comparator<FeedItem> getFeedCompartator() {
        return mComparator;
    }

    @Override
    protected boolean isReomveFeedOnDeleteComplete() {
        return false;
    }
}
