package com.lg.base.core;

import java.io.Serializable;
import java.util.HashMap;

public class BaseEvent implements Serializable, Cloneable{
	
	private static final long serialVersionUID = 1L;
	private Location from = null;
	private Location to = null;
	private Object data = null;
	public BaseEvent(Location from, Location to, Object data) {
		this(from,to);
		this.data = data;
	}
	public BaseEvent(Location from, Location to) {
		this(to);
		this.from = from;
	}
	public BaseEvent(Location to) {
		this.to = to;
	}
	public Location getFrom() {
		return from;
	}
	public Location getTo() {
		return to;
	}
	public Object getData() {
		return data;
	}
	public void setData(Object data) {
		this.data = data;
	}
	public void setFrom(Location from) {
		this.from = from;
	}
	@Override
	public Object clone()  {
		BaseEvent e = null;
		try {
			e = (BaseEvent) super.clone();
		} catch (CloneNotSupportedException e1) {
			e1.printStackTrace();
		}
		return e;
	}
	
	private int what = 0;
	public int getWhat() {
		return what;
	}
	public void setWhat(int what) {
		this.what = what;
	}
	public BaseEvent(Location to,int what) {
		this.to = to;
		this.what = what;
	}
	
	private HashMap<String,String> extra = null;
	public HashMap<String, String> getExtra() {
		return extra;
	}
	public void setExtra(HashMap<String, String> extra) {
		this.extra = extra;
	}
}
