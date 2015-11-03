package com.lg.base.core;

public class DoWhat {
	private int what = 0;
	private Object obj = null;
	public DoWhat(int what, Object obj) {
		this(what);
		this.obj = obj;
	}
	public DoWhat(int what) {
		super();
		this.what = what;
	}
	public int getWhat() {
		return what;
	}
	public Object getObj() {
		return obj;
	}
}
