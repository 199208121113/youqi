package com.lg.base.task;

import com.lg.base.bus.EventLocation;

public class MsgTaskEvent extends TaskEvent{

	private static final long serialVersionUID = 1L;
	private int what;
	public MsgTaskEvent(EventLocation from, EventLocation to, Object data) {
		super(from, to, data);
	}

	public MsgTaskEvent(EventLocation from, EventLocation to) {
		super(from, to);
	}

	public MsgTaskEvent(EventLocation to,int what) {
		super(to);
		this.what = what;
	}

	public int getWhat() {
		return what;
	}
	

}
