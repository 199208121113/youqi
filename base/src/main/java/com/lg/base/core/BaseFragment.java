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
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lg.base.R;
import com.lg.base.bus.BaseEvent;
import com.lg.base.bus.EventBus;
import com.lg.base.bus.EventHandListener;
import com.lg.base.bus.EventLocation;
import com.lg.base.ui.dialog.LightNetWorkSetDialog;
import com.lg.base.ui.dialog.LightProgressDialog;



@SuppressWarnings("javadoc")
/**
 * description:RoboFragment,RoboSherlockFragment
 * 跟BaseActivity类似
 * @author liguo
 *
 */
public abstract class BaseFragment extends Fragment implements EventHandListener {
    protected final String TAG = this.getClass().getSimpleName();
    private final EventLocation from = new EventLocation(this.getClass().getName());
    protected abstract int getContentView();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		EventBus.get().register(this);
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
		InjectManager.init(this);
		if(view instanceof ViewGroup) {
			mGlobalView = (ViewGroup) view;
		}
    }

    @SuppressWarnings("unchecked")
    protected  <V extends View> V find(int id){
        return (V)mGlobalView.findViewById(id);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mGlobalView = null;
		EventBus.get().unRegister(this);
    }

	protected final EventLocation getLocation() {
		return new EventLocation(this.getClass().getName());
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

 	protected BaseActivity.ActionBarMenu onActionBarCreate() {
 		return null;
 	}

 	protected ViewGroup mGlobalView = null;
 	BaseActivity.ActionBarMenu mActionBar = null;
 	View actionBarView = null;

 	private void initGlobalView() {
 		ViewGroup viewGroup = (ViewGroup) LayoutInflater.from(getActivity()).inflate(getContentView(), null);
 		mActionBar = onActionBarCreate();
 		mGlobalView = getRelativeLayout();
 		if (mActionBar != null) {
 			LinearLayout myLinearLayout = getLinearLayout();
 			actionBarView = inflateActionBarView();
			mActionBar.setView(actionBarView);
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
 	
 	protected View inflateActionBarView() {
 		return LayoutInflater.from(getActivity()).inflate(R.layout.layout_actionbar, null);
 	}

	protected BaseActivity.ActionBarMenu getActionBarMenu(){
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
	public void setOnActionBarItemClickListener(BaseActivity.OnActionBarItemClickListener listener) {
		if (listener == null)
			return;
		if (mActionBar == null || actionBarView == null) {
			return;
		}
		mActionBar.setListener(listener);
	}
}
