package com.lg.base.core;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.multidex.MultiDex;
import android.util.Log;

import com.lg.base.bus.EventBus;
import com.lg.base.init.FilePathManager;
import com.lg.base.init.SharedPreferenceManager;
import com.lg.base.receiver.NetWorkReceiver;
import com.lg.base.utils.DateUtil;
import com.lg.base.utils.ExceptionUtil;
import com.lg.base.utils.IOUtil;

import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public abstract class BaseApplication extends Application implements UncaughtExceptionHandler {

	protected static final String TAG = BaseApplication.class.getSimpleName();

	/** UI线程ID */
	private static volatile long uiTid = -1;


	private static volatile BaseApplication appContext;
	private static void setAppInstance(BaseApplication instance){
		if(instance == null){
			return;
		}
		if(appContext == null){
			synchronized (BaseApplication.class){
				BaseApplication tmp = appContext;
				if(tmp == null){
					tmp = instance;
				}
				uiTid = Thread.currentThread().getId();
				appContext = tmp;
			}
		}
	}

	public static BaseApplication getAppInstance() {
		return appContext;
	}

	public String getAccountType(){
		return this.getPackageName();
	}

	@Override
	public void onCreate() {
		super.onCreate();
		BaseApplication.setAppInstance(this);
		Thread.currentThread().setName("T1-UI");
		defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler(this);
		init();

	}

	private void init() {
		registReceivers();
		FilePathManager.init(this);
		SharedPreferenceManager.init(this);
	}

	@Override
	public void onTerminate() {
		super.onTerminate();
		this.unregisterReceiver(mNetWorkReceiver);
	}



	void checkRunOnUI() {
		if (Thread.currentThread().getId() != uiTid)
			throw new IllegalStateException("not run on UI Thread");
	}

	void checkRunOnMain() {
		if (!Thread.currentThread().getName().startsWith(EventBus.T2_THREAD_NAME))
			throw new IllegalStateException("not run on Main Thread");
	}

	// -------------------------------------------------------
	NetWorkReceiver mNetWorkReceiver = null;

	private void registReceivers() {
		mNetWorkReceiver = new NetWorkReceiver();
		IntentFilter mNetWrokFilter = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
		this.registerReceiver(mNetWorkReceiver, mNetWrokFilter);
	}

	// ==========================全局异常处理===========================

	private UncaughtExceptionHandler defaultHandler = null;

	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		LogUtil.e(TAG, "App crash:", ex);
		if (handleException(ex)) {
			ErrorHandler eh = new ErrorHandler(Looper.getMainLooper());
			eh.sendEmptyMessage(2);
		} else if (defaultHandler != null) {
			defaultHandler.uncaughtException(thread, ex);
		} else {
			LogUtil.e(TAG, "unhandled exception", ex);
			exitAndReStart(this);
		}
	}

	/** 重启当前应用 */
	@SuppressWarnings("unused")
	protected final void restartApplication() {
		final Intent intent = getPackageManager().getLaunchIntentForPackage(getPackageName());
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
	}
	
	/** 退出并重启 */
	public static void exitAndReStart(Context ctx) {
		LogUtil.e(TAG, "exitAndReStart()");
		killBackgroundProcesses(ctx);
		killSelfProcess();
	}
	
	/** 杀掉当前进程 */
	public static void killSelfProcess(){
		try {
			LogUtil.e(TAG, "killSelfProcess begin");
			android.os.Process.killProcess(android.os.Process.myPid());
			LogUtil.e(TAG, "killSelfProcess end");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/** 杀掉后台进程 */
	public static void killBackgroundProcesses(Context ctx){
		try {
			ActivityManager manager = (ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);   
			if(manager != null){
				final String pkgName = ctx.getPackageName();
				LogUtil.e(TAG, "killBackgroundProcesses("+pkgName+") begin");
				manager.killBackgroundProcesses(pkgName);
				LogUtil.e(TAG, "killBackgroundProcesses("+pkgName+") end");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}

	/** 获得设备信息并写入到文件 */
	private boolean handleException(Throwable ex) {
		if(isRecordErrLog()){
			Map<String,String> infos = collectDeviceInfo(getAppInstance());
			saveCrashInfoToFile(ex,infos);
		}
		return true;
	}

	// 用来存储设备信息和异常信息

	/** 收集设备信息  */
	public static Map<String,String> collectDeviceInfo(Context ctx) {
		Map<String, String> infos = new HashMap<>();
		try {
			PackageManager pm = ctx.getPackageManager();
			PackageInfo pi = pm.getPackageInfo(ctx.getPackageName(), PackageManager.GET_ACTIVITIES);
			if (pi != null) {
				String versionName = pi.versionName == null ? "null" : pi.versionName;
				String versionCode = pi.versionCode + "";
				infos.put("versionName", versionName);
				infos.put("versionCode", versionCode);
			}
		} catch (Exception e) {
			LogUtil.e(TAG, "an error occured when collect package info", e);
		}

		Field[] fields = Build.class.getDeclaredFields();
		for (Field field : fields) {
			try {
				field.setAccessible(true);
				infos.put(field.getName(), field.get(null).toString());
				LogUtil.d(TAG, field.getName() + " : " + field.get(null));
			} catch (Exception e) {
				LogUtil.e(TAG, "an error occured when collect crash info", e);
			}
		}
		return infos;
	}

	/** 保存错误信息到文件中 */
	private String saveCrashInfoToFile(Throwable ex,Map<String,String> infos) {
		StringBuffer sb = new StringBuffer();
		for (Map.Entry<String, String> entry : infos.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue();
			sb.append(key + "=" + value + "\n");
		}
		String result = ExceptionUtil.getStackTrace(ex);
		sb.append(result);
		try {
			String dir = IOUtil.getExternalStoragePath()+"aaa_dir/";
			IOUtil.mkDir(dir);
			String time = DateUtil.formatDate(System.currentTimeMillis(), "yyyyMMddHHmmss");
			String fileName = time + ".log";
			String fileFullPath =  dir+ fileName;
			IOUtil.saveFileForText(fileFullPath, sb.toString());
			return fileFullPath;
		} catch (Exception e) {
			Log.e(TAG, "an error occured while writing file...", e);
		}
		return null;
	}

	private static class ErrorHandler extends Handler{
		public ErrorHandler(Looper looper) {
			super(looper);
		}
		public void handleMessage(Message msg) {
			if(msg.what == 2){
				exitAndReStart(getAppInstance());
			}
		}
	}
    
    /** 是否记录错误日志 */
    protected boolean isRecordErrLog(){
    	return true;
    }

	@Override
	protected void attachBaseContext(Context base) {
		super.attachBaseContext(base);
		MultiDex.install(this);
	}
}
