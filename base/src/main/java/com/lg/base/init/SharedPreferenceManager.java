package com.lg.base.init;

import android.content.Context;
import android.content.SharedPreferences;

import com.lg.base.utils.StringUtil;

/**
 * SharedPreferenceManager
 * Created by liguo on 2015/11/13.
 */
public class SharedPreferenceManager {
    private static SharedPreferences ref = null;

    public static void init(Context ctx) {
        ref = ctx.getSharedPreferences(getRefName(ctx), Context.MODE_PRIVATE);
    }

    public static String getRefName(Context ctx) {
        String pkgName = StringUtil.toLowerCase(ctx.getPackageName());
        return pkgName.replace(".", "_");
    }

    public static void putInt(String key, int value) {
        ref.edit().putInt(key, value).apply();
    }

    public static void putLong(String key, long value) {
        ref.edit().putLong(key, value).apply();
    }

    public static void putString(String key, String value) {
        ref.edit().putString(key, value).apply();
    }

    public static int getInt(String key) {
        return ref.getInt(key, 0);
    }

    public static int getInt(String key, int defaultValue) {
        return ref.getInt(key, defaultValue);
    }

    public static long getLong(String key) {
        return ref.getLong(key, 0);
    }

    public static long getLong(String key, long defaultValue) {
        return ref.getLong(key, defaultValue);
    }

    public static String getString(String key) {
        return ref.getString(key, "");
    }

    public static String getString(String key, String defaultValue) {
        return ref.getString(key, defaultValue);
    }
}
