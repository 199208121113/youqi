package com.lg.base.task.download;

public class ProgressInfo {
	private long total = 0;
	private long cur = 0;

	public ProgressInfo(long total, long cur) {
		super();
		this.total = total;
		this.cur = cur;
	}

	public long getTotal() {
		return total;
	}

	public long getCur() {
		return cur;
	}
}
