package com.lg.base.utils;

import java.math.BigDecimal;
import java.text.DecimalFormat;

public class NumberUtil {
	public static double formatNumber(double d) {
		return formatNumber(d,2);
	}
	
	public static double formatNumber(double d,int digit) {
		BigDecimal bd = new BigDecimal(d);
		return bd.setScale(digit, BigDecimal.ROUND_HALF_UP).doubleValue();
	}

	public static String format1(double f){
		DecimalFormat df = new DecimalFormat("#.00");
		return df.format(f);
	}

	public static String format2(double f){
		return String.format("%.2f",f);
	}
}
