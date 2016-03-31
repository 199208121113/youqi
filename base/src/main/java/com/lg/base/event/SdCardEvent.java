package com.lg.base.event;

import com.lg.base.bus.BaseEvent;
import com.lg.base.bus.EventLocation;

public class SdCardEvent extends BaseEvent {

	public SdCardEvent(EventLocation from, EventLocation to,SdCardState st) {
		super(from, to);
		this.state = st;
	}
	SdCardState state;

	public SdCardState getState() {
		return state;
	}
	public enum SdCardState {
		mounted, unmounted;
	}
}
