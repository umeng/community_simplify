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

package com.umeng.simplify.ui.activities;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.umeng.comm.core.beans.CommConfig;
import com.umeng.comm.core.beans.CommUser;
import com.umeng.comm.core.beans.CommUser.Permisson;
import com.umeng.comm.core.beans.FeedItem;
import com.umeng.comm.core.beans.ImageItem;
import com.umeng.comm.core.beans.LocationItem;
import com.umeng.comm.core.beans.Topic;
import com.umeng.comm.core.constants.Constants;
import com.umeng.comm.core.listeners.Listeners.SimpleFetchListener;
import com.umeng.comm.core.sdkmanager.ImagePickerManager;
import com.umeng.comm.core.utils.CommonUtils;
import com.umeng.comm.core.utils.DeviceUtils;
import com.umeng.comm.core.utils.Log;
import com.umeng.comm.core.utils.ResFinder;
import com.umeng.comm.core.utils.ToastMsg;
import com.umeng.common.ui.activities.BaseFragmentActivity;
import com.umeng.common.ui.dialogs.ConfirmDialog;
import com.umeng.common.ui.dialogs.CustomCommomDialog;
import com.umeng.common.ui.dialogs.CustomToast;
import com.umeng.common.ui.fragments.TopicPickerFragment;
import com.umeng.common.ui.listener.FrinendClickSpanListener;
import com.umeng.common.ui.listener.TopicClickSpanListener;
import com.umeng.common.ui.mvpview.MvpPostFeedActivityView;
import com.umeng.common.ui.presenter.impl.TakePhotoPresenter;
import com.umeng.common.ui.util.ContentChecker;
import com.umeng.common.ui.util.FeedViewRender;
import com.umeng.common.ui.widgets.FeedEditText;
import com.umeng.simplify.ui.adapters.ImageSelectedAdapter;
import com.umeng.simplify.ui.dialogs.SelectTopicDialog;
import com.umeng.simplify.ui.presenter.impl.FeedPostPresenter;

import java.util.ArrayList;
import java.util.List;

/**
 * 发布feed的Activity
 */
