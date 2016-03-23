package com.lg.base.core;

import android.content.Context;
import android.os.Bundle;

public abstract class UITask implements Runnable {

	protected Bundle extra;
	protected Context context;
	public UITask(Context ctx, Bundle extra) {
		super();
		this.extra = extra;
		this.context = ctx;
	}

	public UITask(Context context) {
		super();
		this.context = context;
	}
	
	public UITask() {
		super();
	}

	public Bundle getExtra() {
		return extra;
	}

	public Context getContext() {
		return context;
	}

}
