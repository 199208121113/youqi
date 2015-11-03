package com.lg.base.utils;

import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;

/**
 * 手机功能 
 * 
 * @author liguo
 */

public class PhoneUtil {
	public static String getModel() {
		return Build.MODEL;
	}

	public static String getIMEI(Context ctx) {
        TelephonyManager telephonyManager = (TelephonyManager)ctx.getSystemService(Context.TELEPHONY_SERVICE);
		return telephonyManager.getDeviceId();
	}

	public static String getLine1Number(Context ctx) {
        TelephonyManager telephonyManager = (TelephonyManager)ctx.getSystemService(Context.TELEPHONY_SERVICE);
		return telephonyManager.getLine1Number();
	}

	public static String getSimSerialNumber(Context ctx) {
        TelephonyManager telephonyManager = (TelephonyManager)ctx.getSystemService(Context.TELEPHONY_SERVICE);
		return telephonyManager.getSimSerialNumber();
	}

	public static String getSubscriberId(Context ctx) {
        TelephonyManager telephonyManager = (TelephonyManager)ctx.getSystemService(Context.TELEPHONY_SERVICE);
		return telephonyManager.getSubscriberId();
	}

	public static String getDeviceSoftwareVersion(Context ctx) {
        TelephonyManager telephonyManager = (TelephonyManager)ctx.getSystemService(Context.TELEPHONY_SERVICE);
		return telephonyManager.getDeviceSoftwareVersion();
	}

	public static String getReleaseVersion() {
		return Build.VERSION.RELEASE;
	}

	public static final String getDeviceId(Context ctx) {
		String mac = null;

		if(NetworkUtil.isAvailable(ctx)) {
			mac = NetworkUtil.getLocalMacAddressFromWifi(ctx);
		}

		if (mac == null) {
			mac = NetworkUtil.getLocalMacAddressFromBluetooth();
		}

		if(mac == null){
			try {
				TelephonyManager tm = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
				mac = tm.getDeviceId();
			} catch (Throwable e) {
				//ignore
			}
		}

		if(mac == null){
			try {
				mac = Settings.Secure.getString(ctx.getContentResolver(), Settings.Secure.ANDROID_ID);
			}catch (Throwable e) {
				//ignore
			}
		}

		if (mac == null) {
			try {
				mac = PhoneUtil.getIMEI(ctx) + PhoneUtil.getModel() + PhoneUtil.getSimSerialNumber(ctx) + PhoneUtil.getReleaseVersion();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if (mac == null) {
			mac = MD5Util.toMd5(PhoneUtil.getModel() + PhoneUtil.getReleaseVersion());
		}

		return mac;
	}

}
