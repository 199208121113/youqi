package com.lg.base.ui;

import android.os.Bundle;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;

import com.lg.base.R;
import com.lg.base.core.BaseActivity;

public abstract class PagerAcitivty<V extends FragmentPagerAdapter> extends BaseActivity implements OnPageChangeListener {
	
	protected final String tag = this.getClass().getSimpleName();
	protected BaseViewPager mViewPager = null;
	protected V mAdapter = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mAdapter = createAdapter();
		if(mAdapter != null){
			mViewPager =find(R.id.act_viewpager);
			mViewPager.setVisibility(View.VISIBLE);
			mViewPager.setOnPageChangeListener(this);
			mViewPager.setAdapter(mAdapter);
//			mViewPager.setTransitionEffect(getTransitionEffect());
		}
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {
	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
	}

	@Override
	public void onPageSelected(int position) {
	    
	}
	protected abstract V createAdapter();
	
//	/** viewpager的切换效果,如果需要定制可以重写此方法 */
//	protected TransitionEffect getTransitionEffect(){
//		return TransitionEffect.CubeOut;
//	}

}
