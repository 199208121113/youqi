package com.lg.base.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.lg.base.bus.LogUtil;

import java.io.File;

public class APKUtil {
	private static final String tag = APKUtil.class.getSimpleName();
	public static final String MIME_TYPE_APK = "application/vnd.android.package-archive";
	/** 文件名 */
	public static void installApk(File f, Context context) {
		if(!f.exists()){
			LogUtil.e(tag, "Download the file does not exist");
			return ;
		}
		Intent intent = getInstallIntent(f);
		context.startActivity(intent);
	}
	
	public static Intent getInstallIntent(File f) {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.fromFile(f), MIME_TYPE_APK);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
		return intent;
	}

}
