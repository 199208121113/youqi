package com.lg.base.utils;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ExceptionUtil {
	public static String getStackTrace(Throwable e) {
		String errMsg = null;
		try {
			StringWriter writer = new StringWriter();
			e.printStackTrace(new PrintWriter(writer, true));
			errMsg = writer.toString();
		} catch (Throwable e1) {
			e1.printStackTrace();
			errMsg = e1.getMessage();
		}
		return errMsg;
	}
	public static String getErrorMessage(Throwable e) {
		String errMsg = e.getMessage();
		if(StringUtil.isNotEmpty(errMsg)){
			return errMsg;
		}
		return getStackTrace(e);
	}
}
