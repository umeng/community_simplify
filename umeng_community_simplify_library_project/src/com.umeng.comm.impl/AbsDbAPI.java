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

package com.umeng.comm.impl;

import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.text.TextUtils;

import com.umeng.comm.core.beans.CommConfig;
import com.umeng.comm.core.beans.CommUser;
import com.umeng.comm.core.constants.DbConstants;
import com.umeng.comm.core.db.engine.DatabaseExecutor;
import com.umeng.comm.core.listeners.Listeners.SimpleFetchListener;
import com.umeng.comm.core.nets.uitls.NetworkUtils;
import com.umeng.comm.core.utils.DeviceUtils;
import com.umeng.comm.core.utils.Log;

import java.io.File;
import java.util.Collection;

import activeandroid.ActiveAndroid;
import activeandroid.Cache;
import activeandroid.TableInfo;
import activeandroid.util.SQLiteUtils;


/**
 * 数据库操作抽象类,封装数据库操作、回调流程
 *
 * @param <T> 操作的数据类型,一般为List的数据集合
 */
public abstract class AbsDbAPI<T> {
    private DatabaseExecutor mDbDispatcher = DatabaseExecutor.getExecutor();
    private Handler mHandler = mDbDispatcher.getUIHandler();

    private static boolean mIsChecked;

    /**
     * 提交数据库命令
     *
     * @param cmd
     */
    public void submit(DbCommand cmd) {
        mDbDispatcher.submit(cmd);
    }

    protected void deliverResult(final SimpleFetchListener<T> listener, final T t) {
        mHandler.post(new Runnable() {

            @Override
            public void run() {
                listener.onComplete(t);
            }
        });
    }

    protected void deliverResultForCount(final SimpleFetchListener<Integer> listener,
                                         final Integer count) {
        mHandler.post(new Runnable() {

            @Override
            public void run() {
                listener.onComplete(count);
            }
        });
    }

    /**
     * 数据库命令类,封装事务处理,提升效率
     */
    public static abstract class DbCommand implements Runnable {
        @Override
        public final void run() {
            try {
                ActiveAndroid.beginTransaction();
                try {
                    execute();
                    ActiveAndroid.setTransactionSuccessful();
                } catch (Exception exception) {
                    Log.e("activeDB", "error, an error occurred during....");
                    exception.printStackTrace();
                } finally {
                    ActiveAndroid.endTransaction();
                }
            }catch (Exception e){
                Log.e("activeDB", "error, execute db command end... ");
                e.printStackTrace();
            }
        }

        protected abstract void execute();
    }

    /**
     * 清理数据库缓存
     */
    protected void checkAndClearCache() {
        if(mIsChecked){
            return;
        }
        mIsChecked = true;

        if (DeviceUtils.getContext() == null ||
                !NetworkUtils.isConnectedToNetwork(DeviceUtils.getContext())) {
            return;
        }

        SQLiteDatabase db = ActiveAndroid.getDatabase();
        if (db == null) {
            return;
        }

        String dbPath = db.getPath();
        File dbFile = new File(dbPath);
        if (dbFile == null || !dbFile.exists()) {
            return;
        }

        final double size = dbFile.length() / 1024.0 / 1024;
        if (size < DbConstants.DB_SIZE_M) {
            return;
        }

        submit(new DbCommand() {
            @Override
            protected void execute() {
                long time = System.currentTimeMillis();
                Log.d("activeDB", "clear cache start... db size:" + size);
                Collection<TableInfo> tableInfos = Cache.getTableInfos();
                CommUser currentUser = CommConfig.getConfig().loginedUser;
                String uId = null;
                if (currentUser != null && !TextUtils.isEmpty(currentUser.id)) {
                    uId = currentUser.id;
                }
                for (TableInfo tableInfo : tableInfos) {
                    if (!TextUtils.isEmpty(uId) && tableInfo.getTableName().equals("user")) {
                        SQLiteUtils.execSql("DELETE FROM " + tableInfo.getTableName() + " WHERE _id != '" + uId + "'");
                    } else {
                        SQLiteUtils.execSql("DELETE FROM " + tableInfo.getTableName());
                    }
                }
                Log.d("activeDB", "clear cache finish... consum time:" + (System.currentTimeMillis() - time));
            }
        });
    }
}
