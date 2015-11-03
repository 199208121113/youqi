package com.lg.base.event;

import com.lg.base.core.BaseEvent;
import com.lg.base.core.Location;

public class ServiceStateEvent extends BaseEvent {

	private static final long serialVersionUID = 1L;
	private State state = null;
	public ServiceStateEvent(Location from, Location to, Object data,State status) {
		super(from, to, data);
		this.state = status;
	}

	public ServiceStateEvent(Location from, Location to,State status) {
		super(from, to);
		this.state = status;
	}

	public State getState() {
		return this.state;
	}
	public static enum State{
		create,destroy
	}
}
