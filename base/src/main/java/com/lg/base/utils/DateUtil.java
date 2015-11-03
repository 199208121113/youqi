package com.lg.base.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateUtil {
	private static final String PATTERN_DEFAULT = "yyyy-MM-dd HH:mm";
	public static final String PATTERN_yyyy_MM_dd_hh_mm_ss = "yyyy-MM-dd HH:mm:ss";
	private static SimpleDateFormat DATE_FORMAT_DEFAULT = null;
	
	/**
	 * pattern yyyy-MM-dd HH:mm
	 * @param millons
	 */
	public static String formatDate(long millons){
		return getDateForamt().format(new Date(millons));
	}
	
	public static String formatDate(long millons,String pattern){
		return new SimpleDateFormat(pattern,Locale.CHINA).format(new Date(millons));
	}
	
	private static SimpleDateFormat getDateForamt(){
		if(DATE_FORMAT_DEFAULT == null)
			DATE_FORMAT_DEFAULT = new SimpleDateFormat(PATTERN_DEFAULT,Locale.CHINA);
		return DATE_FORMAT_DEFAULT;
	}
	
	public static long getMillonsByDateStr(String dateStr,String pattern){
		long mill = 0;
		try {
			SimpleDateFormat sdf = new SimpleDateFormat(pattern,Locale.CHINA);
			mill = sdf.parse(dateStr).getTime();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return mill;
	}
	
	public static long getMillonsByDateStr(String dateStr){
		return getMillonsByDateStr(dateStr,"yyyy-MM-dd");
	}

}
