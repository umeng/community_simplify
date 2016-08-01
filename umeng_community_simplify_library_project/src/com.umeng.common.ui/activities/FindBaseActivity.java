package com.umeng.common.ui.activities;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.umeng.comm.core.beans.CommConfig;
import com.umeng.comm.core.beans.CommUser;
import com.umeng.comm.core.beans.MessageCount;
import com.umeng.comm.core.constants.Constants;
import com.umeng.comm.core.db.ctrl.impl.DatabaseAPI;
import com.umeng.comm.core.imageloader.ImgDisplayOption;
import com.umeng.comm.core.impl.CommunitySDKImpl;
import com.umeng.comm.core.listeners.Listeners;
import com.umeng.comm.core.login.LoginListener;
import com.umeng.comm.core.utils.CommonUtils;
import com.umeng.comm.core.utils.DeviceUtils;
import com.umeng.comm.core.utils.Log;
import com.umeng.comm.core.utils.ResFinder;
import com.umeng.common.ui.adapters.MyAdapterInFind;
import com.umeng.common.ui.colortheme.ColorQueque;
import com.umeng.common.ui.dialogs.CustomCommomDialog;
import com.umeng.common.ui.util.UserTypeUtil;
import com.umeng.common.ui.widgets.RoundImageView;

import java.util.ArrayList;


/**
 * Created by wangfei on 16/1/15.
 */
