package com.lg.base.utils;

import android.text.TextUtils;

import java.util.Locale;

public class StringUtil {
	public static String getFileName(String path){
		if(path == null || path.trim().length() == 0)
			return "null";
		path = path.replaceAll("\\\\","/");
		final int pos = path.lastIndexOf("/");
		if(pos < 0){
			return path;
		}
		return path.substring(pos + 1);
	}
	
	public static String toLowerCase(String str){
		if(str == null)
			return "";
		return str.toLowerCase(Locale.getDefault());
	}

    public static String toUpperCase(String str){
        if(str == null)
            return "";
        return str.toUpperCase(Locale.getDefault());
    }

    public static boolean isEmpty(String s){
        return s == null || s.trim().length() == 0;
    }
    public static boolean isNotEmpty(String s){
        return s != null && s.trim().length() > 0;
    }

    /** 验证是否为合法的JSON格式，只是做了一个最基础的验证 */
    public static boolean isValidJsonStr(String s){
        if(s == null || s.trim().length() < 2){
            return false;
        }
        s = s.replaceAll("\\s", "");
        if(s.length() < 2){
            return false;
        }
        String firstStr = String.valueOf(s.charAt(0));
        String lastStr = String.valueOf(s.charAt(s.length()-1));
        if(firstStr.equals(lastStr)){
            return false;
        }
        String endStr = firstStr+lastStr;
        return endStr.equals("{}") || endStr.equals("[]");
    }

    public static String getBaseFileName(String path){
        String str = getFileName(path);
        if(TextUtils.isEmpty(str)){
            return "null";
        }
        int n = str.indexOf(".");
        if(n >= 0){
            str = str.substring(0,n);
        }
        return str;
    }
    public static String replaceTrim_R_N(String str){
        if(str == null)
            return "";
        return str.replaceAll("\\s","");
    }
	
}
