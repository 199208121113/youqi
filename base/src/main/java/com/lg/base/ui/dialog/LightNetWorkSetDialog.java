package com.lg.base.ui.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.lg.base.utils.IntentUtil;

public class LightNetWorkSetDialog extends AlertDialog {

	public static enum Dialog_Status{
		showing,dismissed
	}
	private static Dialog_Status status = Dialog_Status.dismissed;
	private static AlertDialog lastAlertDialog = null;
	public static AlertDialog create(final Context context, String title, String message) {
		if(status == Dialog_Status.showing){
			if(lastAlertDialog!=null){
				lastAlertDialog.dismiss();
				status = Dialog_Status.dismissed;
			}
			lastAlertDialog = null;
		}
		status = Dialog_Status.showing;
		AlertDialog networkDialog = LightAlertDialog.create(context);
		networkDialog.setTitle(title);
		networkDialog.setMessage(message);
		networkDialog.setCancelable(false);
		networkDialog.setButton(DialogInterface.BUTTON_POSITIVE,"设置网络", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				status = Dialog_Status.dismissed;
				dialog.dismiss();
				context.startActivity(IntentUtil.createNetworkSettingIntent());
				
			}
		});
		networkDialog.setButton(DialogInterface.BUTTON_NEGATIVE,"取消", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				status = Dialog_Status.dismissed;
				dialog.dismiss();
			}
		});
		lastAlertDialog = networkDialog;
		return networkDialog;
	}
	
	public static AlertDialog create(final Context context, String title, String message,OnClickListener retryListener) {
		AlertDialog networkDialog = LightAlertDialog.create(context);
		networkDialog.setTitle(title);
		networkDialog.setMessage(message);
		networkDialog.setCancelable(false);
		networkDialog.setButton(DialogInterface.BUTTON_POSITIVE,"设置网络", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				context.startActivity(IntentUtil.createNetworkSettingIntent());
			}
		});
		networkDialog.setButton(DialogInterface.BUTTON_NEGATIVE,"重试",retryListener);
		networkDialog.setButton(DialogInterface.BUTTON_NEUTRAL,"取消", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		return networkDialog;
	}

	private LightNetWorkSetDialog(Context context, CharSequence message) {
		super(context, THEME_HOLO_LIGHT);
	}

	public static Dialog_Status getStatus() {
		return status;
	}
}
