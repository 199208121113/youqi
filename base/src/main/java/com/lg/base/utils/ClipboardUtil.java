package com.lg.base.utils;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.ClipData;
import android.content.Context;
import android.os.Build;

@SuppressLint("NewApi")
public class ClipboardUtil {
	public static void copy(String content, Context context) {
		// 得到剪贴板管理器
		android.content.ClipboardManager cmb = (android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
		cmb.setPrimaryClip(ClipData.newPlainText(null, content.trim()));
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public static String paste(Context context) {
		String str = null;
		android.content.ClipboardManager cmb = (android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
		//str = cmb.getText().toString().trim();
		ClipData clip = cmb.getPrimaryClip();
        if (clip != null && clip.getItemCount() > 0) {
            str = (String)clip.getItemAt(0).coerceToText(context);
        }
		return str;
	}
}
