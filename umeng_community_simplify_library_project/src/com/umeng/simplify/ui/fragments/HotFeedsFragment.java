package com.umeng.simplify.ui.fragments;

import android.view.View;
import android.view.animation.AlphaAnimation;


import com.umeng.simplify.ui.presenter.impl.HottestFeedPresenter;


/**
 * Created by umeng on 12/1/15.
 */
public class HotFeedsFragment extends PostBtnAnimFragment<HottestFeedPresenter> {

    HottestFeedPresenter mHottestFeedPresenter;

    @Override
    protected void showPostButtonWithAnim() {
        AlphaAnimation showAnim = new AlphaAnimation(0.5f, 1.0f);
        showAnim.setDuration(500);

        mPostBtn.setVisibility(View.VISIBLE);
        mPostBtn.startAnimation(showAnim);
    }

    @Override
    protected HottestFeedPresenter createPresenters() {
        mHottestFeedPresenter = new HottestFeedPresenter(this);
        mHottestFeedPresenter.setIsShowTopFeeds(false);
        return mHottestFeedPresenter;
    }
}
