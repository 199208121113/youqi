package com.lg.base.event;

import com.lg.base.core.BaseEvent;
import com.lg.base.core.Location;

public class NetWorkEvent extends BaseEvent {
	
	private static final long serialVersionUID = 1L;
	boolean available = false;
	NetWorkType netWorkType = null;
	public NetWorkEvent(Location to,boolean available,NetWorkType nt) {
		super(to);
		this.available = available;
		this.netWorkType = nt;
	}
	public static enum NetWorkType{
		gprs,wifi,other
	}
	public boolean isAvailable() {
		return available;
	}
	public NetWorkType getNetWorkType() {
		return netWorkType;
	}
	
	
}
