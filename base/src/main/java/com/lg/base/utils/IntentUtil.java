package com.lg.base.utils;

import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.provider.Settings;

import java.io.File;

public class IntentUtil {

	/**
	 * 创建裁剪Intent Activity需要设置为：
	 * android:configChanges="orientation|keyboardHidden"
	 * */
	public static Intent createPhotoCropIntent(String sourceImageUri, String outPutUri, int width, int height) {
		Intent intent = new Intent("com.android.camera.action.CROP", null);
		intent.setType("image/*");
		intent.putExtra("crop", "true");
		int n = getA(width, height);
		int ax = width / n;
		int ay = height / n;
		intent.putExtra("aspectX", ax);
		intent.putExtra("aspectY", ay);
		intent.putExtra("outputX", width);
		intent.putExtra("outputY", height);
		intent.putExtra("scale", true);
		intent.putExtra("scaleUpIfNeeded", true);
		intent.putExtra("noFaceDetection", true);
		intent.putExtra("return-data", false);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(outPutUri)));
		intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
		intent.setDataAndType(Uri.fromFile(new File(sourceImageUri)), "image/jpeg");
		return intent;
	}

	/**
	 * 创建照相Intent 返回的Intent为null
	 * */
	public static Intent createPhotoTakeIntent(String outPutUri) {
		Intent it = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		File f = new File(outPutUri);
		it.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
		return it;
	}

	
	public static final String ACTION_OPEN_DOCUMENT = "android.intent.action.OPEN_DOCUMENT";
	/**
	 * 创建选择图片Intent 返回的Intent.getData()为URI
	 * */
	public static Intent createPhotoPickerIntent() {
//		Intent it = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//		it.setType("image/*");
//		return it;
		
		Intent it = new Intent(Intent.ACTION_GET_CONTENT);
		it.addCategory(Intent.CATEGORY_OPENABLE);  
		it.setType("image/*");
		if (Build.VERSION.SDK_INT <19) {  
		    it.setAction(Intent.ACTION_GET_CONTENT);  
		}else {  
		    it.setAction(ACTION_OPEN_DOCUMENT);  
		} 
		return it;
	}

	/**
	 * 创建网络设置的Intent
	 */
	public static Intent createNetworkSettingIntent() {
		Intent it = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
		if(Build.VERSION.SDK_INT <= 10){
            it = new Intent();
            ComponentName component = new ComponentName("com.android.settings","com.android.settings.WirelessSettings");
            it.setComponent(component);
            it.setAction("android.intent.action.VIEW");
        }
		return it;
	}

	/** 创建拔打电话的Intent */
	public static Intent createCallPhoneIntent(String phoneNumber) {
		Intent it = new Intent(Intent.ACTION_VIEW, Uri.parse("tel:" + phoneNumber));
		return it;
	}

	/**
	 * 创建浏览器Intent
	 */
	public static Intent createBrowserIntent(String uriString) {
		Intent it = new Intent(Intent.ACTION_VIEW, Uri.parse(uriString));
		return it;
	}

	/**
	 * 创建发送短信的Intent
	 */
	public static Intent createSendSmsIntent(String phoneNum,String text){
		Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:"+phoneNum));
		intent.putExtra("sms_body", text);
		return intent;
	}

	// 求最大公约数
	private static int getA(int m, int n) {
		int min = Math.min(m, n);
		int a = 1;
		for (int i = min; i > 0; i--) {
			if (m % i == 0 && n % i == 0) {
				a = i;
				break;
			}
		}
		return a;
	}
}
