package com.lg.base.utils;

import android.content.Context;
import android.widget.Toast;

import com.lg.base.core.RoboAsyncTask;

public class ToastUtil {

	public static void show(Context context, String msg) {
		show(context, msg, Toast.LENGTH_SHORT);
	}

	public static void show(Context context, int resourceId, int mode) {
		String msg = context.getResources().getString(resourceId);
		show(context, msg, mode);
	}

	public static void show(Context context, int resourceId) {
		show(context, resourceId, Toast.LENGTH_SHORT);
	}

	public static void show(Context context, String msg, int mode) {
        showToast(context, msg, mode);
	}

    private static Toast mToast=null;

    private static Runnable mRunnable = new Runnable() {
        public void run() {
            mToast.cancel();
        }
    };

    private static void showToast(Context mContext, String text,int mode) {
        RoboAsyncTask.getTaskHandler().removeCallbacks(mRunnable);
        if (mToast != null) {
            mToast.setText(text);
        } else {
            mToast = Toast.makeText(mContext, text, Toast.LENGTH_SHORT);
        }
        int duration = 2000;
        if (mode == Toast.LENGTH_LONG ) {
            duration = 3500;
        }
        RoboAsyncTask.getTaskHandler().postDelayed(mRunnable, duration);
        mToast.show();
    }
}
