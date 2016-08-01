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

package com.umeng.common.ui.presenter.impl;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import com.umeng.comm.core.constants.Constants;
import com.umeng.comm.core.nets.uitls.Utils;
import com.umeng.comm.core.utils.BitmapUtils;
import com.umeng.comm.core.utils.Log;
import com.umeng.comm.core.utils.ToastMsg;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TakePhotoPresenter {

    /**
     * 启动拍照的requestCode
     */
    public static final int REQUEST_IMAGE_CAPTURE = 123;
    private Activity mContext;
    private String mNewImagePath;

    private ContentResolver mContentResolver;

    public void attach(Activity activity) {
        mContext = activity;
        mContentResolver = mContext.getContentResolver();
    }

    public void detach() {
        mContext = null;
    }

    /**
     * 启动系统拍照功能
     */
    public void takePhoto() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        ComponentName componentName = takePictureIntent.
                resolveActivity(mContext.getPackageManager());
        if (componentName == null) { // 无拍照的App
            return;
        }
        File photoFile = null;
        try {
            photoFile = createImageFile();
            Uri fileUri = Uri.fromFile(photoFile);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
            // 跳转到拍照页面
            mContext.startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        } catch (IOException ex) {
            // Error occurred while creating the File
            ex.printStackTrace();
            ToastMsg.showShortMsgByResName("umeng_comm_save_photo_failed");
        }
    }

    /**
     * Creates the image file to which the image must be saved.
     *
     * @return
     * @throws IOException
     */
    private File createImageFile() throws IOException {
        SimpleDateFormat dateFormat = (SimpleDateFormat) SimpleDateFormat.getInstance();
        dateFormat.applyPattern("yyyyMMdd_HHmmss");
        String timeStamp = dateFormat.format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        // 检测目录是否存在
        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);

        mNewImagePath = image.getAbsolutePath();
        return image;
    }

    /**
     * 保存新的照片,并且返回照片的Uri路径
     *
     * @return 照片的Uri
     */
    public String updateImageToMediaLibrary() {
        File imgFile = new File(mNewImagePath);

//        BitmapFactory.Options options = new BitmapFactory.Options();
//        options.inJustDecodeBounds = true;
//        BitmapFactory.decodeFile(mNewImagePath, options);
//        // 图片的有效性判断
//        if (options.outWidth < 10 && options.outHeight < 10) {
//            imgFile.delete();
//            return "";
//        }

        Uri contentUri = Uri.fromFile(imgFile);

        int degree = processExif(mNewImagePath);
        if(degree != 0){
            Bitmap bitmap = getBitmap(contentUri);
            if(bitmap != null){
                bitmap = rotateImage(bitmap, degree);
                saveOutput(contentUri, bitmap);
            }
        }

//        if (getThumbnail(contentUri)==null){
//            return "";
//        }
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaScanIntent.setData(contentUri);
        // 发布广播,更新媒体库
        mContext.sendBroadcast(mediaScanIntent);
        Log.e("xxxxxx", "contentUri.toString()=" + contentUri.toString());
        return contentUri.toString();
    }


    private Bitmap getBitmap(Uri uri) {
        InputStream in = null;
        try {
            in = mContentResolver.openInputStream(uri);

            //Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;

            BitmapFactory.decodeStream(in, null, o);
            in.close();

            int scale = BitmapUtils.computeInSmallSize(o, Constants.SCREEN_WIDTH, Constants.SCREEN_WIDTH);

            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            in = mContentResolver.openInputStream(uri);
            Bitmap b = BitmapFactory.decodeStream(in, null, o2);
            in.close();

            return b;
        } catch (FileNotFoundException e) {
            Log.e("xxxxxx", "file " + uri + " not found");
        } catch (IOException e) {
            Log.e("xxxxxx", "file " + uri + " not found");
        }
        return null;
    }

    private int processExif(String photoPath) {
        int degree = 0;
        try {
            if (photoPath == null) {
                return degree;
            }
            ExifInterface ei = new ExifInterface(photoPath);
            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
//                case ExifInterface.ORIENTATION_ROTATE_180:
//                    degree = 180;
//                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return degree;
    }

    // Thong added for rotate
    private Bitmap rotateImage(Bitmap src, float degree) {
        // create new matrix
        Matrix matrix = new Matrix();
        // setup rotation degree
        matrix.postRotate(degree);
        Bitmap bmp = Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), matrix, true);
        return bmp;
    }

    private void saveOutput(Uri uri, Bitmap croppedImage) {
        if (uri != null) {
            OutputStream outputStream = null;
            try {
                outputStream = mContentResolver.openOutputStream(uri);
                if (outputStream != null) {
                    croppedImage.compress(Bitmap.CompressFormat.JPEG, 90, outputStream);
                }
            } catch (IOException ex) {
                Log.e("xxxxxx", "Cannot open file: " + uri, ex);
                return;
            } finally {
                Utils.closeSilently(outputStream);
            }
        } else {
            Log.e("xxxxxx", "not defined image url");
        }
        croppedImage.recycle();
    }


}
