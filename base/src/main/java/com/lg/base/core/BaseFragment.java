package com.lg.base.core;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Application;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lg.base.R;
import com.lg.base.ui.dialog.LightNetWorkSetDialog;
import com.lg.base.ui.dialog.LightProgressDialog;



@SuppressWarnings("javadoc")
/**
 * description:RoboFragment,RoboSherlockFragment
 * 跟BaseActivity类似
 * @author liguo
 *
 */
public abstract class BaseFragment extends Fragment implements MessageHandListener,MessageSendListener,OnActionBarItemSelectedListener {
    protected final String TAG = this.getClass().getSimpleName();
    private BaseApplication app = null;
    private final Location from = new Location(this.getClass().getName());
    protected abstract int getContentView();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = (BaseApplication) getActivity().getApplication();
        app.registerTtListener(this);
    }
    
    @Override
    public final View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	if(getContentView() <=0 )
    		return super.onCreateView(inflater, container, savedInstanceState);
    	initGlobalView();
    	return mGlobalView;
    }
    
    protected ViewGroup getGlobalView(){
		return this.mGlobalView;
	}
    
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mGlobalView = (ViewGroup)view;
//    	ViewTreeObserver vto2 = mGlobalView.getViewTreeObserver();
//        vto2.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
//            @Override
//            public void onGlobalLayout() {
//            	try {
//					mGlobalView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
//					mWidth = mGlobalView.getWidth();
//					mHeight = mGlobalView.getHeight();
//				} catch (Exception e) {
//					e.printStackTrace();
//					DisplayMetrics dm = ScreenUtil.getDisplayMetrics(getActivity());
//					mWidth=dm.widthPixels;
//					mHeight = dm.heightPixels;
//				}
//            }
//        });
		if (actionBarView != null) {
			calcViewSize(actionBarView, new OnViewSizeConfirmed() {
				@Override
				public void onViewSizeConfirmed(View v, int width, int height) {
					actionBarHeight = height;
				}
			});
		}
    }
    
    protected void calcViewSize(final View v, final OnViewSizeConfirmed listener) {
		ViewTreeObserver vto = v.getViewTreeObserver();
		vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				v.getViewTreeObserver().removeGlobalOnLayoutListener(this);
				listener.onViewSizeConfirmed(v, v.getWidth(), v.getHeight());
			}
		});
	}

    @SuppressWarnings("unchecked")
    protected  <V extends View> V find(int id){
        return (V)mGlobalView.findViewById(id);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mGlobalView = null;
        app.unRegisterTtListener(this);
    }
    public final void sendEvent(BaseEvent evt) {
		if(evt.getFrom() == null){
			evt.setFrom(getLocation());
		}
		app.sendEvent(evt);
    }

    public final void sendMessage(Message msg) {
        BaseMessage tmsg = new BaseMessage(from, msg);
        app.sendMessage(tmsg);
    }

    public final void sendEmptyMessage(int what) {
        Message msg = Message.obtain();
        msg.what = what;
        BaseMessage tmsg = new BaseMessage(from, msg);
        app.sendMessage(tmsg);
    }

    public final void sendMessageDelayed(Message msg, long delayMillis) {
        BaseMessage tmsg = new BaseMessage(from, msg);
        app.sendMessageDelayed(tmsg, delayMillis);
    }

    public final void sendEmptyMessageDelayed(int what, long delayMillis) {
        Message msg = Message.obtain();
        msg.what = what;
        BaseMessage tmsg = new BaseMessage(from, msg);
        app.sendMessageDelayed(tmsg, delayMillis);
    }
    
    public final void removeMessage(int what) {
        app.removeMessage(what);
    }

    protected final void checkRunOnUI() {
        app.checkRunOnUI();
    }

    protected final void checkRunOnMain() {
        app.checkRunOnMain();
    }

	protected final Location getLocation() {
		return new Location(this.getClass().getName());
	}

	public static final Location findLocation(Class<?> cls) {
		return new Location(cls.getName());
	}
	
	protected void postRunOnUi(UITask task){
	    app.postRunOnUi(task);
	}

    @Override
    public void executeEvent(BaseEvent evt) {

    }

    @Override
    public void executeMessage(Message msg) {

    }

    @SuppressWarnings("unchecked")
    protected <V extends BaseActivity> V getBaesActivity(){
        return (V)getActivity();
    }

    protected Application getBaseApplication(){
        return getBaesActivity().getBaseApplication();
    }
    
 // ====================actionbar->begin=========================

 	protected ActionBarMenu onActionBarCreate() {
 		return null;
 	}

 	protected ViewGroup mGlobalView = null;
 	ActionBarMenu mActionBar = null;
 	protected volatile int actionBarHeight = 0;
 	View actionBarView = null;

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
 		//ll.setOrientation(LinearLayout.VERTICAL);
 		int width = RelativeLayout.LayoutParams.MATCH_PARENT;
 		int height = RelativeLayout.LayoutParams.MATCH_PARENT;
 		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(width, height);
 		ll.setLayoutParams(lp);
 		return ll;
 	}
 	
 	private View inflateActionBarView() {
 		return LayoutInflater.from(getActivity()).inflate(R.layout.layout_actionbar, null);
 	}

 	@Override
 	public void onActionBarClick(View v) {

 	}

	protected int getActionBarHeight() {
		return this.actionBarHeight;
	}
	protected ActionBarMenu getActionBarMenu(){
		return this.mActionBar;
	}	
  	
  //=================进度条==============
	private View getProgressBarView(){
		View view = LayoutInflater.from(getActivity()).inflate(R.layout.layout_progress_bar, null);
		return view;
	}
 	
	AlertDialog progressDialog = null;
	/** 弹出对证框 */
	@SuppressLint("NewApi")
	protected final AlertDialog showProgressDialog(String message){
		if(progressDialog != null){
			closeProgressDialog();
		}	
		if(getBaesActivity().isRunning()==false){
			return null;
		}
		progressDialog = LightProgressDialog.create(getActivity(), message);
		progressDialog.show();
		View view = getProgressBarView();
		TextView tv = (TextView)view.findViewById(R.id.layout_progress_bar_tv_loading);
		tv.setText(message);
		progressDialog.setContentView(view, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		return progressDialog;
	}
	
	/**关闭对话框*/
	protected final void closeProgressDialog(){
		if(progressDialog == null)
			return ;
		progressDialog.dismiss();
		progressDialog = null;
	}
	
	//==================网络设置dialog=================
	AlertDialog networkDialog = null;
	protected AlertDialog showNetWorkDialog(String title,String message){
		if(networkDialog != null)
			closeNetWorkDialog();
		networkDialog = LightNetWorkSetDialog.create(getActivity(), title, message);
		networkDialog.show();
		return networkDialog;
	}
	protected void closeNetWorkDialog(){
		if(networkDialog == null)
			return ;
		networkDialog.dismiss();
		networkDialog  = null;
	}
}