public class PostFeedActivity extends BaseFragmentActivity implements OnClickListener,
        MvpPostFeedActivityView, ImageSelectedAdapter.OnImageDeleteListener {

    /**
     * 内容编辑框，最多300字
     */
    protected FeedEditText mEditText;

    protected TextView mTopicTv;

    /**
     * 选择的图片的GridView
     */
    protected GridView mGridView;
    private View mGriedViewHolder;
    /**
     * 保存已经选择的图片的路径
     */
    // protected List<String> mImageSelectedAdapter.getDataSource() = new
    // ArrayList<String>();
    /**
     * 显示已经选择的图片的Adapter
     */
    private ImageSelectedAdapter mImageSelectedAdapter;
//    /**
//     * 位置
//     */
//    protected Location mLocation;
    /**
     * 通过拍照获取到的图片地址
     */
    // private String mNewImagePath;
    /**
     * 已选择的话题
     */
    protected List<Topic> mSelecteTopics = new ArrayList();
    //    /**
//     * 保存已经@的好友
//     */
    protected List<CommUser> mSelectFriends = new ArrayList();
//    /**
//     * 我的位置TextView
//     */
//    protected TextView mLocationTv;
//    /**
//     * 选择好友的dialog
//     */
//    private AtFriendDialog mAtFriendDlg;
//    /**
//     * 地理位置选择dialog
//     */
//    private LocationPickerDlg mLocationPickerDlg;
//    /**
//     * 保存地理位置的list
//     */
//    protected List<LocationItem> mLocationItems = new ArrayList<LocationItem>();
    /**
     * 选择话题的Fragment
     */
    private TopicPickerFragment mTopicFragment;
//    /**
//     * 选择话题的ToggleButton
//     */
//    private ToggleButton mTopicButton;
    /**
     * 选择图片的ImageButton
     */
    private ImageButton mPhotoButton;

    private TextView edWatcher;
    private String format;
    private String mSelectedTopicStr;
    private TextView imgWatcher;

    private ImageView imageBtn;

    private ImageView topicBtn;
//    /**
//     * 地理位置的icon
//     */
//    private ImageView mLocIcon;
//    protected EmojiBorad mEmojiBoard;
//    private View mTopicLayout;
//    private View mLocationLayout;
//    private TextView topicTv;
//    private View mTopicIcon;
//    private ImageView topicwarnImg;
    /**
     * 加载地理位置时的Progress
     */
//    private ProgressBar mLocProgressBar;
//    protected ImageView mEmojiImageView;
    protected String TAG = PostFeedActivity.class.getSimpleName();

    /**
     * 是否是发布失败重新发布
     */
    private boolean isRepost = false;


//    protected TopicTipView mTopicTipView;

    private static final String EDIT_CONTENT_KEY = "edit_content_key";
    private static final String EDIT_TITLE_KEY = "edit_content_key";

    FeedPostPresenter mPostPresenter;

    private Dialog mProgressDialog;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        if (getIntent() != null && getIntent().getExtras() != null) {
            Topic topic = getIntent().getExtras().getParcelable(Constants.TAG_TOPIC);
            mSelecteTopics.add(topic);
        }

        setContentView(ResFinder.getLayout("umeng_simplify_post_feed_layout"));
        //setFragmentContainerId(ResFinder.getId("umeng_comm_select_layout"));
        initViews();

        initPresenter();
        Bundle extraBundle = getIntent().getExtras();
        if (extraBundle == null) {
            return;
        }
        isRepost = extraBundle.getBoolean(Constants.POST_FAILED, false);
        // 设置是否是重新发送
        mPostPresenter.setRepost(isRepost);
    }

    private void initPresenter() {
        mPostPresenter = new FeedPostPresenter(this, new ContentChecker(mSelecteTopics,
                mSelectFriends));
        mPostPresenter.attach(this);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        String content = savedInstanceState.getString(EDIT_CONTENT_KEY);
        if (!TextUtils.isEmpty(content)) {
            mEditText.setText(content);
        }
    }

    private boolean isCharsOverflow(String extraText) {
        int extraTextLen = 0;
        if (!TextUtils.isEmpty(extraText)) {
            extraTextLen = extraText.length();
        }
        int len = mEditText.getText().length();
        return len + extraTextLen >= CommConfig.getConfig().mFeedLen;
    }

    private boolean isCharsOverflow(int extraTextLen) {
        int len = mEditText.getText().length();
        return len + extraTextLen >= CommConfig.getConfig().mFeedLen;
    }

    private void showCharsOverflowTips() {
        String tips = ResFinder.getString("umeng_comm_overflow_tips");
        CustomToast.showTopicDefaultMsg(PostFeedActivity.this, tips);
    }

    /**
     * 初始化相关View
     */
    protected void initViews() {
        // 发送和回退按钮
        findViewById(ResFinder.getId("umeng_comm_post_ok_btn")).setOnClickListener(this);
        findViewById(ResFinder.getId("umeng_comm_post_back_btn")).setOnClickListener(this);

        edWatcher = (TextView) findViewById(ResFinder.getId("umeng_text_watcher"));
        imgWatcher = (TextView) findViewById(ResFinder.getId("umeng_image_watcher"));

        initEditView();

        mGridView = (GridView) findViewById(ResFinder.getId("umeng_comm_prev_images_gv"));
        mGriedViewHolder = findViewById(ResFinder.getId("umeng_simplify_images_gv_holder"));
        initSelectedImageAdapter();

        format = ResFinder.getString("umeng_comm_img_watcher");
        mSelectedTopicStr = ResFinder.getString("umeng_simplify_selected_topic");

        String result = String.format(format, getImageListSize());


        imgWatcher.setText(result);
        if (getImageListSize() == 9) {
            mGriedViewHolder.setVisibility(View.INVISIBLE);
        } else {
            mGriedViewHolder.setVisibility(View.VISIBLE);
        }
        imageBtn = (ImageView) findViewById(ResFinder.getId("umeng_image_picker"));
        topicBtn = (ImageView) findViewById(ResFinder.getId("topic_show_btn"));

        imageBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                pickImages();
            }
        });

        if(mSelecteTopics.isEmpty()){
            topicBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    showTopicPickerDlg();
                }
            });
            mTopicTv.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    showTopicPickerDlg();
                }
            });
            showTopicPickerDlg();
        }else {
            showSelectedTopic(mSelecteTopics.get(0));
        }

        mProgressDialog = new CustomCommomDialog(this, ResFinder.getString("umeng_comm_discuss_post_feed_loading"));
        mProgressDialog.setCancelable(false);
    }


    @Override
    protected void onResume() {
        super.onResume();
        //showInputMethod(mEditText);
        mImageLoader.resume();
        mTakePhotoPresenter.attach(this);
    }

    private void showKeyboard() {
        //mFragmentLatout.setVisibility(View.GONE);
        showInputMethod(mEditText);
    }

    /**
     * 初始化EditView并设置回调</br>
     */
    private void initEditView() {

        mEditText = (FeedEditText) findViewById(ResFinder.getId(
                "umeng_comm_post_msg_edittext"));
        mEditText.setFocusableInTouchMode(true);
        //mEditText.requestFocus();
        //mEditText.setMinimumHeight(DeviceUtils.dp2px(this, 150));

        mEditText.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mEditText.mCursorIndex = mEditText.getSelectionStart();

                //mFragmentLatout.setVisibility(View.GONE);
                //mTopicButton.setChecked(false);
            }
        });

        mEditText.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return false;
            }
        });

        mEditText.setOnFocusChangeListener(new View.
                OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {

                }
            }
        });
        mTopicTv = (TextView) findViewById(ResFinder.getId("umeng_comm_post_msg_title"));

        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                edWatcher.setText(mEditText.getText().length() + "/" + 140);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }


    /**
     * 准备feed数据</br>
     */
    protected FeedItem prepareFeed() {
        FeedItem mNewFeed = new FeedItem();
        mNewFeed.text = mEditText.getText().toString().trim();

//        if (mTopic != null) {
//            List<Topic> list = new ArrayList<Topic>();
//            list.add(mTopic);
//            mNewFeed.topics = list;
//        }

        for (String url : mImageSelectedAdapter.getDataSource()) {
            if (!url.equals(Constants.ADD_IMAGE_PATH_SAMPLE)) {
                // 图片地址
                mNewFeed.imageUrls.add(new ImageItem("", "", url));
            }
        }
        // 话题
        mNewFeed.topics.addAll(mSelecteTopics);
        // @好友
        mNewFeed.atFriends.addAll(mSelectFriends);
        // 发表的用户
        mNewFeed.creator = CommConfig.getConfig().loginedUser;
        mNewFeed.type = mNewFeed.creator.permisson == Permisson.ADMIN ? 1 : 0;
        Log.d(TAG, " @@@ my new Feed = " + mNewFeed);
        return mNewFeed;
    }

    /**
     * 清除状态</br>
     */
    @Override
    public void clearState() {
        mTopicTv.setText("");
        mEditText.setText("");
        mEditText.mAtMap.clear();
        mEditText.mTopicMap.clear();
        mImageSelectedAdapter.getDataSource().clear();
//        mTopicButton.setChecked(false);
        // mPhotoButton.setChecked(false);
    }

    @Override
    public void restoreFeedItem(FeedItem feedItem) {
        mEditText.setText(feedItem.text);

        mImageSelectedAdapter.getDataSource().clear();

        int count = feedItem.imageUrls.size();
        for (int i = 0; i < count; i++) {
            // 图片
            mImageSelectedAdapter.getDataSource().add(feedItem.imageUrls.get(i).originImageUrl);
        }
        // 图片
        if (mImageSelectedAdapter.getDataSource().size() < 9) {
            mImageSelectedAdapter.getDataSource().add(0, Constants.ADD_IMAGE_PATH_SAMPLE);
        }
        mImageSelectedAdapter.notifyDataSetChanged();
        // 好友
        mSelectFriends.addAll(feedItem.atFriends);
        // 话题
        mSelecteTopics.addAll(feedItem.topics);
        if (!feedItem.topics.isEmpty()) {
            mTopicTv.setText(feedItem.topics.get(0).name);
        }
        FeedViewRender.parseTopicsAndFriends(mEditText, feedItem, new TopicClickSpanListener() {
            @Override
            public void onClick(Topic topic) {
                Intent intent = new Intent(PostFeedActivity.this,
                        TopicDetailActivity.class);
                intent.putExtra(Constants.TAG_TOPIC, topic);
                PostFeedActivity.this.startActivity(intent);
            }
        }, new FrinendClickSpanListener() {
            @Override
            public void onClick(CommUser user) {
                Intent intent = new Intent(PostFeedActivity.this,
                        UserInfoActivity.class);
                intent.putExtra(Constants.TAG_USER, user);
                PostFeedActivity.this.startActivity(intent);
            }
        });
        // 设置光标位置
        mEditText.setSelection(mEditText.getText().length());
    }

    @Override
    public void changeLocLayoutState(Location location, List<LocationItem> locationItems) {

    }

    private void changeLoctionTextViewState(boolean isSelected) {

    }

    /**
     * 用户点击back按钮。</br>
     */
    private void dealBackLogic() {
        //mFragmentLatout.setVisibility(View.GONE);
        hideInputMethod(mEditText);
        mPostPresenter.handleBackKeyPressed();
        this.finish();
    }

    TakePhotoPresenter mTakePhotoPresenter = new TakePhotoPresenter();

    @Override
    public void onClick(View v) {
        final int id = v.getId();
        if (ResFinder.getId("umeng_comm_post_ok_btn") == id) { // 点击发送按钮
            // TODO 统一处理逻辑
            if (mSelecteTopics.isEmpty()) {
                CustomToast.showTopicDefaultMsg(PostFeedActivity.this);
                hideInputMethod(mEditText);
                return;
            }
            postFeed(prepareFeed());
        } else if (ResFinder.getId("umeng_comm_post_back_btn") == id) { // 点击back按钮
            closeActivity();
        }
//        else if (ResFinder.getId("umeng_comm_take_photo_btn") == id) { // 拍照按钮
//            mTakePhotoPresenter.takePhoto();
//            changeButtonStatus(false, false);
//        } else if (ResFinder.getId("umeng_comm_select_location_btn") == id) { // 选择位置
//            showLocPickerDlg();
//            changeButtonStatus(false, false);
//        } else if (ResFinder.getId("umeng_comm_add_image_btn") == id) { // 添加图片
//            pickImages();
//            changeButtonStatus(true, false);
//        } else if (ResFinder.getId("umeng_comm_at_friend_btn") == id) { // @好友
//            showAtFriendsDialog();
//            changeButtonStatus(false, false);
//        } else if (ResFinder.getId("umeng_comm_pick_topic_btn") == id) { // 选择话题
//            showTopicFragment();
//            changeButtonStatus(false, true);
//        } else if (ResFinder.getId("umeng_comm_select_emoji_btn") == id) { // 选择话题
//
//        }
    }

    private void pickImages() {
        ImagePickerManager
                .getInstance()
                .getCurrentSDK()
                .jumpToPickImagesPage(this,
                        (ArrayList<String>) mImageSelectedAdapter.getDataSource());
    }

    protected void postFeed(FeedItem feedItem) {
        mPostPresenter.postNewFeed(feedItem, false);
    }

    @Override
    public void startPostFeed() {
        hideInputMethod(mEditText);
        if (!mProgressDialog.isShowing()) {
            mProgressDialog.show();
        }
    }

    @Override
    public void postFeedComplete(boolean success) {
        if (success) {
            finish();
        } else {
            mProgressDialog.dismiss();
        }
    }

    /**
     * 已选择图片显示的Adapter
     */
    private void initSelectedImageAdapter() {
        mImageSelectedAdapter = new ImageSelectedAdapter(PostFeedActivity.this);
        mImageSelectedAdapter.setOnImageDeleteListener(this);
        mImageSelectedAdapter.getDataSource().add(0, Constants.ADD_IMAGE_PATH_SAMPLE);
        mImageSelectedAdapter.reverseAddBtnPositon();
        // 设置选择item时得背景为透明
        mGridView.setSelector(new ColorDrawable(Color.TRANSPARENT));
        mGridView.setAdapter(mImageSelectedAdapter);
        mGridView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {

                String clickImageUrl = mImageSelectedAdapter.getItem(position);
                boolean isAddImage = Constants.ADD_IMAGE_PATH_SAMPLE.equals(clickImageUrl);
                if (isAddImage) { // 如果触发的是添加图片事件，则显示选择图片的Fragment
                    //pickImages();
                    pickImages();
                }
            }
        });

    }

    private SelectTopicDialog mSelectTopicDlg;

    /**
     * 显示选择话题的Dialog</br>
     */
    private void showTopicPickerDlg() {

        if (mSelectTopicDlg == null) {
            mSelectTopicDlg = new SelectTopicDialog(PostFeedActivity.this, ResFinder.getStyle(
                    "umeng_comm_dialog_fullscreen"));

            mSelectTopicDlg.setOwnerActivity(PostFeedActivity.this);
            // 数据获取监听器
            mSelectTopicDlg.setDataListener(new SimpleFetchListener<Topic>() {

                @Override
                public void onComplete(Topic topic) {
                    if (topic != null) {
                        mSelecteTopics.clear();
                        mSelecteTopics.add(topic);
                        showSelectedTopic(topic);
                    }else {
                        if(mSelecteTopics.isEmpty()) {
                            PostFeedActivity.this.finish();
                        }
                    }
                }
            });

            mSelectTopicDlg.setOnKeyListener(new DialogInterface.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                    if(keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0){
                        if(mSelecteTopics.isEmpty()) {
                            PostFeedActivity.this.finish();
                        }
                        return true;
                    }
                    return false;
                }
            });
        }
        mSelectTopicDlg.show();
    }

    private void showSelectedTopic(Topic topic) {
        if (!TextUtils.isEmpty(topic.name)) {
            String topicName = topic.name;
            if(!TextUtils.isEmpty(Constants.TOPIC_GAT)){
                topicName = topicName.substring(1, topic.name.length() - 1);
            }
            topicName = String.format(mSelectedTopicStr, topicName);
            mTopicTv.setText(topicName);
        }
    }


    /**
     * 显示选择地理位置的Dialog</br>
     */


    /**
     * 移除字符</br>
     *
     * @param c
     */
    private void removeChar(char c) {
        Editable editable = mEditText.getText();
        if (editable.length() <= 0) {
            return;
        }
        if (editable.charAt(editable.length() - 1) == c) {
            editable.delete(editable.length() - 1, editable.length());
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        //mFragmentLatout.setVisibility(View.GONE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
        if(mSelectTopicDlg != null){
            mSelectTopicDlg.dismiss();
        }
        mTakePhotoPresenter.detach();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        String content = mEditText.getText().toString();
        if (!TextUtils.isEmpty(content)) {
            outState.putString(EDIT_CONTENT_KEY, content);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode != RESULT_OK) {
            return;
        }

        // 处理图片选择
        onPickedImages(requestCode, data);
        // 将拍照得到的图片添加到gallery中, 并且显示到GridView中
        onTakedPhoto(requestCode);
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void onPickedImages(int requestCode, Intent data) {
        if (requestCode == Constants.PICK_IMAGE_REQ_CODE) {// selected image
            if (data != null && data.getExtras() != null) {
                mImageSelectedAdapter.getDataSource().clear();
                // 获取选中的图片
                List<String> selectedList = ImagePickerManager.getInstance().getCurrentSDK()
                        .parsePickedImageList(data);
                updateImageList(selectedList);


                String result = String.format(format, getImageListSize());


                imgWatcher.setText(result);
                if (getImageListSize() == 9) {
                    mGriedViewHolder.setVisibility(View.INVISIBLE);
                } else {
                    mGriedViewHolder.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    /**
     * Add the picture to the photo gallery. Must be called on all camera images
     * or they will disappear once taken.
     */
    protected void onTakedPhoto(int requestCode) {

        if (requestCode != TakePhotoPresenter.REQUEST_IMAGE_CAPTURE) {
            return;
        }

        String imgUri = mTakePhotoPresenter.updateImageToMediaLibrary();

        List<String> selectedList = mImageSelectedAdapter.getDataSource();
        // 更新媒体库
        selectedList.remove(Constants.ADD_IMAGE_PATH_SAMPLE);
        if (selectedList.size() < 9) {
            selectedList.add(imgUri);
            appendAddImageIfLessThanNine(selectedList);
        } else {
            ToastMsg.showShortMsgByResName("umeng_comm_image_overflow");
        }
        mImageSelectedAdapter.notifyDataSetChanged();
    }

    /**
     * 如果选中的图片小于九张那么在最后添加一个"添加"图片
     *
     * @param selectedList
     */
    private void appendAddImageIfLessThanNine(List<String> selectedList) {
        if (selectedList.size() < 9) {
            if (!selectedList.contains(Constants.ADD_IMAGE_PATH_SAMPLE)) {
                selectedList.add(Constants.ADD_IMAGE_PATH_SAMPLE);
            }
        }
    }

    @Override
    public void canNotPostFeed() {
        if (mImageSelectedAdapter.getCount() < 9) {
            if(!mImageSelectedAdapter.getDataSource().contains(Constants.ADD_IMAGE_PATH_SAMPLE)){
                mImageSelectedAdapter.addToFirst(Constants.ADD_IMAGE_PATH_SAMPLE);
            }
        }
    }


    @Override
    public void onImageDelete() {
//        if(mImageSelectedAdapter.getCount() == 1){
//            mGridView.setVisibility(View.GONE);
//        }
        adjustGridViewHeight();

        String result = String.format(format, getImageListSize());


        imgWatcher.setText(result);
        if (getImageListSize() == 9) {
            mGriedViewHolder.setVisibility(View.INVISIBLE);
        } else {
            mGriedViewHolder.setVisibility(View.VISIBLE);
        }
    }

    private void updateImageList(List<String> selectedList) {
//        if(mGridView.getVisibility() == View.GONE){
//            mGridView.setVisibility(View.VISIBLE);
//        }
        if (selectedList != null) {
            appendAddImageIfLessThanNine(selectedList);
            mImageSelectedAdapter.updateListViewData(selectedList);
        }
        adjustGridViewHeight();
    }

    private int getImageListSize() {
        int sum = 0;
        for (String url : mImageSelectedAdapter.getDataSource()) {
            if (!url.equals(Constants.ADD_IMAGE_PATH_SAMPLE)) {
                sum++;
            }
        }
        return (9 - sum);
    }

    private void adjustGridViewHeight() {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mGridView.getLayoutParams();
        Point point = DeviceUtils.getScreenSize(this);
        // 12dp * 2 + 8dp * 3
        int itemHeight = (point.x - CommonUtils.dip2px(this, 12 * 2 + 4 * 3)) / 4;
        int height;
        if (mImageSelectedAdapter.getCount() < 4) {
            height = itemHeight;
        } else {
            int rowNum = (mImageSelectedAdapter.getCount() - 1) / 4;
            height = (rowNum + 1) * itemHeight + rowNum * CommonUtils.dip2px(this, 4);
        }
        params.height = height;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            closeActivity();
            return true;

        }
        return super.onKeyDown(keyCode, event);
    }

    private void closeActivity() {
        boolean hasTopic = !TextUtils.isEmpty(mTopicTv.getText().toString());
        boolean hasContent = !TextUtils.isEmpty(mEditText.getText().toString());
        boolean hasImg = mImageSelectedAdapter.getDataSource().size() >= 2;
        if (hasTopic || hasContent || hasImg) {
            ConfirmDialog.showDialog(this, ResFinder.getString("umeng_comm_discuss_post_feed_exit"),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dealBackLogic();
                        }
                    }, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
        } else {
            this.finish();
        }
    }


}
