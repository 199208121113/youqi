package com.lg.base.ui.adapter;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public abstract class BaseTabStatePagerAdapter extends FragmentStatePagerAdapter {

	public BaseTabStatePagerAdapter(FragmentManager fm) {
		super(fm);
	}

	/** 选项卡的图片 */
	public abstract int getPageImageResourceId(int position);

	/** 选项卡的背景 */
	public abstract int getPageBackgroupResourceId(int position);

}
