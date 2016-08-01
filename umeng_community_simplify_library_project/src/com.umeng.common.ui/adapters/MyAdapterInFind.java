package com.umeng.common.ui.adapters;

import android.content.Context;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.umeng.comm.core.utils.CommonUtils;
import com.umeng.comm.core.utils.DeviceUtils;

import com.umeng.comm.core.utils.ResFinder;
import com.umeng.common.ui.colortheme.ColorQueque;

import java.util.ArrayList;


/**
 * Created by wangfei on 16/6/29.
 */
public class MyAdapterInFind extends BaseAdapter{
    Context context;
    ArrayList<String> list;
    private int unReadcount;
    public MyAdapterInFind(Context context,ArrayList<String> list){
        this.context = context;
        this.list = list;
    }
    @Override
    public int getCount() {
        return list.size();
    }


    @Override
    public Object getItem(int position) {
        return null;
    }

    public void setUnReadcount(int unReadcount) {
        this.unReadcount = unReadcount;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    public void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            return;
        }
        int totalHeight = 0;
        for (int i = 0; i < listAdapter.getCount(); i++) {
//            View listItem = listAdapter.getView(i, null, listView);
//            listItem.measure(0, 0);  //<span style="font-family: Helvetica, Tahoma, Arial, sans-serif; font-size: 14px; line-height: 25px; text-align: left; ">在还没有构建View 之前无法取得View的度宽。 </span><span style="font-family: Helvetica, Tahoma, Arial, sans-serif; font-size: 14px; line-height: 25px; text-align: left; ">在此之前我们必须选 measure 一下. </span><br style="font-family: Helvetica, Tahoma, Arial, sans-serif; font-size: 14px; line-height: 25px; text-align: left; ">
            totalHeight += DeviceUtils.dp2px(context,48);
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        if (params == null){
            params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        params.height = totalHeight
                + (listView.getDividerHeight() * (listAdapter.getCount() - 1));

        listView.setLayoutParams(params);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView ==null){
            convertView =  LayoutInflater.from(context).inflate(ResFinder.getLayout("umeng_comm_my_adapter"), null);
        }
        String name = list.get(position);
        ImageView icon = (ImageView)convertView.findViewById(ResFinder.getId("umeng_comm_item_icon"));
        icon.setImageDrawable(ColorQueque.getDrawable(getIconName(name)));
        TextView tv = (TextView)convertView.findViewById(ResFinder.getId("umeng_comm_my_tv"));
        tv.setText(name);
        convertView.setTag(name);
        if (name.equals(ResFinder.getString("umeng_comm_user_notification"))){
            View redPoint = convertView.findViewById(ResFinder.getId("umeng_comm_notify_badge_view"));
                    if (unReadcount > 0) {
            if (CommonUtils.isLogin(context)) {
                redPoint.setVisibility(View.VISIBLE);
            } else {
                redPoint.setVisibility(View.GONE);
            }
        } else {
                        redPoint.setVisibility(View.GONE);
        }
        }
        return convertView;
    }
    private String getIconName(String name){
        if (name.equals(ResFinder.getString("umeng_comm_user_notification"))){
            return "umeng_comm_notification_icon";
        }else if(name.equals(ResFinder.getString("umeng_comm_user_favorites"))){
            return "umeng_comm_favortes_icon";
        }else if(name.equals(ResFinder.getString("umeng_comm_recommend_friends"))){
            return  "umeng_comm_firends_icon";
        }else if(name.equals(ResFinder.getString("umeng_comm_myfocus"))){
            return "umeng_comm_mytopics_icon";
        }else if(name.equals(ResFinder.getString("umeng_comm_mypics"))){
            return "umeng_comm_mypics_icon";
        } else if(name.equals(ResFinder.getString("umeng_comm_recommend_nearby"))){
            return "umeng_comm_nearby_content_icon";
        } else if(name.equals(ResFinder.getString("umeng_comm_nearby_user"))){
            return "umeng_comm_nearby_user_icon";
        } else if(name.equals(ResFinder.getString("umeng_comm_realtime"))){
            return "umeng_comm_realtime_icon";
        } else if(name.equals(ResFinder.getString("umeng_comm_recommend_user"))){
            return "umeng_comm_recommend_user_icon";
        } else if(name.equals(ResFinder.getString("umeng_comm_recommend_topic"))){
            return "umeng_comm_recommend_topic_icon";
        }
        return "umeng_comm_mypics_icon";
    }
//    private String getTag(String name){
//        if (name.equals(ResFinder.getString("umeng_comm_user_notification"))){
//            return "umeng_comm_notification_icon";
//        }else if(name.equals(ResFinder.getString("umeng_comm_user_favorites"))){
//            return "umeng_comm_favortes_icon";
//        }else if(name.equals(ResFinder.getString("umeng_comm_recommend_friends"))){
//            return  "umeng_comm_firends_icon";
//        }else if(name.equals(ResFinder.getString("umeng_comm_myfocus"))){
//            return "umeng_comm_mytopics_icon";
//        }else if(name.equals(ResFinder.getString("umeng_comm_mypics"))){
//            return "umeng_comm_mypics_icon";
//        } else if(name.equals(ResFinder.getString("umeng_comm_recommend_nearby"))){
//            return "umeng_comm_nearby_content_icon";
//        } else if(name.equals(ResFinder.getString("umeng_comm_nearby_user"))){
//            return "umeng_comm_nearby_user_icon";
//        } else if(name.equals(ResFinder.getString("umeng_comm_realtime"))){
//            return "umeng_comm_realtime_icon";
//        } else if(name.equals(ResFinder.getString("umeng_comm_recommend_user"))){
//            return "umeng_comm_recommend_user_icon";
//        } else if(name.equals(ResFinder.getString("umeng_comm_recommend_topic"))){
//            return "umeng_comm_recommend_topic_icon";
//        }
//        return "umeng_comm_mypics_icon";
//    }

}