public abstract class FindBaseActivity extends BaseFragmentActivity implements View.OnClickListener {
    protected CommUser mUser;
    protected String mContainerClass;
    //    protected RecommendTopicBaseFragment mRecommendTopicFragment;
//    protected RecommendUserFragment mRecommendUserFragment;
//    protected FriendsFragment mFriendsFragment;
//    protected NearbyFeedFragment mNearbyFeedFragment;
//    protected FavoritesFragment mFavoritesFragment;
//    protected RealTimeFeedFragment mRealTimeFeedFragment;
    protected MessageCount mUnReadMsg;
    protected View mMsgBadgeView;
//    protected View mNotifyBadgeView;
    protected LinearLayout typeContainer;
    protected Dialog processDialog;
    protected LinearLayout listContainer;
//    protected ArrayList<String> titles = new ArrayList<>();
    protected MyAdapterInFind msgAdapter;
    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        getLayout();
        initViews();
    }
    protected void initViews(){
        getLayout();
        processDialog = new CustomCommomDialog(this,ResFinder.getString("umeng_comm_logining"));
        findViewById(ResFinder.getId("umeng_comm_title_back_btn")).setOnClickListener(this);
//        findViewById(ResFinder.getId("umeng_comm_topic_recommend")).setOnClickListener(this);
//        findViewById(ResFinder.getId("umeng_comm_user_recommend")).setOnClickListener(this);
        findViewById(ResFinder.getId("user_have_login")).setOnClickListener(this);
        findViewById(ResFinder.getId("user_haveno_login")).setOnClickListener(this);
//        findViewById(ResFinder.getId("umeng_comm_setting_recommend")).setOnClickListener(this);
//        findViewById(ResFinder.getId("umeng_comm_friends")).setOnClickListener(this);
//        findViewById(ResFinder.getId("umeng_comm_myfocustopic")).setOnClickListener(this);
//        findViewById(ResFinder.getId("umeng_comm_mypics")).setOnClickListener(this);
//        findViewById(ResFinder.getId("umeng_comm_favortes")).setOnClickListener(this);
//        findViewById(ResFinder.getId("umeng_comm_notification")).setOnClickListener(this);
//        findViewById(ResFinder.getId("umeng_comm_nearby_recommend")).setOnClickListener(this);
//        findViewById(ResFinder.getId("umeng_comm_nearby_user")).setOnClickListener(this);
//        findViewById(ResFinder.getId("umeng_comm_realtime")).setOnClickListener(this);
        typeContainer = (LinearLayout) findViewById(ResFinder.getId("user_type_icon_container"));
        // 右上角的通知
        //findViewById(ResFinder.getId("umeng_comm_title_notify_btn")).setOnClickListener(this);
        findViewById(ResFinder.getId("umeng_comm_title_setting_btn")).setOnClickListener(this);
        // 未读消息红点
//        mMsgBadgeView = findViewById(ResFinder.getId("umeng_comm_notify_badge_view"));
//        mMsgBadgeView.setVisibility(View.GONE);

        // 未读系统通知的红点
//        mNotifyBadgeView = findViewById(ResFinder.getId("umeng_comm_badge_view"));

        TextView textView = (TextView) findViewById(ResFinder.getId("umeng_comm_title_tv"));
        textView.setText(ResFinder.getString("umeng_comm_mine"));
//        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        listContainer = (LinearLayout)findViewById(ResFinder.getId("lists"));
        initList();
        parseIntentData();


            setupUnreadFeedMsgBadge();


        mUser = CommonUtils.getLoginUser(this);
        registerInitSuccessBroadcast();

    }

    protected void initList(){

        TextView first = new TextView(this);
        first.setBackgroundColor(ColorQueque.getColor("umeng_comm_feed_list_bg"));
        first.setTextSize(14);
        first.setPadding(DeviceUtils.dp2px(this,10),DeviceUtils.dp2px(this,5),0,DeviceUtils.dp2px(this,5));
        first.setTextColor(ColorQueque.getColor("umeng_comm_active_user_name_textcolor"));
        first.setText(ResFinder.getString("umeng_comm_mine"));
        listContainer.addView(first);
        View divide = new View(this);
        divide.setBackgroundColor(ColorQueque.getColor("umeng_comm_divider"));
        LinearLayout.LayoutParams dlp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,DeviceUtils.dp2px(this,1));
        divide.setLayoutParams(dlp);

        listContainer.addView(divide);

        ListView listView1 = new ListView(this);
        listView1.setDividerHeight(0);
        ArrayList<String> firstlist = new ArrayList<String>();
        firstlist.add(ResFinder.getString("umeng_comm_user_notification"));
        firstlist.add(ResFinder.getString("umeng_comm_user_favorites"));
        firstlist.add(ResFinder.getString("umeng_comm_recommend_friends"));
        firstlist.add(ResFinder.getString("umeng_comm_myfocus"));
        firstlist.add(ResFinder.getString("umeng_comm_mypics"));
        MyAdapterInFind adapterInFind = new MyAdapterInFind(this,firstlist);
        if (firstlist.contains(ResFinder.getString("umeng_comm_user_notification"))){
            msgAdapter = adapterInFind;
        }
        listView1.setAdapter(adapterInFind);
        listView1.setVerticalScrollBarEnabled(false);
        listView1.setOnItemClickListener(listener);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, firstlist.size()*DeviceUtils.dp2px(this,48));//每行固定高48
        listContainer.addView(listView1,lp);


        TextView second = new TextView(this);
        second.setBackgroundColor(ColorQueque.getColor("umeng_comm_feed_list_bg"));
        second.setTextSize(14);
        second.setPadding(10,5,0,5);
        second.setTextColor(ColorQueque.getColor("umeng_comm_active_user_name_textcolor"));
        second.setText(ResFinder.getString("umeng_comm_recommend"));
        listContainer.addView(second);

        View divide2 = new View(this);
        divide2.setBackgroundColor(ColorQueque.getColor("umeng_comm_divider"));
        divide2.setLayoutParams(dlp);
        listContainer.addView(divide2);

        ListView listView2 = new ListView(this);
        ArrayList<String> secondlist = new ArrayList<String>();
        secondlist.add(ResFinder.getString("umeng_comm_recommend_nearby"));
        secondlist.add(ResFinder.getString("umeng_comm_nearby_user"));
        secondlist.add(ResFinder.getString("umeng_comm_realtime"));
        secondlist.add(ResFinder.getString("umeng_comm_recommend_user"));
        secondlist.add(ResFinder.getString("umeng_comm_recommend_topic"));
        MyAdapterInFind adapterInFind2 = new MyAdapterInFind(this,secondlist);
        if (secondlist.contains(ResFinder.getString("umeng_comm_user_notification"))){
            msgAdapter = adapterInFind2;
        }
        listView2.setDividerHeight(0);
        listView2.setAdapter(adapterInFind2);
        listView2.setVerticalScrollBarEnabled(false);
        listView2.setOnItemClickListener(listener);
        LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, secondlist.size()*DeviceUtils.dp2px(this,48));//每行固定高48
        listContainer.addView(listView2,lp2);
