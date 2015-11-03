package com.lg.base.ui.holder;

import android.view.ViewGroup;

public interface IOnScrollListener {
	
	public void onScrollStateChanged(ViewGroup vg, int scrollState);

	public void onScroll(ViewGroup vg, int firstVisibleItem, int visibleItemCount, int totalItemCount);
}
