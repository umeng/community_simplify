package com.umeng.common.ui.configure;

import android.content.Context;
import android.content.res.AssetManager;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created by wangfei on 16/6/26.
 */
public  class parseJson {
    public static ArrayList<String> title = new ArrayList<String>();
    public static ArrayList<String> tc = new ArrayList<String>();
    public static ArrayList<String> topic = new ArrayList<String>();
    public static ArrayList<String> ttc = new ArrayList<String>();
    public static void getJson(Context context, String fileName) {

        StringBuilder stringBuilder = new StringBuilder();
        try {
            AssetManager assetManager = context.getAssets();
            BufferedReader bf = new BufferedReader(new InputStreamReader(
                    assetManager.open(fileName)));
            String line;
            while ((line = bf.readLine()) != null) {
                stringBuilder.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        initData(stringBuilder.toString());
       //  stringBuilder.toString();
    }
    public static void initData(String result){
        if (TextUtils.isEmpty(result)){
            return;
        }
        JSONObject json = null;
        try {
            json = new JSONObject(result);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JSONArray titlejsonrray = json.optJSONArray("title");
        title.clear();
        for (int i = 0;i<titlejsonrray.length();i++){
            try {
                title.add(titlejsonrray.getString(i));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        JSONArray tcJsonarray = json.optJSONArray("titlecontent");
        tc.clear();
        for (int i = 0;i<tcJsonarray.length();i++){
            try {
                tc.add(tcJsonarray.getString(i));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        JSONArray topicJsonarray = json.optJSONArray("topictitle");
        topic.clear();
        for (int i = 0;i<topicJsonarray.length();i++){
            try {
                topic.add(topicJsonarray.getString(i));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        JSONArray ttcJsonarray = json.optJSONArray("topiccontent");
        ttc.clear();
        for (int i = 0;i<ttcJsonarray.length();i++){
            try {
                ttc.add(ttcJsonarray.getString(i));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

}