//        for (String temp:firstlist){
//            temp.equals(ResFinder.getString("umeng_comm_user_notification"));
//        }


    }
    AdapterView.OnItemClickListener listener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (view.getTag().equals(ResFinder.getString("umeng_comm_user_notification"))){
                if (!CommonUtils.isLogin(FindBaseActivity.this)) {
                    CommunitySDKImpl.getInstance().login(FindBaseActivity.this, new LoginListener() {
                        @Override
                        public void onStart() {
                            processDialog.show();
                        }

                        @Override
                        public void onComplete(int stCode, CommUser userInfo) {
                            processDialog.dismiss();
                            if (stCode == 0) {
                                gotoFeedNewMsgActivity();
                            }
                        }
                    });
                } else {

                    gotoFeedNewMsgActivity();
                }
            }else if (view.getTag().equals(ResFinder.getString("umeng_comm_user_favorites"))){
                if (!CommonUtils.isLogin(FindBaseActivity.this)) {
                    CommunitySDKImpl.getInstance().login(FindBaseActivity.this, new LoginListener() {
                        @Override
                        public void onStart() {
                            processDialog.show();
                        }

                        @Override
                        public void onComplete(int stCode, CommUser userInfo) {
                            processDialog.dismiss();
                            if (stCode == 0) {
                                showFavoritesFeed();
                            }
                        }
                    });
                } else {

                    showFavoritesFeed();
                }
            }else if (view.getTag().equals(ResFinder.getString("umeng_comm_recommend_friends"))){
                if (!CommonUtils.isLogin(FindBaseActivity.this)) {
                    CommunitySDKImpl.getInstance().login(FindBaseActivity.this, new LoginListener() {
                        @Override
                        public void onStart() {
                            processDialog.show();
                        }

                        @Override
                        public void onComplete(int stCode, CommUser userInfo) {

                            if (stCode == 0) {
                                showFriendsFragment();
                            }
                            processDialog.dismiss();
                        }
                    });
                } else {

                    showFriendsFragment();
                }
            }else if (view.getTag().equals(ResFinder.getString("umeng_comm_myfocus"))){
                if (!CommonUtils.isLogin(FindBaseActivity.this)) {
                    CommunitySDKImpl.getInstance().login(FindBaseActivity.this, new LoginListener() {
                        @Override
                        public void onStart() {
                            processDialog.show();
                        }

                        @Override
                        public void onComplete(int stCode, CommUser userInfo) {
                            processDialog.dismiss();
                            if (stCode == 0) {
                                gotoMyFollowActivity();
                            }
                        }
                    });
                } else {
                    gotoMyFollowActivity();
                }
            }else if (view.getTag().equals(ResFinder.getString("umeng_comm_mypics"))){
                if (!CommonUtils.isLogin(FindBaseActivity.this)) {
                    CommunitySDKImpl.getInstance().login(FindBaseActivity.this, new LoginListener() {
                        @Override
                        public void onStart() {
                            processDialog.show();
                        }

                        @Override
                        public void onComplete(int stCode, CommUser userInfo) {
                            processDialog.dismiss();
                            if (stCode == 0) {
                                gotoMyPicActivity();
                            }
                        }
                    });
                } else {

                    gotoMyPicActivity();
                }
            }else if (view.getTag().equals(ResFinder.getString("umeng_comm_recommend_nearby"))){
                if (!CommonUtils.isLogin(FindBaseActivity.this)) {
                    CommunitySDKImpl.getInstance().login(FindBaseActivity.this, new LoginListener() {
                        @Override
                        public void onStart() {
                            processDialog.show();
                        }

                        @Override
                        public void onComplete(int stCode, CommUser userInfo) {

                            processDialog.dismiss();
                            if (stCode == 0) {
                                showNearbyFeed();
                            }
                        }
                    });
                } else {

                    showNearbyFeed();
                }
            }else if (view.getTag().equals(ResFinder.getString("umeng_comm_nearby_user"))){
                if (!CommonUtils.isLogin(FindBaseActivity.this)) {
                    CommunitySDKImpl.getInstance().login(FindBaseActivity.this, new LoginListener() {
                        @Override
                        public void onStart() {
                            processDialog.show();
                        }

                        @Override
                        public void onComplete(int stCode, CommUser userInfo) {

                            processDialog.dismiss();
                            if (stCode == 0) {
                                showNearByUser();
                            }
                        }
                    });
                } else {
                    showNearByUser();
                }
            }else if (view.getTag().equals(ResFinder.getString("umeng_comm_realtime"))){
                showRealTimeFeed();
            }else if (view.getTag().equals(ResFinder.getString("umeng_comm_recommend_user"))){
                showRecommendUserFragment();
            }else if (view.getTag().equals(ResFinder.getString("umeng_comm_recommend_topic"))){
                showRecommendTopic();
            }
        }
    };
    protected void initUserInfo() {
        if (CommonUtils.isLogin(this)) {
            CommUser user = CommonUtils.getLoginUser(this);

            findViewById(ResFinder.getId("user_have_login")).setVisibility(View.VISIBLE);
            findViewById(ResFinder.getId("user_haveno_login")).setVisibility(View.GONE);
            ((RoundImageView) findViewById(ResFinder.getId("userinfo_headicon"))).setImageDrawable(ColorQueque.getDrawable("umeng_comm_defaul_icon"));
            ImgDisplayOption option = ImgDisplayOption.getOptionByGender(user.gender);
            ((RoundImageView) findViewById(ResFinder.getId("userinfo_headicon"))).setImageUrl(user.iconUrl, option);
            ((TextView) findViewById(ResFinder.getId("user_name_tv"))).setText(user.name);

            StringBuffer content = new StringBuffer(ResFinder.getString("umeng_comm_my_fans"));
            content.append(" ").append(CommonUtils.getLimitedCount(user.fansCount));
            ((TextView) findViewById(ResFinder.getId("user_fanscount"))).setText(content.toString());

            content.delete(0, content.length());
            content.append(ResFinder.getString("umeng_comm_followed_user"));
            content.append(" ").append(CommonUtils.getLimitedCount(user.followCount));
            ((TextView) findViewById(ResFinder.getId("user_focuscount"))).setText(content.toString());

            content.delete(0, content.length());
            content.append(ResFinder.getString("umeng_comm_user_socre"));
            content.append(" ").append(CommonUtils.getLimitedCount(user.point));
            ((TextView) findViewById(ResFinder.getId("user_score"))).setText(content.toString());
        } else {
            findViewById(ResFinder.getId("user_haveno_login")).setVisibility(View.VISIBLE);
            findViewById(ResFinder.getId("user_have_login")).setVisibility(View.GONE);
            ((RoundImageView) findViewById(ResFinder.getId("userinfo_headicon_nologin"))).setImageDrawable(ColorQueque.getDrawable("umeng_comm_defaul_icon"));
            ((TextView) findViewById(ResFinder.getId("user_name_tv_nologin"))).setText("立即登陆");
        }
        mUser = CommConfig.getConfig().loginedUser;
        displayUserMedal();
        if(mUser.medals == null || mUser.medals.isEmpty()){
            loadUserFromDB(mUser.id);
        }
    }

    private void displayUserMedal(){
        CommUser user = CommConfig.getConfig().loginedUser;
        if(user.medals == null || user.medals.isEmpty()){
            typeContainer.setVisibility(View.GONE);
        }else {
            typeContainer.setVisibility(View.VISIBLE);
            UserTypeUtil.SetUserType(FindBaseActivity.this, user, typeContainer);
        }
    }

    private void loadUserFromDB(final String uId){
        DatabaseAPI.getInstance().getUserDBAPI().loadUserFromDB(uId, new Listeners.SimpleFetchListener<CommUser>() {
            @Override
            public void onComplete(CommUser user) {
                if(!FindBaseActivity.this.isFinishing()){
                    if(user != null){
                        CommConfig.getConfig().loginedUser.medals = user.medals;
                        displayUserMedal();
                    }
                }
            }
        });
    }

    protected void getLayout() {
        setContentView(ResFinder.getLayout("umeng_comm_find_layout"));
    }

    protected void parseIntentData() {
        mUser = getIntent().getExtras().getParcelable(Constants.TAG_USER);
        mContainerClass = getIntent().getExtras().getString(Constants.TYPE_CLASS);
        mUnReadMsg = CommConfig.getConfig().mMessageCount;
    }

    /**
     * 设置通知红点</br>
     */


    /**
     * 设置消息数红点</br>
     */
    protected void setupUnreadFeedMsgBadge() {
        if (msgAdapter!=null){
            msgAdapter.setUnReadcount(mUnReadMsg.unReadTotal);
            msgAdapter.notifyDataSetChanged();
        }

//        if (mUnReadMsg.unReadTotal > 0) {
//            if (CommonUtils.isLogin(this)) {
//                mMsgBadgeView.setVisibility(View.VISIBLE);
//            } else {
//                mMsgBadgeView.setVisibility(View.GONE);
//            }
//        } else {
//            mMsgBadgeView.setVisibility(View.GONE);
//        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == ResFinder.getId("umeng_comm_title_back_btn")) { // 返回事件
            finish();
        } else if (id == ResFinder.getId("user_have_login")) { // 个人中心
            gotoUserInfoActivity();
        } else if (id == ResFinder.getId("user_haveno_login")) { // 个人中心
            CommunitySDKImpl.getInstance().login(FindBaseActivity.this, new LoginListener() {
                @Override
                public void onStart() {
                    processDialog.show();
                }

                @Override
                public void onComplete(int stCode, CommUser userInfo) {
                    initUserInfo();
                    processDialog.dismiss();
                }
            });
        } else if (id == ResFinder.getId("umeng_comm_title_setting_btn")) {
            if (!CommonUtils.isLogin(FindBaseActivity.this)) {
                final ProgressDialog mProgressDialog = new ProgressDialog(this);
                mProgressDialog.setCancelable(true);
                mProgressDialog.setCanceledOnTouchOutside(false);
                CommunitySDKImpl.getInstance().login(FindBaseActivity.this, new LoginListener() {
                    @Override
                    public void onStart() {
                        processDialog.show();
                    }

                    @Override
                    public void onComplete(int stCode, CommUser userInfo) {
                        if (stCode == 0) {
                            Intent setting = new Intent(FindBaseActivity.this, SettingActivity.class);
                            setting.putExtra(Constants.TYPE_CLASS, mContainerClass);
                            startActivity(setting);
                        }
                        processDialog.dismiss();
                    }
                });
            } else {
                Intent setting = new Intent(this, SettingActivity.class);
                setting.putExtra(Constants.TYPE_CLASS, mContainerClass);
                startActivity(setting);
            }
        }
    }

    protected abstract void gotoMyFollowActivity();

    protected abstract void gotoMyPicActivity();

    protected abstract void gotoNotificationActivity();

    protected abstract void gotoFeedNewMsgActivity();

    @Override
    protected void onResume() {
        super.onResume();
        initUserInfo();

            setupUnreadFeedMsgBadge();

    }

    /**
     * 跳转到用户中心Activity</br>
     */
    protected abstract void gotoUserInfoActivity();

    /**
     * 显示附件推荐Feed</br>
     */
    protected abstract void showNearbyFeed();

    /**
     * 显示附近用户Feed</br>
     */
    protected abstract void showNearByUser();

    /**
     * 显示实时内容的Fragment</br>
     */
    protected abstract void showRealTimeFeed();

    /**
     * 显示收藏Feed</br>
     */
    protected abstract void showFavoritesFeed();

    /**
     * 显示推荐话题的Dialog</br>
     */
    protected abstract void showRecommendTopic();

    /**
     * 隐藏发现页面，显示fragment</br>
     *
     * @param fragment
     */
    protected abstract void showCommFragment(Fragment fragment);

    /**
     * 隐藏fragment，显示发现页面</br>
     */
    protected void showFindPage(){
        initUserInfo();
    }

    /**
     * 显示朋友圈Fragment</br>
     */
    protected abstract void showFriendsFragment();

    /**
     * 显示推荐用户fragment</br>
     */
    protected abstract void showRecommendUserFragment();

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK
                && findViewById(ResFinder.getId("container")).getVisibility() == View.VISIBLE) {
            findViewById(ResFinder.getId("umeng_comm_find_base")).setVisibility(View.VISIBLE);
            findViewById(ResFinder.getId("container")).setVisibility(View.GONE);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 注册登录成功时的广播</br>
     */
    protected void registerInitSuccessBroadcast() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.ACTION_INIT_SUCCESS);
        LocalBroadcastManager.getInstance(this).registerReceiver(mInitConfigReceiver,
                filter);
    }

    protected BroadcastReceiver mInitConfigReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            mUnReadMsg = CommConfig.getConfig().mMessageCount;

                setupUnreadFeedMsgBadge();

        }
    };

    @Override
    public void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mInitConfigReceiver);
        super.onDestroy();
    }

}
