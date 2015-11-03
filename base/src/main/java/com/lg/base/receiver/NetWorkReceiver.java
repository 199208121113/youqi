package com.lg.base.receiver;

import android.content.Context;
import android.content.Intent;

import com.google.inject.Inject;
import com.lg.base.core.BaseReceiver;
import com.lg.base.core.Location;
import com.lg.base.event.NetWorkEvent;
import com.lg.base.event.NetWorkEvent.NetWorkType;
import com.lg.base.utils.NetworkUtil;

public class NetWorkReceiver extends BaseReceiver {
	@Inject
	NetworkUtil networkUtil;
	@Override
	protected void doReceive(Context context, Intent intent) {
		boolean available = networkUtil.isAvailable(context);
		NetWorkType nt = NetWorkType.gprs;
		if(networkUtil.isWifi(context)){
			nt = NetWorkType.wifi;
		}else if(networkUtil.isGPRS(context)){
			nt = NetWorkType.gprs;
		}else{
			nt = NetWorkType.other;
		}
		NetWorkEvent evt = new NetWorkEvent(Location.any, available, nt);
		sendEvent(evt);
	}

}
