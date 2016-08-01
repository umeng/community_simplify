package com.umeng.common.ui.util;

import android.content.Context;
import android.os.Build;

/**
 * @author Tinker Pang <kangnixi@gmail.com>
 * @copyright ©2013-2103 girlcoding.com
 * @license http://www.girlcoding.com
 */
public final class SDKVersionUtils {

    // Build.VERSION_CODES.M
    public static boolean isMOrHigher(){
        return Build.VERSION.SDK_INT >= 23;
    }

    // Build.VERSION_CODES.KITKAT
    public static boolean isKitKatOrHigher() {
        return Build.VERSION.SDK_INT >= 19;
    }

    // Build.VERSION_CODES.JELLY_BEAN_MR1
    public static boolean isJellyBeanMR1OrHigher() {
        return Build.VERSION.SDK_INT >= 17;
    }

    // Build.VERSION_CODES.JELLY_BEAN
    public static boolean isJellyBeanOrHigher() {
        return Build.VERSION.SDK_INT >= 16;
    }

    // Build.VERSION_CODES.ICE_CREAM_SANDWICH
    public static boolean isICSOrHigher() {
        return Build.VERSION.SDK_INT >= 14;
    }

    // Build.VERSION_CODES.HONEYCOMB
    public static boolean isHoneycombOrHigher() {
        return Build.VERSION.SDK_INT >= 11;
    }

    // Build.VERSION_CODES.GINGERBREAD
    public static boolean isGingerbreadOrHigher() {
        return Build.VERSION.SDK_INT >= 9;
    }

    // Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO
    public static boolean isFroyoOrHigher() {
        return Build.VERSION.SDK_INT >= 8;
    }

//    public static boolean isGoogleTV(Context context) {
//        return context.getPackageManager().hasSystemFeature(
//                "com.google.android.tv");
//    }


}
