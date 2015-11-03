package com.lg.base.core;

import android.os.Message;

public class BaseMessage {
	private Location from = null;
	private Message msg = null;
	public BaseMessage(Location from, Message msg) {
		super();
		this.from = from;
		this.msg = msg;
	}
	public Location getFrom() {
		return from;
	}
	public Message getMsg() {
		return msg;
	}
}
