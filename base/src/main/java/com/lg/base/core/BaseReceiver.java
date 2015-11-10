package com.lg.base.core;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Message;

public abstract class BaseReceiver extends BroadcastReceiver implements MessageSendListener{

	private final Location from = new Location(this.getClass().getName());
	protected final String tag = this.getClass().getSimpleName();
	private BaseApplication app = null;

	@Override
	public void onReceive(Context context, Intent intent) {
		this.app = (BaseApplication) context.getApplicationContext();
		try {
			doReceive(context, intent);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected abstract void doReceive(Context context, Intent intent);
	
	public final void sendEvent(BaseEvent evt) {
		if(evt.getFrom() == null){
			evt.setFrom(getLocation());
		}
		app.sendEvent(evt);
	}

	public final void sendMessage(Message msg) {
		BaseMessage tmsg = new BaseMessage(from, msg);
		app.sendMessage(tmsg);
	}

	public final void sendEmptyMessage(int what) {
		Message msg = Message.obtain();
		msg.what = what;
		BaseMessage tmsg = new BaseMessage(from, msg);
		app.sendMessage(tmsg);
	}

	public final void sendMessageDelayed(Message msg, long delayMillis) {
		BaseMessage tmsg = new BaseMessage(from, msg);
		app.sendMessageDelayed(tmsg, delayMillis);
	}

	public final void sendEmptyMessageDelayed(int what, long delayMillis) {
		Message msg = Message.obtain();
		msg.what = what;
		BaseMessage tmsg = new BaseMessage(from, msg);
		app.sendMessageDelayed(tmsg, delayMillis);
	}

	protected Location getLocation(){
		return new Location(this.getClass().getName());
	}
	
	protected Location findLocation(Class<?> cls){
		return new Location(cls.getName());
	}

}
