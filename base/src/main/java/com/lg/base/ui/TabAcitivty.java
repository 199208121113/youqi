package com.lg.base.ui;

import android.annotation.TargetApi;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabContentFactory;
import android.widget.TabHost.TabSpec;
import android.widget.TabWidget;
import android.widget.TextView;

import com.lg.base.R;
import com.lg.base.core.BaseActivity;

import java.util.HashMap;
import java.util.Map;

import roboguice.inject.InjectView;

public abstract class TabAcitivty extends BaseActivity implements OnTabChangeListener, TabContentFactory {

	protected final String tag = this.getClass().getSimpleName();
	protected TabHost mTabHost = null;

	@InjectView(android.R.id.tabs)
	protected TabWidget mTabWidget = null;

	protected LinearLayout dotsLayout = null;

	private final HashMap<String, TabInfo> mTabs = new HashMap<String, TabInfo>();
	private TabInfo mLastTab = null;

	private static final class TabInfo {
		private final String tag;
		private final Class<?> clss;
		private final Bundle args;
		private Fragment fg;
		private int tabIndex;
		public TabInfo(String tag, Class<?> clss, Bundle args,int tabIndex) {
			super();
			this.tag = tag;
			this.clss = clss;
			this.args = args;
			this.tabIndex = tabIndex;
		}

		private Fragment getFragment() {
			return this.fg;
		}

		private void setFragment(Fragment fg) {
			this.fg = fg;
		}
	}

	@Override
	public View createTabContent(String tag) {
		return new View(getApplicationContext());
	}

	@Deprecated
	public void onTabChangedOld(String tabId) {
		TabInfo newTab = mTabs.get(tabId);
		if (mLastTab != newTab) {
			FragmentTransaction ft = getFragmentTransaction();
			if (mLastTab != null) {
				if (mLastTab.getFragment() != null) {
					ft.detach(mLastTab.getFragment());
				}
			}
			if (newTab != null) {
				if (newTab.getFragment() == null || newTab.getFragment().isDetached()) {
					newTab.setFragment(Fragment.instantiate(this, newTab.clss.getName(), newTab.args));
					ft.add(android.R.id.tabcontent, newTab.getFragment(), newTab.tag);
				} else {
					ft.attach(newTab.getFragment());
				}
			}
			mLastTab = newTab;
			ft.commitAllowingStateLoss();
			getSupportFragmentManager().executePendingTransactions();
		}
		onTabChanged(mTabHost.getCurrentTab());
	}

	@Override
	public void onTabChanged(String tabId) {
		TabInfo newTab = mTabs.get(tabId);
		if (getNeedDestoryOnTabChanged()) { // 需要销毁
			if (mLastTab != newTab) {
				FragmentTransaction ft = getFragmentTransaction();
				if (newTab.getFragment() == null || newTab.getFragment().isDetached()) {
					newTab.setFragment(Fragment.instantiate(this, newTab.clss.getName(), newTab.args));
				}
				ft.replace(android.R.id.tabcontent, newTab.getFragment());
				ft.commitAllowingStateLoss();
				mLastTab = newTab;
				getSupportFragmentManager().executePendingTransactions();
			}
		} else { // 不需要销毁
			if (mLastTab != newTab) {
				FragmentTransaction ft = getFragmentTransaction();
				Fragment newFragment = newTab.getFragment();
				if (newFragment == null || newFragment.isDetached()) {
					newFragment = Fragment.instantiate(this, newTab.clss.getName(), newTab.args);
					newTab.setFragment(newFragment);
				}
				if (mLastTab != null) {
					ft.hide(mLastTab.getFragment());
				}
				if (!newFragment.isAdded()) {
					ft.add(android.R.id.tabcontent, newFragment, newTab.tag);
				} 
				ft.show(newFragment);
				ft.commitAllowingStateLoss();
				mLastTab = newTab;
				getSupportFragmentManager().executePendingTransactions();
			}
		}
		onTabChanged(mTabHost.getCurrentTab());
	}
	
