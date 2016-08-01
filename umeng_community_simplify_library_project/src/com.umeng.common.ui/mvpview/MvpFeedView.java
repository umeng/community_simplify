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
package com.umeng.common.ui.mvpview;

import com.umeng.comm.core.beans.FeedItem;

import java.util.List;

/**
 * Feed View接口
 */
public interface MvpFeedView extends MvpBaseRefreshView {

    /**
     * 获取Feed的数据集</br>
     *
     * @return
     */
    public List<FeedItem> getBindDataSource();

    /**
     * 更新Feed Adapter中的数据</br>
     */
    public void notifyDataSetChanged();

    /**
     * 清空Feed列表</br>
     */
    public void clearListView();

    /**
     * 显示登录view</br>
     */
    public void showLoginView();

    /**
     * 隐藏登录view</br>
     */
    public void hideLoginView();

    /**
     * 显示登录提示view</br>
     */
    public void showVisitView();

    /**
     * 隐藏登录提示view</br>
     */
    public void hideVisitView();

    /**
     * 显示progress bar</br>
     */
    public void showProgressBar();

    /**
     * 隐藏progress bar</br>
     */
    public void hideProgressBar();

    /**
     * 跳转到详情页</br>
     */
    public void gotoPostFeedActivity();

    /**
     * 滚动到顶部</br>
     */
    public void scrollToTop();
}
