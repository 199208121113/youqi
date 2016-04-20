package com.lg.base.core;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lg.base.R;
import com.lg.base.bus.BaseEvent;
import com.lg.base.bus.EventBus;
import com.lg.base.bus.EventHandListener;
import com.lg.base.bus.EventLocation;
import com.lg.base.bus.LogUtil;
import com.lg.base.bus.UITask;
import com.lg.base.event.NetWorkEvent;
import com.lg.base.ui.dialog.LightNetWorkSetDialog;
import com.lg.base.ui.dialog.LightProgressDialog;

import java.util.List;

/**
 * description:RoboFragmentActivity
 * 继承RoboSherlockFragmentActivity之后，不但可以使用roboguice还可以使用ActionbarSherlock
 * @author liguo
 *
 */
public abstract class BaseActivity extends FragmentActivity implements EventHandListener, OnActionBarItemClickListener {

	protected String TAG = this.getClass().getSimpleName();
	private volatile BaseApplication app = null;
//	private final EventLocation from = new EventLocation(this.getClass().getName());
//	public final String DATA_SERIALIZABLE = "DATA_SERIALIZABLE";

	private volatile boolean running = false;
	private volatile boolean isSelfDestroyed =false;


	private static volatile ActivityManager AM_INSTANCE;
	public static ActivityManager getActivityManager(Context ctx){
		if(AM_INSTANCE == null){
			synchronized (BaseActivity.class){
				ActivityManager am = AM_INSTANCE;
				if(am == null) {
					am = (ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);
				}
				AM_INSTANCE = am;
			}
		}
		return AM_INSTANCE;

	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		app = (BaseApplication) getApplication();
		this.running = true;
        isSelfDestroyed = false;
		initGlobalView();
		if(validFullScreen()){
			enableFullScreen();
		}
		setContentView(mGlobalView);

		InjectManager.init(this);
		EventBus.get().register(this);

		initGoBack();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		running = true;
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		running = false;
	}
	
	protected final boolean isRunning(){
		return this.running;
	}

    public boolean isSelfDestroyed() {
        return isSelfDestroyed;
    }

	protected final ViewGroup getGlobalView(){
		return this.mGlobalView;
	}
	
