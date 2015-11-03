package com.lg.base.event;

import com.lg.base.core.BaseEvent;
import com.lg.base.core.Location;

public class SdCardEvent extends BaseEvent {

	private static final long serialVersionUID = 1L;

	public SdCardEvent(Location from, Location to,SdCardState st) {
		super(from, to);
		this.state = st;
	}
	SdCardState state;
	boolean mounted;

	public SdCardState getState() {
		return state;
	}
	public static enum SdCardState {
		mounted, unmounted;
	}
}