	private FragmentTransaction getFragmentTransaction(){
		Fragment_Animation_Type animationType = getAnimationType();
		if(animationType == null){
			animationType = Fragment_Animation_Type.fade_in_and_slide_out;
		}
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		if(animationType == Fragment_Animation_Type.none){
			return ft;
		}
		int curIndex = mTabHost.getCurrentTab();
		int oldIndex = mLastTab == null ? curIndex : mLastTab.tabIndex;
		int a1 = 0;
		int a2 = 0;
		if(curIndex > oldIndex){//下一页
			a1 = R.anim.slideleft_in;
			a2 = R.anim.slideleft_out;
		}else if(curIndex < oldIndex){ //上页
			a1 = R.anim.slideright_in;
			a2 = R.anim.slideright_out;
		}
		if(animationType == Fragment_Animation_Type.fade_in_and_slide_out){
			a2 = android.R.anim.fade_out;
			if(curIndex > oldIndex){//下一页
				a2 = R.anim.slideleft_out;
			}else if(curIndex < oldIndex){ //上页
				a2 = R.anim.slideright_out;
			}
			return ft.setCustomAnimations(android.R.anim.fade_in,a2);
		}else{
			if(a1 > 0 && a2 > 0){
				ft.setCustomAnimations(a1,a2);
			}
			return ft;
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		findViewById(android.R.id.tabcontent).setVisibility(View.VISIBLE);
		mTabHost = (TabHost) findViewById(android.R.id.tabhost);
		mTabHost.setOnTabChangedListener(this);
		mTabHost.setup();
		dotsLayout = find(R.id.act_tabhost_tips_layout);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		if(mLastTab == null){
			return;
		}
		Fragment newFragment = mLastTab.getFragment();
		if (newFragment == null || newFragment.isDetached()) {
			FragmentTransaction ft = getFragmentTransaction();
			newFragment = Fragment.instantiate(this, mLastTab.clss.getName(), mLastTab.args);
			mLastTab.setFragment(newFragment);
			if (!newFragment.isAdded()) {
				ft.add(android.R.id.tabcontent, newFragment, mLastTab.tag);
			} 
			ft.show(newFragment);
			ft.commitAllowingStateLoss();
			getSupportFragmentManager().executePendingTransactions();
		}
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putInt("tabIndex", mTabHost.getCurrentTab());
		super.onSaveInstanceState(outState);
	}
	
	private void addTab(TabSpec tabSpec, Class<?> cls) {
		tabSpec.setContent(this);
		String tag = tabSpec.getTag();
		TabInfo info = new TabInfo(tag, cls, null,mTabs.size());
		info.setFragment(getSupportFragmentManager().findFragmentByTag(tag));
		if (info.getFragment() != null && !info.getFragment().isDetached()) {
			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			ft.detach(info.getFragment());
			ft.commit();
		}
		mTabs.put(tag, info);
		mTabHost.addTab(tabSpec);
	}

	protected void addTab(String title, int imageResourceId, int backgroundResourceId, Class<?> cls, AddTabCallBack listener) {
		TabSpec spec = mTabHost.newTabSpec(cls.getSimpleName());
		View view = getLayoutInflater().inflate(R.layout.item_tab, null);
		TextView tv = ((TextView) view.findViewById(R.id.tv_tab));
		if (title != null && title.trim().length() > 0) {
			ColorStateList csl = getTextColorStateList();
			if (csl != null) {
				tv.setTextColor(csl);
			} else {
				tv.setTextColor(getResources().getColor(getTextColor()));
			}
			tv.setText(title.trim());
			tv.setVisibility(View.VISIBLE);
		} else {
			tv.setText("");
			tv.setVisibility(View.GONE);
		}
		ImageView iv = ((ImageView) view.findViewById(R.id.iv_tab));
		if (imageResourceId > 0) {
			iv.setImageResource(imageResourceId);
		} else {
			iv.setVisibility(View.GONE);
		}
		if (backgroundResourceId > 0) {
			//view.setBackgroundResource(backgroundResourceId);
			int color = getResources().getColor(backgroundResourceId);
			view.setBackgroundColor(color);
		}
		if (listener != null) {
			listener.callBack(view, iv, tv);
		}
		TextView tvRedPoint = (TextView)view.findViewById(R.id.item_tab_red_point);
		Map<String,String> sab = buildShowReadPointMap();
		if(sab != null && sab.containsKey(spec.getTag())){
			tvRedPoint.setVisibility(View.VISIBLE);
		}else{
			tvRedPoint.setVisibility(View.GONE);
		}
		spec.setIndicator(view);
		addTab(spec, cls);
	}

	protected void addTab(String title, int imageResourceId, int backgroundResourceId, Class<?> cls) {
		addTab(title, imageResourceId, backgroundResourceId, cls, null);
	}

	@Override
	protected int getContentView() {
		return R.layout.act_tabhost;
	}

	public interface AddTabCallBack {
		void callBack(View itemView, ImageView iv, TextView tv);
	}

	// ======================切换时是否销毁Fragment===========================
	/** 当tab选项卡发生改变时是否需要销毁fragment */
	protected boolean getNeedDestoryOnTabChanged() {
		return false;
	}

	// =====================分隔符========================
	/** 隐藏tabWidget的分隔线,默认不显示 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
    protected final void setDriversVisiable(boolean show) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            int n = show ? TabWidget.SHOW_DIVIDER_MIDDLE : TabWidget.SHOW_DIVIDER_NONE;
            mTabWidget.setShowDividers(n);
        }
	}

	// =====================文字颜色========================
	/** 字体颜色状态列表 */
	protected ColorStateList getTextColorStateList() {
		// getResources().getColorStateList(xml->selecter)
		return null;
	}

	protected int getTextColor() {
		return android.R.color.white;
	}

	// =====================TabWidget的指示条========================
	/** TabWidget的指示条资源ID,为selecter */
	protected int getDotsResourceId() {
		return R.drawable.sl_tab_dots;
	}

	/** 显示TabWidget指示条,默认是不显示的 */
	protected final void setDotsLayoutVisiable(boolean show) {
		int n = show ? View.VISIBLE : View.GONE;
		dotsLayout.setVisibility(n);
	}

	/** 初始化TabWidget */
	protected final void initDotsView() {
		int n = dotsLayout.getVisibility();
		if (n != View.VISIBLE)
			return;
		int c = mTabWidget.getTabCount();
		if (dotsLayout.getChildCount() > 0) {
			dotsLayout.removeAllViews();
		}
		// 广告指示栏
		for (int i = 0; i < c; i++) {
			ImageView iv = new ImageView(this);
			LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
			iv.setLayoutParams(lp);
			iv.setBackgroundResource(getDotsResourceId());
			lp.weight = 1;
			dotsLayout.addView(iv);
			iv.setEnabled(i == 0);
		}
	}

	protected void onTabChanged(int pos) {
		final int childCount = dotsLayout.getChildCount();
		if (childCount == 0)
			return;
		int index = pos <= childCount - 1 ? pos : pos % childCount;
		for (int i = 0; i < childCount; i++) {
			dotsLayout.getChildAt(i).setEnabled(i == index);
		}
	}

	// =====================TabWidget的北景色========================
	/**
	 * @param bgResId
	 *            颜色值，需要在color.xml中定义
	 */
	protected void setTabBackgroud(int bgResId) {
		mTabWidget.setBackgroundResource(bgResId);
	}

	protected void setTabIndex(int pos) {
		mTabHost.setCurrentTab(pos);
	}
	
	/**
	 * 1:fade_in&fade_out
	 * 2:slideleft_in_out&slideright_in_out
	 * */
	protected Fragment_Animation_Type getAnimationType(){
		return Fragment_Animation_Type.fade_in_and_slide_out;
	}
	
	public static enum Fragment_Animation_Type{
		none,fade_in_and_slide_out,slide_left_right_in_out
	}

	protected Map<String,String> buildShowReadPointMap(){
		return null;
	}
	protected void hideCurrentRedPoint(){
		View view = mTabHost.getCurrentTabView();
		if(view == null){
			return;
		}
		TextView tvRedPoint = (TextView)view.findViewById(R.id.item_tab_red_point);
		if(tvRedPoint != null){
			tvRedPoint.setVisibility(View.GONE);
		}
	}

}
