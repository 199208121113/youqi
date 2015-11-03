package com.lg.base.task;

import com.lg.base.core.Location;

public class MsgTaskEvent extends TaskEvent{

	private static final long serialVersionUID = 1L;
	private int what;
	public MsgTaskEvent(Location from, Location to, Object data) {
		super(from, to, data);
	}

	public MsgTaskEvent(Location from, Location to) {
		super(from, to);
	}

	public MsgTaskEvent(Location to,int what) {
		super(to);
		this.what = what;
	}

	public int getWhat() {
		return what;
	}
	

}
