package com.lg.base.receiver;

import android.content.Context;
import android.content.Intent;

import com.lg.base.core.BaseEvent;
import com.lg.base.core.BaseReceiver;
import com.lg.base.core.Location;
import com.lg.base.core.LogUtil;
import com.lg.base.event.PackageEvent;
import com.lg.base.event.SdCardEvent;
import com.lg.base.event.SdCardEvent.SdCardState;


public class EnviromentReceiver extends BaseReceiver {

	private final String tag = this.getClass().getSimpleName();

	@Override
	protected void doReceive(Context context, Intent intent) {
//		Bundle bundle = intent.getExtras();
		String action = intent.getAction();
		BaseEvent baseEvent = null;
		// sd卡状态
		if (action.equals(Intent.ACTION_MEDIA_MOUNTED)) {
			SdCardEvent e = new SdCardEvent(getLocation(),Location.any,SdCardState.mounted);
			baseEvent = e;
		} else if (action.equals(Intent.ACTION_MEDIA_EJECT) || action.equals(Intent.ACTION_MEDIA_REMOVED)) {
			SdCardEvent e = new SdCardEvent(getLocation(),Location.any,SdCardState.unmounted);
			baseEvent = e;
		}
		// 软件安装卸载状态
		else if (Intent.ACTION_PACKAGE_ADDED.equals(action)) {
			int n = intent.getDataString().indexOf(":")+1;
			String packageName = intent.getDataString().substring(n).trim();
			LogUtil.d(tag ,"有应用被添加 :" + packageName);
			PackageEvent e = new PackageEvent(getLocation(),Location.any,packageName,PackageEvent.State.added);
			baseEvent = e;
		} else if (Intent.ACTION_PACKAGE_REMOVED.equals(action)) {
			int n = intent.getDataString().indexOf(":")+1;
			String packageName = intent.getDataString().substring(n).trim();
			LogUtil.d(tag ,"有应用被删除 : " + packageName);
			PackageEvent e = new PackageEvent(getLocation(),Location.any,packageName,PackageEvent.State.removed);
			baseEvent = e;
		} else if (Intent.ACTION_PACKAGE_REPLACED.equals(action)) {
			int n = intent.getDataString().indexOf(":")+1;
			String packageName = intent.getDataString().substring(n).trim();
			LogUtil.d(tag ,"有应用被替换 :" + packageName);
			PackageEvent e = new PackageEvent(getLocation(),Location.any,packageName,PackageEvent.State.replaced);
			baseEvent = e;
		}
		if(baseEvent != null){
			sendEvent(baseEvent);
		}
	}
}
