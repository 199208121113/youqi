package com.lg.base.core;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Message;

import com.lg.base.bus.BaseEvent;
import com.lg.base.bus.EventBus;
import com.lg.base.bus.EventHandListener;
import com.lg.base.bus.EventLocation;
import com.lg.base.event.NetWorkEvent;

public abstract class BaseService extends Service implements EventHandListener {

	protected final String tag = this.getClass().getSimpleName()+"::";
	private BaseApplication app = null;

	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		EventBus.get().register(this);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		EventBus.get().unRegister(this);
	}

	@Override
	public void executeEvent(BaseEvent evt) {
		if(evt instanceof NetWorkEvent){
			NetWorkEvent event = (NetWorkEvent)evt;
			onNetworkStateChanged(event);
			return ;
	    }
	}

	@Override
	public void executeMessage(Message msg) {

	}

	protected final void checkRunOnUI(){
		app.checkRunOnUI();
	}
	
	protected final void checkRunOnMain(){
		app.checkRunOnMain();
	}
	
	protected final EventLocation getLocation(){
		return new EventLocation(this.getClass().getName());
	}
	
	/** this method is running on Main-Thread,not on UI-Thread */
	protected void onNetworkStateChanged(NetWorkEvent evt){
		if(evt.isAvailable()){
			LogUtil.d(tag, "当前网络可用,类型:"+evt.getNetWorkType().name());
		}else{
			LogUtil.d(tag, "当前网络不可用");
		}
	}
}
