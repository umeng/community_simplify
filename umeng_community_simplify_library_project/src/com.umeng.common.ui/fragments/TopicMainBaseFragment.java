package com.umeng.common.ui.fragments;


import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.TextView;

import com.umeng.comm.core.utils.ResFinder;

import com.umeng.common.ui.configure.parseJson;

import java.util.ArrayList;

/**
 * Created by wangfei on 16/7/5.
 */
public abstract class TopicMainBaseFragment extends Fragment {
    protected ArrayList<Fragment> topicBaseFragments = new ArrayList<Fragment>();
    public View mRootView;
    public TextView button1,button3,button4;
    protected View.OnClickListener switchListener;

    public void initSwitchView(){
        if (topicBaseFragments.size()==1){
            button1 = (TextView)mRootView.findViewById(ResFinder.getId("umeng_switch_button_one"));
            button3 = (TextView)mRootView.findViewById(ResFinder.getId("umeng_switch_button_three"));
            button4 = (TextView)mRootView.findViewById(ResFinder.getId("umeng_switch_button_four"));
            View tabBtnContanier = mRootView.findViewById(ResFinder.getId("uemng_switch_button_container"));
            if(tabBtnContanier != null){
                tabBtnContanier.setVisibility(View.GONE);
            }
            button1.setVisibility(View.GONE);
            button3.setVisibility(View.GONE);
            button4.setVisibility(View.GONE);
            button1.setOnClickListener(switchListener);
            button3.setOnClickListener(switchListener);
            button4.setOnClickListener(switchListener);
            mRootView.findViewById(ResFinder.getId("uemng_switch_button_container")).setVisibility(View.GONE);
        }else if (topicBaseFragments.size()==2){
            button1 = (TextView)mRootView.findViewById(ResFinder.getId("umeng_switch_button_one"));
            button3 = (TextView)mRootView.findViewById(ResFinder.getId("umeng_switch_button_three"));
            button4 = (TextView)mRootView.findViewById(ResFinder.getId("umeng_switch_button_four"));
            if (parseJson.topic.size() == 2){

                button1.setText(parseJson.topic.get(0));
                button4.setText(parseJson.topic.get(1));
            }
            button3.setVisibility(View.GONE);
            button1.setOnClickListener(switchListener);
            button3.setOnClickListener(switchListener);
            button4.setOnClickListener(switchListener);
            button1.setSelected(true);
        }else if (topicBaseFragments.size()==3){
            button1 = (TextView)mRootView.findViewById(ResFinder.getId("umeng_switch_button_one"));
            button3 = (TextView)mRootView.findViewById(ResFinder.getId("umeng_switch_button_three"));
            button4 = (TextView)mRootView.findViewById(ResFinder.getId("umeng_switch_button_four"));
            if (parseJson.topic.size() == 3){

                button1.setText(parseJson.topic.get(0));
                button3.setText(parseJson.topic.get(1));
                button4.setText(parseJson.topic.get(2));
            }
            button1.setOnClickListener(switchListener);
            button3.setOnClickListener(switchListener);
            button4.setOnClickListener(switchListener);
            if (parseJson.ttc.size()==0){
                button4.setSelected(true);
            }else {
                button1.setSelected(true);
            }
        }else if (topicBaseFragments.size()==0){
            button1 = (TextView)mRootView.findViewById(ResFinder.getId("umeng_switch_button_one"));
            button3 = (TextView)mRootView.findViewById(ResFinder.getId("umeng_switch_button_three"));
            button4 = (TextView)mRootView.findViewById(ResFinder.getId("umeng_switch_button_four"));

            button1.setOnClickListener(switchListener);
            button3.setOnClickListener(switchListener);
            button4.setOnClickListener(switchListener);
            button1.setSelected(true);
        }


    }
    protected void initswitchListener(){

        switchListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view.getId() == ResFinder.getId("umeng_switch_button_one")){
                    button1.setSelected(true);
                    button3.setSelected(false);
                    button4.setSelected(false);
                    ChangeFragment(0);
                }else if(view.getId() == ResFinder.getId("umeng_switch_button_three")){
                    button1.setSelected(false);

                    button3.setSelected(true);
                    button4.setSelected(false);
                    ChangeFragment(1);
                }else if(view.getId() == ResFinder.getId("umeng_switch_button_four")){
                    button1.setSelected(false);
                    button3.setSelected(false);
                    button4.setSelected(true);
                    ChangeFragment(topicBaseFragments.size()-1);
                }
            }
        };
    }
    public abstract void ChangeFragment(int num);
    public abstract void initFragment();

}
