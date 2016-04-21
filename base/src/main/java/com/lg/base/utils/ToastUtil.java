package com.lg.base.utils;

import android.content.Context;
import android.widget.Toast;

import com.lg.base.core.BaseApplication;
import com.lg.base.core.SimpleAsyncTask;

public class ToastUtil {

	public static void show(String msg) {
		show(msg, Toast.LENGTH_SHORT);
	}

	public static void show(String msg, int mode) {
        showToast(BaseApplication.getAppInstance(), msg, mode);
	}

    private static Toast mToast=null;

    private static Runnable mRunnable = new Runnable() {
        public void run() {
            mToast.cancel();
        }
    };

    private static void showToast(Context mContext, String text,int mode) {
        SimpleAsyncTask.getTaskHandler().removeCallbacks(mRunnable);
        if (mToast != null) {
            mToast.setText(text);
        } else {
            mToast = Toast.makeText(mContext, text, Toast.LENGTH_SHORT);
        }
        int duration = 2000;
        if (mode == Toast.LENGTH_LONG ) {
            duration = 3500;
        }
        SimpleAsyncTask.getTaskHandler().postDelayed(mRunnable, duration);
        mToast.show();
    }
}
