package com.lg.base.core;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.lg.base.bus.EventLocation;

public abstract class BaseReceiver extends BroadcastReceiver{

	private final EventLocation from = new EventLocation(this.getClass().getName());
	protected final String tag = this.getClass().getSimpleName();

	@Override
	public void onReceive(Context context, Intent intent) {
		try {
			doReceive(context, intent);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected abstract void doReceive(Context context, Intent intent);
	
	protected EventLocation getLocation(){
		return new EventLocation(this.getClass().getName());
	}
	
}
