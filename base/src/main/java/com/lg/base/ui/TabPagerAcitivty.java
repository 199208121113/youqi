package com.lg.base.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabContentFactory;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

import com.lg.base.R;
import com.lg.base.core.BaseActivity;
import com.lg.base.ui.adapter.BaseTabPagerAdapter;

public abstract class TabPagerAcitivty<V extends BaseTabPagerAdapter> extends BaseActivity implements OnTabChangeListener, TabContentFactory, OnPageChangeListener {

	protected final String tag = this.getClass().getSimpleName();
	protected TabHost mTabHost = null;
	protected BaseViewPager mViewPager = null;
	protected V mAdapter = null;

	@Override
	public View createTabContent(String tag) {
		return new View(getApplicationContext());
	}

	@Override
	public void onTabChanged(String tabId) {
		mViewPager.setCurrentItem(mTabHost.getCurrentTab(),true);
		mViewPager.getAdapter().notifyDataSetChanged();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mViewPager = (BaseViewPager)findViewById(R.id.act_tabhost_viewpager);
		mViewPager.setDuration(500);
		mAdapter = createAdapter();
		if (mAdapter != null) {
			mViewPager.setVisibility(View.VISIBLE);
			mViewPager.setOnPageChangeListener(this);
			mViewPager.setAdapter(mAdapter);
//			if(mViewPager instanceof JazzyViewPager){
//				((JazzyViewPager)mViewPager).setTransitionEffect(getTransitionEffect());
//			}
		}
		mTabHost = (TabHost) findViewById(android.R.id.tabhost);
		mTabHost.setOnTabChangedListener(this);
		mTabHost.setup();
		createTabs();
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {
	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
	}

	@Override
	public void onPageSelected(int position) {
		mTabHost.setCurrentTab(position);
	}

	private void createTabs() {
		if (mTabHost.getTabWidget().getTabCount() > 0) {
			mTabHost.setCurrentTab(0);
			mTabHost.clearAllTabs();
		}
		int count = mAdapter.getCount();
		for (int i = 0; i < count; i++) {
			TabSpec spec = mTabHost.newTabSpec("tag_tab_" + i);
			spec.setContent(this);
			View view = getLayoutInflater().inflate(getItemLayout(), null);
			TextView tvTitle = (TextView) view.findViewById(R.id.tv_tab);
			tvTitle.setText(mAdapter.getPageTitle(i));
            tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP,getTextSize());
			if(mAdapter.getTextColorStateList() != null){
				tvTitle.setTextColor(mAdapter.getTextColorStateList());
			}
			ImageView iv = ((ImageView) view.findViewById(R.id.iv_tab));
			if (mAdapter.getPageImageResourceId(i) > 0) {
				iv.setImageResource(mAdapter.getPageImageResourceId(i));
			}else{
                iv.setVisibility(View.GONE);
            }
			if (mAdapter.getPageBackgroupResourceId(i) > 0) {
				view.setBackgroundResource(mAdapter.getPageBackgroupResourceId(i));
			}
			spec.setIndicator(view);
			addTab(spec, Fragment.class);
		}
	}
	protected int getItemLayout(){
		return R.layout.item_tab;
	}

	private void addTab(TabSpec tabSpec, Class<?> cls) {
		tabSpec.setContent(this);
		mTabHost.addTab(tabSpec);
	}

	protected abstract V createAdapter();
	
//	/** viewpager的切换效果,如果需要定制可以重写此方法 */
//	protected TransitionEffect getTransitionEffect(){
//		return TransitionEffect.CubeOut;
//	}

    protected int getTextSize(){
        return 12;
    }
}
