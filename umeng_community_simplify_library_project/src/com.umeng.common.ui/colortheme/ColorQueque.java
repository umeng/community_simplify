package com.umeng.common.ui.colortheme;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.View;

import com.umeng.comm.core.constants.Constants;
import com.umeng.comm.core.utils.Log;
import com.umeng.comm.core.utils.ResFinder;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by wangfei on 16/6/23.
 */
public  class ColorQueque {

  private static Resources.Theme theme = ResFinder.getApplicationContext().getResources().newTheme();

    public static void init(){

    }
   public static  HashMap<Integer,View> viewHashMap = new HashMap<Integer,View>();
    public void add(int attr,View view){
        viewHashMap.put(new Integer(attr),view);
    }
//    public static int getColor(int attr){
//        Resources.Theme theme = ResFinder.getApplicationContext().getTheme();
//        TypedValue typedValue = new TypedValue();
//        theme.resolveAttribute(attr, typedValue, true);
//        return typedValue.data;
//    }
//    public static int getColor(Context context,int attr){
//
//        Resources.Theme theme =context.getTheme();
//
//        TypedValue typedValue = new TypedValue();
//        theme.resolveAttribute(attr, typedValue, true);
//
//        return typedValue.data;
//    }
    public static int getColor(String name){
        int attr = ResFinder.getAttr(name);
////        Resources.Theme theme =ResFinder.getApplicationContext().getTheme();
//        Resources.Theme  theme =ResFinder.getApplicationContext().getResources().newTheme();
//        theme.applyStyle(ResFinder.getStyle(Constants.theme),true);
//        TypedArray a = theme.obtainStyledAttributes(ResFinder.getStyle(Constants.theme),
//                new int[] { attr });
//        int attributeResourceId = a.getResourceId(0, 0);
//        return ResFinder.getApplicationContext().getResources().getColor(
//                attributeResourceId);

        int styleId = ResFinder.getStyle(Constants.theme);
        theme.applyStyle(styleId, true);
        TypedValue typedValue = new TypedValue();
        theme.resolveAttribute(attr, typedValue, true);
        return typedValue.data;
    }
//    public static Drawable getDrawable( int attr) {
//        Resources.Theme theme = ResFinder.getApplicationContext().getTheme();
//        TypedArray a = theme.obtainStyledAttributes(ResFinder.getStyle(Constants.theme),
//                new int[] { attr });
//        int attributeResourceId = a.getResourceId(0, 0);
//
//        return ResFinder.getApplicationContext().getResources().getDrawable(
//                attributeResourceId);
//
//    }
    public static Drawable getDrawable(String attrname) {
        int attr = ResFinder.getAttr(attrname);
//        Resources.Theme theme = ResFinder.getApplicationContext().getTheme();

        theme.applyStyle(ResFinder.getStyle(Constants.theme),true);
        TypedArray a = theme.obtainStyledAttributes(ResFinder.getStyle(Constants.theme),
                new int[] { attr });
        int attributeResourceId = a.getResourceId(0, 0);
        return ResFinder.getApplicationContext().getResources().getDrawable(
                attributeResourceId);

    }
}
