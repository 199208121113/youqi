package com.lg.base.ui.date;


import java.io.Serializable;

public class CellItem implements Serializable{

	private static final long serialVersionUID = 1L;

	/*
	 * 月份： ,1-12月，返回0-11 星期：周日-周六 ，返回1-7
	 */

//	private static final String[] week = { "周日", "周一", "周二", "周三", "周四", "周五", "周六" };

	private long datetime;

	// 新历的天数
	private int day;

	// 旧历的几月初几
	private String desc;

	public int getDay() {
		return day;
	}

	public void setDay(int day) {
		this.day = day;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public CellItem(int day, long datetime) {
		super();
		this.day = day;
		this.datetime = datetime;
	}

	public long getDatetime() {
		return datetime;
	}

}
