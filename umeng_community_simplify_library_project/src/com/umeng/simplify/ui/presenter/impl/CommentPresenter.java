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

package com.umeng.simplify.ui.presenter.impl;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import com.umeng.comm.core.beans.CommConfig;
import com.umeng.comm.core.beans.CommUser;
import com.umeng.comm.core.beans.Comment;
import com.umeng.comm.core.beans.FeedItem;
import com.umeng.comm.core.constants.Constants;
import com.umeng.comm.core.constants.ErrorCode;
import com.umeng.comm.core.listeners.Listeners;
import com.umeng.comm.core.nets.responses.LoginResponse;
import com.umeng.comm.core.nets.responses.SimpleResponse;
import com.umeng.comm.core.nets.uitls.NetworkUtils;
import com.umeng.comm.core.utils.CommonUtils;
import com.umeng.comm.core.utils.ResFinder;
import com.umeng.comm.core.utils.ToastMsg;
import com.umeng.common.ui.mvpview.MvpCommentView;


public class CommentPresenter extends com.umeng.common.ui.presenter.impl.CommentPresenter {

    public CommentPresenter(MvpCommentView commentView, FeedItem feedItem) {
        super(commentView, feedItem);
    }

    public void clickCommentItem(int position, final Comment comment) {
        CommUser loginUser = CommConfig.getConfig().loginedUser;
        // 自己创建的评论可以删除
        if (comment.creator.id.equals(loginUser.id)) {
            showSelectDialog(position, comment, true);
        } else if (isMyFeed(comment, loginUser.id)) { // 在自己发布的feed页面可以删除或者回复他人的评论
            showSelectDialog(position, comment, true);
        } else {
            boolean hasDeletePermission = comment.permission >= Constants.PERMISSION_CODE_DELETE;
            showSelectDialog(position, comment, hasDeletePermission);
        }
    }

    /**
     * 弹出举报或者回复对话框
     */
    private void showSelectDialog(final int position, final Comment comment, final boolean delete) {
        final Dialog selectDialog = new Dialog(mContext,
                ResFinder.getStyle("umeng_comm_action_dialog_fullscreen"));

        selectDialog.setContentView(ResFinder.getLayout("umeng_simplify_comment_action_dialog"));
        selectDialog.setCanceledOnTouchOutside(true);

        Window window = selectDialog.getWindow();
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.BOTTOM;
        window.setAttributes(wlp);
        window.setWindowAnimations(ResFinder.getStyle("dialogWindowAnim"));

        View reportTextView = selectDialog.findViewById(ResFinder
                .getId("umeng_comm_report_comment_tv"));
        View reportUserTextView = selectDialog.findViewById(ResFinder
                .getId("umeng_comm_report_user_comment_tv"));
        View deleteTextView = selectDialog.findViewById(ResFinder
                .getId("umeng_comm_delete_comment_tv"));
        View copyTextView = selectDialog.findViewById(ResFinder
                .getId("umeng_comm_copy_comment_tv"));

        // 不能举报自己的评论
        if (isSelfComment(comment.creator)) {
            reportTextView.setVisibility(View.GONE);
            reportUserTextView.setVisibility(View.GONE);
            selectDialog.findViewById(ResFinder.getId("umeng_comm_report_line")).setVisibility(View.GONE);
            selectDialog.findViewById(ResFinder.getId("umeng_comm_report_line1")).setVisibility(View.GONE);
            deleteTextView.setBackgroundResource(ResFinder.getResourceId(ResFinder.ResType.DRAWABLE,
                    "umeng_comm_more_radius_top"));
        } else {
            reportTextView.setVisibility(View.VISIBLE);
            reportUserTextView.setVisibility(View.VISIBLE);
            selectDialog.findViewById(ResFinder.getId("umeng_comm_report_line")).setVisibility(View.VISIBLE);
            selectDialog.findViewById(ResFinder.getId("umeng_comm_report_line1")).setVisibility(View.VISIBLE);
            deleteTextView.setBackgroundResource(ResFinder.getResourceId(ResFinder.ResType.DRAWABLE,
                    "umeng_comm_more_radius_none"));
        }

        // 有删除权限则不需要举报按钮
        if (delete) {
            deleteTextView.setVisibility(View.VISIBLE);
            selectDialog.findViewById(ResFinder.getId("umeng_comm_report_line2")).setVisibility(View.VISIBLE);
            reportTextView.setVisibility(View.GONE);
        } else {
            deleteTextView.setVisibility(View.GONE);
            selectDialog.findViewById(ResFinder.getId("umeng_comm_report_line2")).setVisibility(View.GONE);
            reportTextView.setVisibility(View.VISIBLE);
        }

        reportTextView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                selectDialog.dismiss();
                String tips = ResFinder.getString("umeng_comm_confirm_spam");
                showConfirmDialog(comment, tips, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        spamComment(comment.id);
                    }
                });
            }
        });

        deleteTextView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                selectDialog.dismiss();
                String tips = ResFinder.getString("umeng_comm_delete_comment");
                showConfirmDialog(comment, tips, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteComment(comment);
                    }
                });
            }
        });

        reportUserTextView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                selectDialog.dismiss();
                String tips = ResFinder.getString("umeng_comm_confirm_spam");
                showConfirmDialog(comment, tips, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        reportCommentUser(comment.creator);
                    }
                });
            }
        });

        copyTextView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                selectDialog.dismiss();
                copyToClipboard(comment);
            }
        });


        View cancelBtn = selectDialog.findViewById(ResFinder.getId("umeng_comm_cancel_tv"));
        cancelBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                selectDialog.dismiss();
            }
        });

        selectDialog.show();
    }

    private boolean isSelfComment(CommUser user) {
        if (user.id.equals(CommConfig.getConfig().loginedUser.id)) {
            return true;
        } else {
            return false;
        }
    }

    private void reportCommentUser(final CommUser user) {
        Listeners.SimpleFetchListener<LoginResponse> loginListener = new Listeners.SimpleFetchListener<LoginResponse>() {
            @Override
            public void onComplete(LoginResponse response) {
                if (response.errCode != ErrorCode.NO_ERROR) {
                    return;
                }
                // 举报feed
                mCommunitySDK.spamUser(user.id,
                        new Listeners.FetchListener<SimpleResponse>() {

                            @Override
                            public void onStart() {
                            }

                            @Override
                            public void onComplete(SimpleResponse response) {
                                if (NetworkUtils.handleResponseComm(response)) {
                                    return;
                                }
                                if (response.errCode == ErrorCode.NO_ERROR) {
                                    ToastMsg.showShortMsgByResName(
                                            "umeng_comm_discuss_spammer_success");
                                } else if (response.errCode == ErrorCode.REPORT_SAME_FEED) {
                                    ToastMsg.showShortMsgByResName(
                                            "umeng_comm_discuss_spammered");
                                } else {
                                    ToastMsg.showShortMsgByResName("umeng_comm_discuss_spammer_failed");
                                }
                            }
                        });
            }
        };
        CommonUtils.checkLoginAndFireCallback(mContext, loginListener);
    }

    @SuppressLint("NewApi")
    private void copyToClipboard(Comment comment) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            ClipData data = ClipData.newPlainText("feed_text", comment.text);
            android.content.ClipboardManager mClipboard = (android.content.ClipboardManager) mContext
                    .getSystemService(Context.CLIPBOARD_SERVICE);
            mClipboard.setPrimaryClip(data);
        } else {
            android.text.ClipboardManager mClipboard = (android.text.ClipboardManager) mContext
                    .getSystemService(Context.CLIPBOARD_SERVICE);
            mClipboard.setText(comment.text);
        }
    }

}
