package com.lg.base.core;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public abstract class BaseReceiver extends BroadcastReceiver{

	private final Location from = new Location(this.getClass().getName());
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
	
	protected Location getLocation(){
		return new Location(this.getClass().getName());
	}
	
}
