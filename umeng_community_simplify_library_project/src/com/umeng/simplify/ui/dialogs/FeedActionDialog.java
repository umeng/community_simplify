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

package com.umeng.simplify.ui.dialogs;

import android.content.Context;
import android.graphics.Color;
import android.view.View;

import com.umeng.comm.core.beans.CommConfig;
import com.umeng.comm.core.beans.CommUser;
import com.umeng.comm.core.constants.Constants;
import com.umeng.comm.core.utils.ResFinder;
import com.umeng.comm.core.utils.ResFinder.ResType;
import com.umeng.common.ui.presenter.impl.FeedDetailActivityPresenter;

/**
 * Feed详情页的举报、删除、拷贝Dialog
 */
public class FeedActionDialog extends com.umeng.common.ui.dialogs.FeedActionDialog {


    public FeedActionDialog(Context context) {
        super(context);
        mPresenter = new FeedDetailActivityPresenter();
        mPresenter.attach(context);
    }

    /**
     * 在显示 or 隐藏 某些View时，需要改变他们的背景</br>
     */
    protected void changeBackground() {
        if (mReportUser.getVisibility() == View.GONE) {
            mDeleteView.setBackgroundResource(ResFinder.getResourceId(ResType.DRAWABLE, "umeng_comm_more_radius_top"));
        } else {
            mReportUser.setBackgroundResource(ResFinder.getResourceId(ResType.DRAWABLE, "umeng_comm_more_radius_top"));
        }
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        mSaveView.setVisibility(View.GONE);
        mShareView.setVisibility(View.GONE);
        mSetRecommened.setVisibility(View.GONE);

        if (!isReportable()) {
            mReportView.setVisibility(View.GONE);
            mReportUser.setVisibility(View.GONE);
        }
        if (!isDeleteable()) {
            mDeleteView.setVisibility(View.GONE);
            mReportView.setVisibility(View.VISIBLE);
        } else {
            mDeleteView.setBackgroundColor(Color.WHITE);
            mReportView.setVisibility(View.GONE);
        }
        changeBackground();
    }

    @Override
    protected boolean isDeleteable() {
        CommUser loginedUser = CommConfig.getConfig().loginedUser;
        boolean deleteable = mFeedItem != null && loginedUser.id.equals(mFeedItem.creator.id); // 自己的feed情况
        boolean hasDeletePermission = mFeedItem != null && mFeedItem.permission >= Constants.PERMISSION_CODE_DELETE;
        return deleteable || hasDeletePermission;
    }
}
