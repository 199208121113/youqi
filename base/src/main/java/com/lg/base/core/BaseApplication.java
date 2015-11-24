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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public abstract class BaseApplication extends Application implements UncaughtExceptionHandler {

	protected static final String TAG = BaseApplication.class.getSimpleName();
	private static volatile ConcurrentHashMap<String, MessageHandListener> ttListenerMap = new ConcurrentHashMap<>();
	/** UI线程ID */
	private static volatile long uiTid = -1;


	private static BaseApplication appContext;

	public static BaseApplication getAppInstance() {
		return appContext;
	}

	public String getAccountType(){
		return this.getPackageName();
	}

	private static final Handler TASK_HANDLER = new Handler();

	public static Handler getTaskHandler() {
		return TASK_HANDLER;
	}

	private volatile Handler handler = null;
	@Override
	public void onCreate() {
		super.onCreate();
		appContext = this;
		if(handler == null){
			handler = new Handler(new Handler.Callback() {
				@Override
				public boolean handleMessage(Message msg) {
					if (uiTid <= 0) {
						uiTid = Thread.currentThread().getId();
						Thread.currentThread().setName("T1-UI");
					}
					if (msg == null || msg.obj == null)
						return false;
					if(!(msg.obj instanceof BaseMessage))
						return false;
					BaseMessage ttMsg = (BaseMessage) msg.obj;
					String from = ttMsg.getFrom().getUri();
					MessageHandListener ttMsgListener = ttListenerMap.get(from);
					if (ttMsgListener != null)
						ttMsgListener.executeMessage(ttMsg.getMsg());
					return true;
				}
			});
		}
		defaultMessageSender = new DefaultMessageSenderListenerImpl(this);
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

	public void sendMessage(BaseMessage tmsg) {
		Message msg = handler.obtainMessage();
		msg.obj = tmsg;
		handler.sendMessage(msg);
	}

    public void sendMessageDelayed(BaseMessage tmsg, long delayMillis) {
		Message msg = handler.obtainMessage();
		msg.obj = tmsg;
		handler.sendMessageDelayed(msg, delayMillis);
	}
    public void removeMessage(int what){
		handler.removeMessages(what);
	}

    public void sendEvent(BaseEvent evt) {
		if (evt == null) {
			LogUtil.e(TAG,"evt is null");
			return;
		}
		if (evt.getFrom() == null) {
			LogUtil.e(TAG,"evt.from is null");
			return;
		}
		if (evt.getTo() == null) {
			LogUtil.e(TAG,"evt.to is null");
			return;
		}
		String to = evt.getTo().getUri().trim();
		executors.execute(new EventWorker(evt));
		LogUtil.d(TAG, getThreadPoolInfo());
		if (!(ttListenerMap.containsKey(to) || Location.any.getUri().equals(to))) {
			LogUtil.e(TAG, "to:" + to + " can't register");
		}
	}
	private String getThreadPoolInfo(){
		int corePoolSize = executors.getCorePoolSize();
		int maxPoolSize = executors.getMaximumPoolSize();
		int activeCountForThread = executors.getActiveCount();
		int queueCount = executors.getQueue().size();
		StringBuilder sb = new StringBuilder();
		sb.append("核心线程数:" + corePoolSize + ",最大线程数:" + maxPoolSize + ",当前活动线程:" + activeCountForThread + "\n,缓存队列数量:" + queueCount + "");
		sb.append("曾经同时位于池中的最大线程数:" + executors.getLargestPoolSize());
		return sb.toString();
	}

    public void registerTtListener(MessageHandListener tl) {
		ttListenerMap.put(tl.getClass().getName(), tl);
	}

    public void unRegisterTtListener(MessageHandListener tl) {
		ttListenerMap.remove(tl.getClass().getName());
	}

	public static boolean containsMessageHandListener(String fullClassName){
		return ttListenerMap.containsKey(fullClassName);
	}

	private class EventWorker implements Runnable {
		private BaseEvent evt;

		public EventWorker(BaseEvent evt) {
			super();
			this.evt = evt;
		}

		@Override
		public void run() {
			String to = evt.getTo().getUri();
			if (to == null || to.trim().length() == 0)
				return;
			if (Location.any.getUri().equals(to)) {
				for (MessageHandListener lt : ttListenerMap.values()) {
					lt.executeEvent(evt);
				}
				return;
			}
			MessageHandListener lt = ttListenerMap.get(to);
			if (lt == null)
				return;
			lt.executeEvent(evt);
		}
	}

	void checkRunOnUI() {
		if (Thread.currentThread().getId() != uiTid)
			throw new IllegalStateException("not run on UI Thread");
	}

	void checkRunOnMain() {
		if (!Thread.currentThread().getName().startsWith(T2_THREAD_NAME))
			throw new IllegalStateException("not run on Main Thread");
	}

	public void postRunOnUi(UITask task) {
		handler.post(task);
	}

	// -------------------------------------------------------
	NetWorkReceiver mNetWorkReceiver = null;

	private void registReceivers() {
		mNetWorkReceiver = new NetWorkReceiver();
		IntentFilter mNetWrokFilter = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
		this.registerReceiver(mNetWorkReceiver, mNetWrokFilter);
	}

	// ====================================================
	private static final String T2_THREAD_NAME="T2-MainThread#";
	/** 持久化线程工厂 */
	private static final ThreadFactory threadFactory = new ThreadFactory() {
		private final AtomicLong mCount = new AtomicLong(1);

		@Override
		public Thread newThread(Runnable r) {
			return new Thread(r, T2_THREAD_NAME + mCount.getAndIncrement());
		}
	};
	private static final int CORE_POOL_SIZE = 5;
	private static final int MAXIMUM_POOL_SIZE = 128;
	private static final int KEEP_ALIVE = 10;
	private final ThreadPoolExecutor executors = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(20), threadFactory,new ThreadPoolExecutor.DiscardOldestPolicy());

	// ==========================全局异常处理===========================

	private UncaughtExceptionHandler defaultHandler = null;

	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		LogUtil.e(TAG, "App crash:", ex);
		if (handleException(ex)) {
			LooperThread lt = new LooperThread(this);
			lt.start();
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
	protected static void exitAndReStart(Context ctx) {
		LogUtil.e(TAG, "exitAndReStart()");
		killBackgroundProcesses(ctx);
		killSelfProcess();
		//System.exit(0);
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
			Map<String,String> infos = collectDeviceInfo(getApplicationContext());
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
	
    private static class LooperThread extends Thread {  
        public Handler mHandler;
        private int sendCount = 0;
        private Context mContext;
		public LooperThread(Context context) {
			super();
			this.mContext = context;
		}

		public void run() {  
            Looper.prepare();  
            mHandler = new Handler() {
                public void handleMessage(Message msg) {
                	if(msg.what ==1){
                		mHandler.removeMessages(msg.what);
                		//ToastUtil.show(mContext, "抱歉！程序出错,即将退出");
                		this.sendEmptyMessageDelayed(2, 2000);
                	}else if(msg.what == 2){
                		exitAndReStart(mContext);
                	}
                }
            };
            if(sendCount == 0){
            	mHandler.sendEmptyMessage(1);
            	sendCount++;
            }
            Looper.loop();  
        }
    }
    
    /** 是否记录错误日志 */
    protected boolean isRecordErrLog(){
    	return true;
    }

	private static DefaultMessageSenderListenerImpl defaultMessageSender = null;
	public static DefaultMessageSenderListenerImpl getDefaultMessageSender(){
		return defaultMessageSender;
	}

	@Override
	protected void attachBaseContext(Context base) {
		super.attachBaseContext(base);
		MultiDex.install(this);
	}
}
