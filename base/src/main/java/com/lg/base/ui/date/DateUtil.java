package com.lg.base.ui.date;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * 月份： 1-12月，返回0-11 星期：周日-周六 ，返回1-7
 * 
 * */
public class DateUtil {

	private static final HashMap<String, String> WeekMap = new HashMap<String, String>(7);
	private static final HashMap<String, String> MonthMap = new HashMap<String, String>(7);
	static {
		WeekMap.put("1", "周日");
		WeekMap.put("2", "周一");
		WeekMap.put("3", "周二");
		WeekMap.put("4", "周三");
		WeekMap.put("5", "周四");
		WeekMap.put("6", "周五");
		WeekMap.put("7", "周六");
		
		MonthMap.put("0", "1月");
		MonthMap.put("1", "2月");
		MonthMap.put("2", "3月");
		MonthMap.put("3", "4月");
		MonthMap.put("4", "5月");
		MonthMap.put("5", "6月");
		MonthMap.put("6", "7月");
		MonthMap.put("7", "8月");
		MonthMap.put("8", "9月");
		MonthMap.put("9", "10月");
		MonthMap.put("10", "11月");
		MonthMap.put("11", "12月");
	}

	/** 获取当前这个月的天数 */
	public static List<CellItem> getCurrentDaysOfMonth(Calendar calendar){
		Calendar cal = (Calendar) calendar.clone();
		int days = cal.getActualMaximum(Calendar.DATE);
		cal.set(Calendar.DAY_OF_MONTH, 1);
		// 取到上月的最后一天
		cal.add(Calendar.DAY_OF_MONTH, -1);
		List<CellItem> itemList = new ArrayList<CellItem>();
		// 获取这个月总共有多少天
		for (int i =1; i <= days; i++) {
			cal.add(Calendar.DAY_OF_MONTH, 1);
			int day = cal.get(Calendar.DAY_OF_MONTH);
			CellItem ci = new CellItem(day,cal.getTimeInMillis());
			itemList.add(ci);
		}
		return itemList;
	}
	
	/** 获取上一个月的最后几天 */
	public static List<CellItem> getBeforeDaysOfMonth(Calendar calendar) {
		Calendar cal = (Calendar) calendar.clone();
		List<CellItem> itemList = new ArrayList<CellItem>();
		// 设置为当月的第一天
		cal.set(Calendar.DAY_OF_MONTH, 1);
		int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1;
		if (dayOfWeek == 0) { // 说明是星期日
			return itemList;
		}
		final int count = dayOfWeek;
		for (int i = 0; i < count; i++) {
			cal.add(Calendar.DAY_OF_MONTH, -1);
			int day = cal.get(Calendar.DAY_OF_MONTH);
			CellItem ci = new CellItem(day,cal.getTimeInMillis());
			itemList.add(ci);
		}
		Collections.reverse(itemList);
		return itemList;
	}

	/** 获取下一个月的最后几天 */
	public static List<CellItem> getAfterDaysOfMonth(Calendar calendar) {
		Calendar cal = (Calendar) calendar.clone();
		List<CellItem> itemList = new ArrayList<CellItem>();
		// 在当前的月上加了一个月
		cal.add(Calendar.MONTH, 1);
		// 设置为当月的第一天
		cal.set(Calendar.DAY_OF_MONTH, 1);
		// 取到上月的最后一天
		cal.add(Calendar.DAY_OF_MONTH, -1);
		int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1;
		if (dayOfWeek == 6) { // 说明是星期六
			return itemList;
		}
		final int count = 6 - dayOfWeek;
		for (int i = 0; i < count; i++) {
			cal.add(Calendar.DAY_OF_MONTH, 1);
			int day = cal.get(Calendar.DAY_OF_MONTH);
			CellItem ci = new CellItem(day,cal.getTimeInMillis());
			itemList.add(ci);
		}
		return itemList;
	}

	/** 获取下一个月的最后几天 ,但要根据上个月显示了多少天来定 */
	public static List<CellItem> getAfterDaysOfMonth2(Calendar calendar, int beforeCount) {
		// 固定 = 行：6，列：7列 ，count=42

		Calendar cal = (Calendar) calendar.clone();
		final int totalCount = 42;

		// 获取这个月总共有多少天
		int days = cal.getActualMaximum(Calendar.DATE);

		List<CellItem> itemList = new ArrayList<CellItem>();
		// 在当前的月上加了一个月
		cal.add(Calendar.MONTH, 1);
		// 设置为当月的第一天
		cal.set(Calendar.DAY_OF_MONTH, 1);
		// 取到上月的最后一天
		cal.add(Calendar.DAY_OF_MONTH, -1);

		final int count = totalCount - days - beforeCount;
		if (count <= 0) { // 说明是星期六
			return itemList;
		}
		for (int i = 0; i < count; i++) {
			cal.add(Calendar.DAY_OF_MONTH, 1);
			int day = cal.get(Calendar.DAY_OF_MONTH);
			CellItem ci = new CellItem(day,cal.getTimeInMillis());
			itemList.add(ci);
		}
		return itemList;
	}

	public static final String getDateStr(Calendar cal) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String s = sdf.format(cal.getTime());
		int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
		s += " " + WeekMap.get(String.valueOf(dayOfWeek));
		return s;
	}
	
	public static final String getDateStr(long millons) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Date d = new Date(millons);
		String s = sdf.format(d);
		return s;
	}
	
	public static String getDayOfWeek(int day){
		return WeekMap.get(String.valueOf(day));
	}
	public static String getMonthOfYear(int month){
		return MonthMap.get(String.valueOf(month));
	}
	

}
