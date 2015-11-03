package com.lg.base.ui.adapter;

import android.view.View;


public interface OnAdapterItemStateChangeListener<DATA, STATE> {
	void onStateChanged(AdapterItem<DATA, STATE> item, View v, int... posIndex);
	
}