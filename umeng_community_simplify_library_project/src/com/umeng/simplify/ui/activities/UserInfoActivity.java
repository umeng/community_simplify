package com.umeng.simplify.ui.activities;


import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.umeng.comm.core.beans.BaseBean;
import com.umeng.comm.core.beans.CommUser;
import com.umeng.comm.core.beans.FeedItem;
import com.umeng.comm.core.constants.Constants;
import com.umeng.comm.core.constants.ErrorCode;
import com.umeng.comm.core.imageloader.ImgDisplayOption;
import com.umeng.comm.core.impl.CommunitySDKImpl;
import com.umeng.comm.core.listeners.Listeners;
import com.umeng.comm.core.nets.responses.SimpleResponse;
import com.umeng.comm.core.sdkmanager.ImageLoaderManager;
import com.umeng.comm.core.utils.CommonUtils;
import com.umeng.comm.core.utils.DeviceUtils;
import com.umeng.comm.core.utils.ResFinder;
import com.umeng.comm.core.utils.ToastMsg;
import com.umeng.common.ui.activities.BaseFragmentActivity;
import com.umeng.common.ui.mvpview.MvpUserInfoView;
import com.umeng.common.ui.presenter.impl.UserInfoPresenter;
import com.umeng.common.ui.util.BroadcastUtils;
import com.umeng.common.ui.util.UserTypeUtil;
import com.umeng.common.ui.util.ViewFinder;
import com.umeng.common.ui.widgets.CommentEditText;
import com.umeng.common.ui.widgets.RoundImageView;
import com.umeng.simplify.ui.fragments.PostedFeedsFragment;

/**
 * Created by wangfei on 16/4/28.
 */
public class UserInfoActivity extends BaseFragmentActivity implements View.OnClickListener,MvpUserInfoView {
    private PostedFeedsFragment mPostedFragment = null;
    protected CommUser mUser;
    protected int mSelectedColor = Color.BLUE;
    protected TextView mUserNameTv;
    private RoundImageView mHeaderImageView;
    protected ImageView mGenderImageView;
    protected CommentEditText mCommentEditText;
    protected View mCommentLayout;
    protected int mScreenWidth;
    protected UserInfoPresenter mPresenter;
    protected LinearLayout typeContainer;
    private TextView postedCount;

