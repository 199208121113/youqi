package com.lg.base.ui.adapter;

import android.content.res.ColorStateList;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public abstract class BaseTabPagerAdapter extends FragmentPagerAdapter {

	public BaseTabPagerAdapter(FragmentManager fm) {
		super(fm);
	}

	/** 选项卡的图片 */
	public abstract int getPageImageResourceId(int position);

	/** 选项卡的背景 */
	public abstract int getPageBackgroupResourceId(int position);

	/** 字体颜色状态列表 */
	public ColorStateList getTextColorStateList() {
		// getResources().getColorStateList(xml->selecter)
		return null;
	}

}
