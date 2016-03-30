package com.lg.base.core;

import android.content.Context;
import android.os.Bundle;

public abstract class UITask implements Runnable {

	protected Object data;
	protected Bundle extra;
	protected Context context;

	public UITask(Context context) {
		super();
		this.context = context;
	}
	
	public UITask() {
		super();
	}

	public Object getData() {
		return data;
	}

	public UITask setData(Object data) {
		this.data = data;
		return this;
	}

	public Bundle getExtra() {
		return extra;
	}

	public UITask setExtra(Bundle extra) {
		this.extra = extra;
		return this;
	}

	public Context getContext() {
		return context;
	}

	public UITask setContext(Context context) {
		this.context = context;
		return this;
	}
}