    /**
     * 视图查找器,避免每次findViewById进行强转
     */
    protected ViewFinder mViewFinder;
    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(ResFinder.getLayout("umeng_simplify_user_info"));
        mUser = getIntent().getExtras().getParcelable(Constants.TAG_USER);
        if (mUser == null) {
            return;
        }
        mPresenter = new UserInfoPresenter(this, this, mUser);
        mPresenter.onCreate(arg0);
        initFragment();
        initUIComponents();
        setupUserInfo(mUser);
        BroadcastUtils.registerFeedBroadcast(getApplicationContext(), mReceiver);
    }
    public void initFragment() {
        mViewFinder = new ViewFinder(getWindow().getDecorView());
        mPostedFragment = PostedFeedsFragment.newInstance();
        mPostedFragment.setCurrentUser(mUser);
        mPostedFragment.setOnDeleteListener(new PostedFeedsFragment.OnDeleteListener() {

            @Override
            public void onDelete(BaseBean item) {

            }
        });
        addFragment(ResFinder.getId("umeng_comm_user_info_fragment_container"),
                mPostedFragment);
    }
    protected BroadcastUtils.DefalutReceiver mReceiver = new BroadcastUtils.DefalutReceiver() {

        public void onReceiveFeed(Intent intent) {// 发送or删除时
            FeedItem feedItem = getFeed(intent);
            if (feedItem == null || !CommonUtils.isMyself(mUser)) {
                return;
            }
            BroadcastUtils.BROADCAST_TYPE type = getType(intent);
            if (BroadcastUtils.BROADCAST_TYPE.TYPE_FEED_POST == type) {
                updateFeedTextView(++mUser.feedCount);
            }
            if (BroadcastUtils.BROADCAST_TYPE.TYPE_FEED_DELETE == type) {
                updateFeedTextView(--mUser.feedCount);
            }
        }

        @Override
        public void onReceiveUser(Intent intent) {
            CommUser newUser = getUser(intent);
            BroadcastUtils.BROADCAST_TYPE type = getType(intent);
            boolean follow = true;
            if (type == BroadcastUtils.BROADCAST_TYPE.TYPE_USER_FOLLOW) {
                follow = true;
            } else if (type == BroadcastUtils.BROADCAST_TYPE.TYPE_USER_CANCEL_FOLLOW) {
                follow = false;
            }
//            for (CommUser user : users) {
//                if (user.id.equals(newUser.id)) {
//                    user.isFollowed = follow;
//                    user.extraData.putBoolean(Constants.IS_FOCUSED, follow);
//                    break;
//                }
//            }

        }
    };
    @Override
    public void onClick(View v) {
        if (v.getId() == ResFinder.getId("umeng_comm_title_back_btn")) { // 返回
            this.finish();
        }else if (v.getId() == ResFinder.getId("spam_user")){
            CommunitySDKImpl.getInstance().spamUser(mUser.id, new Listeners.SimpleFetchListener<SimpleResponse>() {
                @Override
                public void onComplete(SimpleResponse response) {
                    if (response.errCode == ErrorCode.NO_ERROR) {
                        ToastMsg.showShortMsgByResName("umeng_comm_text_spammer_success");
                    } else if (response.errCode == ErrorCode.SPAMMERED_CODE) {
                        ToastMsg.showShortMsgByResName("umeng_comm_user_spamed");
                    }else if (response.errCode == ErrorCode.REPORT_SAME_FEED) {
                        ToastMsg.showShortMsgByResName("umeng_comm_user_spamed");
                    }
                    else {
                        ToastMsg.showShortMsgByResName("umeng_comm_text_spammer_failed");
                    }
                }
            });
        }
    }
    @Override
    protected void onDestroy() {
        mPresenter.onDestroy();
        BroadcastUtils.unRegisterBroadcast(getApplicationContext(), mReceiver);
        super.onDestroy();
    }
    /**
     * 隐藏评论布局</br>
     */
    protected void hideCommentLayout() {
        mCommentLayout.setVisibility(View.GONE);
        hideInputMethod(mCommentEditText);
    }
    protected void initCommentView() {
        mCommentEditText = mViewFinder.findViewById(ResFinder
                .getId("umeng_comm_comment_edittext"));
        mCommentLayout = findViewById(ResFinder.getId("umeng_comm_commnet_edit_layout"));

        findViewById(ResFinder.getId("umeng_comm_comment_send_button")).setOnClickListener(this);
        mCommentEditText.setEditTextBackListener(new CommentEditText.EditTextBackEventListener() {

            @Override
            public boolean onClickBack() {
                hideCommentLayout();
                return true;
            }
        });
    }
    protected void initUIComponents() {

        mSelectedColor = ResFinder.getColor("umeng_comm_text_topic_light_color");



//        View settingButton = findViewById(ResFinder.getId("umeng_comm_title_more_btn"));
//        if (mUser.permisson == CommUser.Permisson.ADMIN){
//            settingButton.setVisibility(View.INVISIBLE);
//        }
//        settingButton.setOnClickListener(new Listeners.LoginOnViewClickListener() {
//
//            @Override
//            protected void doAfterLogin(View v) {
//
//            }
//        });

        mUserNameTv = mViewFinder.findViewById(ResFinder.getId("umeng_comm_user_name_tv"));
        mUserNameTv.setText(mUser.name);
        postedCount = mViewFinder.findViewById(ResFinder.getId("feed_count"));
        mHeaderImageView = mViewFinder.findViewById(ResFinder.getId(
                "umeng_comm_user_header"));
        mHeaderImageView.setBackGroundColor(ResFinder.getColor("umeng_comm_color_transparent"));

        ImgDisplayOption option = ImgDisplayOption.getOptionByGender(mUser.gender);
        mHeaderImageView.setImageUrl(mUser.iconUrl, option);
        mGenderImageView = mViewFinder.findViewById(ResFinder.getId("umeng_comm_user_gender"));
        initCommentView();
        mScreenWidth = DeviceUtils.getScreenSize(getApplicationContext()).x;
        findViewById(ResFinder.getId("umeng_comm_title_back_btn")).setOnClickListener(this);
        findViewById(ResFinder.getId("spam_user")).setOnClickListener(this);
        if (CommonUtils.getLoginUser(this).id.equals(mUser.id)){
            findViewById(ResFinder.getId("spam_user")).setVisibility(View.GONE);
        }

        typeContainer = (LinearLayout) findViewById(ResFinder.getId("user_type_icon_container"));
    }

    @Override
    public void setToggleButtonStatus(boolean status) {

    }

    @Override
    public void updateFansTextView(int count) {

    }

    @Override
    public void updateFeedTextView(int count) {
        String postFeedCount = CommonUtils.getLimitedCount(count);
        postedCount.setText(ResFinder.getString("umeng_comm_dynamic") + "  " + postFeedCount);
    }

    @Override
    public void updateFollowTextView(int count) {

    }

    public void setupUserInfo(CommUser user) {
        if (!user.id.equals(mUser.id)) {
            return;
        }
        mUser = user;
        mUserNameTv.setText(user.name);
        if (user.gender == CommUser.Gender.MALE) {
            mGenderImageView.setImageDrawable(ResFinder.getDrawable("umeng_male"));
        } else if (user.gender == CommUser.Gender.FEMALE) {
            mGenderImageView.setImageDrawable(ResFinder.getDrawable("umeng_female"));
        }
        ImgDisplayOption option = ImgDisplayOption.getOptionByGender(user.gender);
        mHeaderImageView.setImageUrl(user.iconUrl, option);

        ImageLoaderManager.getInstance().getCurrentSDK().resume();

//        TextView mScoreTextView = (TextView)findViewById(ResFinder.getId("umeng_comm_user_jifen_tv"));
//        StringBuffer str = new StringBuffer(ResFinder.getString("umeng_comm_user_socre"));
//        str.append(CommonUtils.getLimitedCount(mUser.point));
//        mScoreTextView.setText(str.toString());
        UserTypeUtil.SetUserType(this, user, typeContainer);
    }
}