package com.lg.base.utils;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Window;
import android.view.WindowManager;

import java.lang.reflect.Field;

public class ScreenUtil {

	/** 标题栏高度 */
	public static int getTitleBarHeight(Activity act) {
		Rect rect = new Rect();
		Window win = act.getWindow();
		win.getDecorView().getWindowVisibleDisplayFrame(rect);
		int statusBarHeight = rect.top;
		int contentViewTop = win.findViewById(Window.ID_ANDROID_CONTENT).getTop();
		int titleBarHeight = contentViewTop - statusBarHeight;
		return titleBarHeight;
	}

	/** 通知栏高度  ,以下代码不能在onCreate里面使用，否则获取状态栏高度将小于等于0 */
	public static int getStatusBarHeight(Activity act) {
		Rect rect = new Rect();
		Window win = act.getWindow();
		win.getDecorView().getWindowVisibleDisplayFrame(rect);
		int statusBarHeight = rect.top;
		if(statusBarHeight > 0){
			return statusBarHeight;
		}
		try {
			Class<?> c = Class.forName("com.android.internal.R$dimen");
			Object obj = c.newInstance();
			Field field = c.getField("status_bar_height");
			int x = Integer.parseInt(field.get(obj).toString());
			int y = act.getResources().getDimensionPixelSize(x);
			statusBarHeight = y;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return statusBarHeight;
		
	}

	/** 获取屏幕的高宽度 */
	public static Display getDisplay(Context context) {
		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		return wm.getDefaultDisplay();
	}

	/** dp转px */
	public static int dip2px(Context context, float dpValue) {
		if(dpValue == 0){
			return 0;
		}
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dpValue * scale + 0.5f);
	}

	/** px转dp */
	public static int px2dip(Context context, float pxValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (pxValue / scale + 0.5f);
	}

    /** 将px值转换为sp值，保证文字大小不变 */
    public static int px2sp(Context context, float pxValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (pxValue / fontScale + 0.5f);
    }

    /** 将sp值转换为px值，保证文字大小不变 */
    public static int sp2px(Context context, float spValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

	/*
	 * 
	 * getLocationOnScreen
	 * 计算该视图在全局坐标系中的x，y值，（注意这个值是要从屏幕顶端算起，也就是包括了通知栏的高度）//获取在当前屏幕内的绝对坐标
	 * getLocationInWindow 计算该视图在它所在的widnow的坐标x，y值，//获取在整个窗口内的绝对坐标
	 * 
	 * getLeft , getTop, getBottom, getRight, 这一组是获取相对在它父亲里的坐标 
	 * int[] location = new int[2] ;
	 * view.getLocationInWindow(location); //获取在当前窗口内的绝对坐标
	 * view.getLocationOnScreen(location);//获取在整个屏幕内的绝对坐标 location
	 * [0]--->x坐标,location [1]--->y坐标
	 */
	
	/** 判断此android设备是否为平板 */
	public static boolean isTablet(Context ctx){
		return isTabletBySystemConfig(ctx);
	}
	
	/** 根据屏幕尺寸来判断是否为平板 */
	public static boolean isPadByScreenSize(Context ctx) {
		boolean isPad = false;
		try {
			double screenInches = getScreenPhysicalSize(ctx);// 屏幕尺寸
			isPad =(screenInches >= 7.0);// 大于7尺寸则为Pad
		} catch (Exception e) {
			e.printStackTrace();
		}
		return isPad;
	}
	
	/** 获取屏幕的物理尺寸,一般大于7寸的则为平板 */
	public static double getScreenPhysicalSize(Context ctx) {
		WindowManager wm = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		DisplayMetrics dm = new DisplayMetrics();
		display.getMetrics(dm);
		double x = Math.pow(dm.widthPixels / dm.xdpi, 2);
		double y = Math.pow(dm.heightPixels / dm.ydpi, 2);
		double screenInches = Math.sqrt(x + y);// 屏幕尺寸
		return screenInches;
    }
	
	/** 根据是否有电话功能来判断是否为平板,、
	 *  true：是平板
	 * false： 不是平板*/
    public static boolean isPadByHasPhone(Context ctx) {
    	boolean isPad = false;
        try {
			TelephonyManager telephony = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
			isPad =( telephony.getPhoneType() == TelephonyManager.PHONE_TYPE_NONE);
		} catch (Exception e) {
			e.printStackTrace();
		}
        return isPad;
    }
    
	public static boolean isTabletBySystemConfig(Context ctx) {
		return (ctx.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
	}

}
