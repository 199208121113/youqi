package com.lg.base.ui.adapter;

import android.view.View;



public class AdapterItem<DATA, STATE> {
	
	private DATA data;
	private STATE state;
	private OnAdapterItemStateChangeListener<DATA, STATE> onAdapterItemStateChangeListener = null;

	public AdapterItem(DATA data, STATE state) {
		if (data == null)
			throw new IllegalArgumentException("data is null");
		this.data = data;
		this.state = state;
	}

	public DATA getData() {
		return data;
	}

	public STATE getState() {
		return state;
	}

	public void notifyStateChanged(View v,int... posIndex) {
		OnAdapterItemStateChangeListener<DATA, STATE> listener = getOnAdapterItemStateChangeListener();
		if (listener != null)
			listener.onStateChanged(this,v,posIndex);
	}

	public void setOnAdapterItemStateChangeListener(OnAdapterItemStateChangeListener<DATA, STATE> onAdapterItemStateChangeListener) {
		this.onAdapterItemStateChangeListener = onAdapterItemStateChangeListener;
	}

	public OnAdapterItemStateChangeListener<DATA, STATE> getOnAdapterItemStateChangeListener() {
		return onAdapterItemStateChangeListener;
	}
}