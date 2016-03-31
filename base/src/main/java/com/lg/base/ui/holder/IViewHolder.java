package com.lg.base.ui.holder;

import android.view.View;

import com.lg.base.ui.adapter.AdapterItem;

public interface IViewHolder<DATA, STATE>  {

	void setItem(AdapterItem<DATA, STATE> item);

	AdapterItem<DATA, STATE> getItem();

	void bindItem();

	void refreshView();

	View getRootView();

	void initViews();

	void recycleItem();

	void destroy();
}
