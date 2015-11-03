package com.lg.base.task.download;

import android.os.Bundle;

public class TaskInfo {
	private String id;
	private String name;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	private Bundle extraBundle;

	public Bundle getExtraBundle() {
		return extraBundle;
	}

	public void setExtraBundle(Bundle extraBundle) {
		this.extraBundle = extraBundle;
	}
}
