package com.lg.base.event;


import com.lg.base.core.BaseEvent;
import com.lg.base.core.Location;

public class PackageEvent extends BaseEvent {

	private static final long serialVersionUID = 1L;
	private String packageName = null;
	private State state = null;

	public PackageEvent(Location from, Location to, String packageName, State st) {
		super(from, to);
		this.packageName = packageName;
		this.state = st;
	}

	public String getPackageName() {
		return packageName;
	}

	public State getState() {
		return state;
	}

	public enum State {
		added, removed, replaced
	}
}
