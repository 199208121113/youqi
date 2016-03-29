package com.lg.base.bus;

import android.os.Bundle;

import java.io.Serializable;

public class BaseEvent implements Serializable, Cloneable{
	
	private static final long serialVersionUID = 1L;

	private int what = 0;
	private EventLocation from = null;
	private EventLocation to = null;
	private Object data = null;
	private Bundle bundle = null;
	private EventThread runOnThread = EventThread.IO;

	public BaseEvent(EventLocation from, EventLocation to, Object data) {
		this(from,to);
		this.data = data;
	}
	public BaseEvent(EventLocation from, EventLocation to) {
		this(to);
		this.from = from;
	}
	public BaseEvent(EventLocation to) {
		this.to = to;
	}
	public BaseEvent(EventLocation to,int what) {
		this.to = to;
		this.what = what;
	}

	//===================getter and setter===========================

	public static long getSerialVersionUID() {
		return serialVersionUID;
	}

	public int getWhat() {
		return what;
	}

	public BaseEvent setWhat(int what) {
		this.what = what;
		return this;
	}

	public EventLocation getFrom() {
		return from;
	}

	public BaseEvent setFrom(EventLocation from) {
		this.from = from;
		return this;
	}

	public EventLocation getTo() {
		return to;
	}

	public BaseEvent setTo(EventLocation to) {
		this.to = to;
		return this;
	}

	public Object getData() {
		return data;
	}

	public BaseEvent setData(Object data) {
		this.data = data;
		return this;
	}

	public Bundle getBundle() {
		return bundle;
	}

	public BaseEvent setBundle(Bundle bundle) {
		this.bundle = bundle;
		return this;
	}

	public EventThread getRunOnThread() {
		return runOnThread;
	}

	public BaseEvent setRunOnThread(EventThread runOnThread) {
		this.runOnThread = runOnThread;
		return this;
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

	//======================rxJava scheduler====================
	/*Scheduler scheduler = null;

	public Scheduler getScheduler() {
		return scheduler;
	}

	public BaseEvent setScheduler(Scheduler scheduler) {
		this.scheduler = scheduler;
		return this;
	}*/
}
