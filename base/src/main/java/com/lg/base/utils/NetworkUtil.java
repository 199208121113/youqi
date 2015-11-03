package com.lg.base.utils;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class NetworkUtil {
	private static final String TAG = NetworkUtil.class.getSimpleName();

	private static ConnectivityManager cm = null;
	public static ConnectivityManager getCM(Context ctx){
		if(cm == null){
			cm = (ConnectivityManager)ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
		}
		return cm;
	}

	/** 检查网络是否畅通 */
	public static boolean isAvailable(Context ctx) {
		if(ctx == null)
			return false;
		NetworkInfo info = getCM(ctx).getActiveNetworkInfo();
		if (info == null)
			return false;
		return info.isConnected();
	}

	/** 判断当前网络是否为wifi */
	public static boolean isWifi(Context ctx) {
		if(ctx == null)
			return false;
        ConnectivityManager connectivityManager = (ConnectivityManager)ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = connectivityManager.getActiveNetworkInfo();
		if (info == null) {
			return false;
		}
		return info.isConnected() && info.getType() == ConnectivityManager.TYPE_WIFI;
	}
	
	/** 判断当前网络是否为GPRS */
	public static boolean isGPRS(Context ctx) {
		if(ctx == null)
			return false;
        ConnectivityManager connectivityManager = (ConnectivityManager)ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = connectivityManager.getActiveNetworkInfo();
		if (info == null) {
			return false;
		}
		return info.isConnected() && info.getType() == ConnectivityManager.TYPE_MOBILE;
	}

	/** 通过蓝牙获取MAC地址 */
	public static String getLocalMacAddressFromBluetooth() {
		String macAddress = null;
		try {
			BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
			if (bluetoothAdapter != null)
				macAddress = bluetoothAdapter.getAddress();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return macAddress;
	}

	/** 通过WIFI获取MAC地址 */
	public static String getLocalMacAddressFromWifi(Context context) {
		String macAddress = null;
		try {
			WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
			WifiInfo info = wifi.getConnectionInfo();
			if (info != null)
				macAddress = info.getMacAddress();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return macAddress;
	}

	/** 获取IP地址 */
	public static String getLocalIpAddress(Context context) {
		String ipAddress = null;
        try {
            WifiManager wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
            if (!wifiManager.isWifiEnabled()) {
                wifiManager.setWifiEnabled(true);
            }
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            int intAddress = wifiInfo.getIpAddress();
            ipAddress = intToIp(intAddress);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(ipAddress != null && ipAddress.trim().length() > 0){
            return ipAddress;
        }

        List<String> ipList = new ArrayList<String>();
		try {
			Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
			while (en.hasMoreElements()) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					String ip = inetAddress.getHostAddress().toString();
					if (!inetAddress.isLoopbackAddress()) {
                        ipList.add(ip);
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
        if(ipList.size() == 0)
            return null;
        if(ipList.size() == 1){
            ipAddress = ipList.get(0);
        }else if(ipList.size() > 1){
            int size = ipList.size();
            boolean has192=false,has10=false;
            for (int i = 0;i<size;i++){
                String ip = ipList.get(i);
                if(ip.startsWith("192.")){
                    has192=true;
                }else if(ip.startsWith("10.")){
                    has10=true;
                }
            }
            for (int i = 0;i<size;i++){
                String ip = ipList.get(i);
                if(has192){
                    if(ip.startsWith("192.")){
                        ipAddress = ip;
                        break;
                    }
                }else if(has10){
                    if(ip.startsWith("10.")){
                        ipAddress = ip;
                        break;
                    }
                }
            }
        }
        if(ipAddress == null && ipList.size() > 0){
            ipAddress = ipList.get(0);
        }
		return ipAddress;
	}

    private static String intToIp(int ipAddress) {
        String ip = String.format("%d.%d.%d.%d",(ipAddress & 0xff),(ipAddress >> 8 & 0xff),(ipAddress >> 16 & 0xff),(ipAddress >> 24 & 0xff));
        return ip;
    }

	public static String getNetworkType(Context ctx) {
		String strNetworkType = "";

		NetworkInfo networkInfo = getCM(ctx).getActiveNetworkInfo();
		if (networkInfo == null || !networkInfo.isConnected()) {
			return strNetworkType;
		}
		if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
			strNetworkType = "WIFI";
		} else if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
			String _strSubTypeName = networkInfo.getSubtypeName();
			// TD-SCDMA   networkType is 17
			int networkType = networkInfo.getSubtype();
			switch (networkType) {
				case TelephonyManager.NETWORK_TYPE_GPRS:
				case TelephonyManager.NETWORK_TYPE_EDGE:
				case TelephonyManager.NETWORK_TYPE_CDMA:
				case TelephonyManager.NETWORK_TYPE_1xRTT:
				case TelephonyManager.NETWORK_TYPE_IDEN: //api<8 : replace by 11
					strNetworkType = "2G";
					break;
				case TelephonyManager.NETWORK_TYPE_UMTS:
				case TelephonyManager.NETWORK_TYPE_EVDO_0:
				case TelephonyManager.NETWORK_TYPE_EVDO_A:
				case TelephonyManager.NETWORK_TYPE_HSDPA:
				case TelephonyManager.NETWORK_TYPE_HSUPA:
				case TelephonyManager.NETWORK_TYPE_HSPA:
				case TelephonyManager.NETWORK_TYPE_EVDO_B: //api<9 : replace by 14
				case TelephonyManager.NETWORK_TYPE_EHRPD:  //api<11 : replace by 12
				case TelephonyManager.NETWORK_TYPE_HSPAP:  //api<13 : replace by 15
					strNetworkType = "3G";
					break;
				case TelephonyManager.NETWORK_TYPE_LTE:    //api<11 : replace by 13
					strNetworkType = "4G";
					break;
				default:
					// http://baike.baidu.com/item/TD-SCDMA 中国移动 联通 电信 三种3G制式
					if (_strSubTypeName.equalsIgnoreCase("TD-SCDMA") || _strSubTypeName.equalsIgnoreCase("WCDMA") || _strSubTypeName.equalsIgnoreCase("CDMA2000")) {
						strNetworkType = "3G";
					} else {
						strNetworkType = _strSubTypeName;
					}
					break;
			}
		}
		return strNetworkType;
	}
}
