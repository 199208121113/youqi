package com.lg.base.event;

import com.lg.base.core.BaseEvent;
import com.lg.base.core.Location;

public class ActivityStateEvent extends BaseEvent {
	private static final long serialVersionUID = 1L;
	private State state = null;
	public ActivityStateEvent(Location from, Location to, Object data,State s) {
		super(from, to, data);
		this.state = s;
	}
	public ActivityStateEvent(Location from, Location to,State s) {
		super(from, to);
		this.state = s;
	}
	public State getState() {
		return this.state;
	}
	public static enum State{
		create,start,stop,destroy,restart,pause,resume
	}
}
