package com.lg.base.core;

public class Location {
	private String uri = null;
	public static final Location any = new Location("-ALL-");
	public Location(String uri) {
		super();
		if(uri == null || uri.trim().length() == 0)
			throw new IllegalArgumentException("uri is null");
		this.uri = uri.trim();
	}
	public String getUri() {
		return uri;
	}
}
