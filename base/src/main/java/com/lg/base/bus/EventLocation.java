package com.lg.base.bus;

public class EventLocation {
	private String uri = null;
	public static final EventLocation any = new EventLocation("-ALL-");
	public EventLocation(String uri) {
		super();
		if(uri == null || uri.trim().length() == 0)
			throw new IllegalArgumentException("uri is null");
		this.uri = uri.trim();
	}
	public String getUri() {
		return uri;
	}
}