	/** 初始返回事件，并提供onGoBack()方法供子类实现具体的逻辑 */
	private void initGoBack() {
        View goBackView = getGoBackView();
		if (goBackView != null) {
            goBackView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onGoBack();
                }
            });
        }
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
        isSelfDestroyed = true;
		EventBus.get().unRegister(this);
	}

	/**检测是否在UI线程中运行*/
	protected final void checkRunOnUI() {
		app.checkRunOnUI();
	}

	/**检测是否在二线程或主线程中运行*/
	protected final void checkRunOnMain() {
		app.checkRunOnMain();
	}

	protected final EventLocation getLocation() {
		return new EventLocation(this.getClass().getName());
	}

	protected void sendExitEvent(){
		BaseEvent exitEvent = new BaseEvent(EventLocation.any,exit_what);
		EventBus.get().sendEvent(exitEvent);
	}
	public static final int exit_what = -1988;
	/** 处理Event，是在二线程中 */
	@Override
	public void executeEvent(BaseEvent evt) {
		if (evt instanceof NetWorkEvent) {
			EventBus.get().postRunOnUiThread(new UITask() {
				@Override
				public void run() {
					NetWorkEvent event = (NetWorkEvent) getData();
					onNetworkStateChanged(event);
				}
			}.setData(evt));

		}else if(evt != null && evt.getWhat() == exit_what){
			EventBus.get().postRunOnUiThread(new UITask(this) {
				@Override
				public void run() {
					finish();
				}
			});
		}
	}

	/** 处理Message，是在UI线程中 */
	@Override
	public void executeMessage(Message msg) {

	}

	/**当网络状态发生改变时的处理*/
	protected void onNetworkStateChanged(NetWorkEvent evt) {
		if (evt.isAvailable()) {
			LogUtil.d(TAG, "当前网络可用,类型:" + evt.getNetWorkType().name());
		} else {
			List<RunningTaskInfo> runningTaskInfos = getActivityManager(this).getRunningTasks(1) ;
		    if(runningTaskInfos == null || runningTaskInfos.size() == 0)
		    	return ;
		    //必须判断当前Activity是否在栈顶，如果是才弹出网络提示框
		    String topActivityName = (runningTaskInfos.get(0).topActivity).getClassName();		
		    if(this.getClass().getName().equals(topActivityName)){
		    	//showNetWorkDialog("网络已断开","当前网络不可用,是否现在就设置?");
		    }
		}
	}

	/** 代替findViewById */
	@SuppressWarnings("unchecked")
	protected <V extends View> V find(int id) {
		return (V) this.findViewById(id);
	}

	public BaseApplication getBaseApplication() {
		return this.app;
	}

	/**本Activity对应的xml文件*/
	protected abstract int getContentView();

	// ==============================================
	/** 返回的ResID */
	protected View getGoBackView() {
        ActionBarMenu bar = getActionBarMenu();
        if(bar != null){
            return bar.getLeftLayout();
        }
        return null;
	}

	/** 当点击返回按钮时调用 */
	protected void onGoBack() {
		this.finish();
	}
	
	/** 是否启用合屏 */
	protected boolean validFullScreen(){
		return false;
	}
	
	/** 启用全屏 */
	private void enableFullScreen(){
		//设置无标题  
        requestWindowFeature(Window.FEATURE_NO_TITLE);  
        //设置全屏  
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);  
	}

	// ====================actionbar->begin=========================

	/** Actionbar创建的时候,如果需要则返回Actionbar */
	protected ActionBarMenu onActionBarCreate() {
		return null;
	}

	protected ViewGroup mGlobalView = null;
	ActionBarMenu mActionBar = null;
	private int actionBarHeight = 0;
	View actionBarView = null;

	/** 初始化合局View
	 *  主要是判断是否有自定义的ActionBar，如果有则创建一个LinearLayout，然后分别加Actionbar，及contentView
	 *  
	 *  */
	private void initGlobalView() {
 		ViewGroup viewGroup = (ViewGroup) LayoutInflater.from(getActivity()).inflate(getContentView(), null);
 		mActionBar = onActionBarCreate();
 		mGlobalView = getRelativeLayout();
 		if (mActionBar != null) {
 			LinearLayout myLinearLayout = getLinearLayout();
 			actionBarView = inflateActionBarView();
 			actionBarHeight = actionBarView.getHeight();
 			mActionBar.setViewAndListener(actionBarView, this);
 			myLinearLayout.addView(actionBarView);
 			
 			LayoutParams lp1 = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
 			myLinearLayout.addView(viewGroup,lp1);
 			
 			LayoutParams lp2 = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
 			mGlobalView.addView(myLinearLayout,lp2);
 		} else {
 			LayoutParams lp2 = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
 			mGlobalView.addView(viewGroup,lp2);
 		}
 	}

 	private LinearLayout getLinearLayout() {
 		LinearLayout ll = new LinearLayout(getActivity());
 		ll.setOrientation(LinearLayout.VERTICAL);
 		int width = LinearLayout.LayoutParams.MATCH_PARENT;
 		int height = LinearLayout.LayoutParams.MATCH_PARENT;
 		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(width, height);
 		ll.setLayoutParams(lp);
 		return ll;
 	}
 	
 	private RelativeLayout getRelativeLayout() {
 		RelativeLayout ll = new RelativeLayout(getActivity());
 		int width = RelativeLayout.LayoutParams.MATCH_PARENT;
 		int height = RelativeLayout.LayoutParams.MATCH_PARENT;
 		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(width, height);
 		ll.setLayoutParams(lp);
 		return ll;
 	}

 	protected View inflateActionBarView() {

 		return LayoutInflater.from(getActivity()).inflate(R.layout.layout_actionbar, null);
 	}
 	
 	private final Activity getActivity(){
 		return this;
 	}

	/** ActionBar中的View的点击事件 */
	@Override
	public void onActionBarClick(View v) {

	}
	
	/** 获得Actionbar的高度 */
	protected final int getActionBarHeight() {
		return this.actionBarHeight;
	}
	
	/**返回 ActionBar实例*/
	protected final ActionBarMenu getActionBarMenu(){
		return this.mActionBar;
	}	
	
	//=================进度条==============
 	private View getProgressBarView(){
		View view = LayoutInflater.from(this).inflate(R.layout.layout_progress_bar, null);
		return view;
	}
 	
	AlertDialog progressDialog = null;
	/** 弹出对证框 */
	@SuppressLint("NewApi")
	protected AlertDialog showProgressDialog(String message){
		if(progressDialog != null){
			closeProgressDialog();
		}	
		if(this.isRunning()==false){
			return null;
		}
		progressDialog = LightProgressDialog.create(this, message);
		progressDialog.show();
		View view = getProgressBarView();
		TextView tv = (TextView)view.findViewById(R.id.layout_progress_bar_tv_loading);
		tv.setText(message);
		progressDialog.setContentView(view, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		return progressDialog;
	}

    @SuppressLint("NewApi")
    protected AlertDialog showProgressDialog(String message,DialogInterface.OnDismissListener listener){
        if(progressDialog != null){
            closeProgressDialog();
        }
        if(this.isRunning()==false){
            return null;
        }
        progressDialog = LightProgressDialog.create(this, message);
        if(listener != null) {
            progressDialog.setOnDismissListener(listener);
        }
        progressDialog.show();
        View view = getProgressBarView();
        TextView tv = (TextView)view.findViewById(R.id.layout_progress_bar_tv_loading);
        tv.setText(message);
        progressDialog.setContentView(view, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        return progressDialog;
    }
	
	/**关闭对话框*/
	protected void closeProgressDialog(){
		if(progressDialog == null)
			return ;
		progressDialog.dismiss();
		progressDialog = null;
	}
	
	//==================网络设置dialog=================
	AlertDialog networkDialog = null;
	/**显示网络对话框架*/
	protected final AlertDialog showNetWorkDialog(String title,String message){
		if(networkDialog != null)
			closeNetWorkDialog();
		networkDialog = LightNetWorkSetDialog.create(this, title, message);
		networkDialog.show();
		return networkDialog;
	}
	/** 关闭网络对话框 */
	protected final void closeNetWorkDialog(){
		if(networkDialog == null)
			return ;
		networkDialog.dismiss();
		networkDialog  = null;
	}
	//=====================未处理事件队列======================
	//protected volatile LinkedBlockingQueue<BaseEvent> events = new LinkedBlockingQueue<BaseEvent>();
	
	public static void exit() {
		try {
			android.os.Process.killProcess(android.os.Process.myPid());
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.exit(0);
	}

	/**
	 * 不需要动画，可传入0作为资源
	 */
	public void startActivityForAnimation(Intent intent, final int targetInResID, final int currentOutResID) {
		startActivity(intent);
		overridePendingTransition(targetInResID, currentOutResID);
	}

	public void finishActivityForAnimation(final int currentOutResID) {
		super.finish();
		overridePendingTransition(0, currentOutResID);
	}
}
