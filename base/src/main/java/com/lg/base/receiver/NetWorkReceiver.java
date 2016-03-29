package com.lg.base.receiver;

import android.content.Context;
import android.content.Intent;

import com.lg.base.core.BaseReceiver;
import com.lg.base.bus.EventBus;
import com.lg.base.bus.EventLocation;
import com.lg.base.event.NetWorkEvent;
import com.lg.base.event.NetWorkEvent.NetWorkType;
import com.lg.base.utils.NetworkUtil;

public class NetWorkReceiver extends BaseReceiver {

	@Override
	protected void doReceive(Context context, Intent intent) {
		boolean available = NetworkUtil.isAvailable(context);
		NetWorkType nt;
		if(NetworkUtil.isWifi(context)){
			nt = NetWorkType.wifi;
		}else if(NetworkUtil.isGPRS(context)){
			nt = NetWorkType.gprs;
		}else{
			nt = NetWorkType.other;
		}
		NetWorkEvent evt = new NetWorkEvent(EventLocation.any, available, nt);
		EventBus.get().sendEvent(evt);
	}

}
