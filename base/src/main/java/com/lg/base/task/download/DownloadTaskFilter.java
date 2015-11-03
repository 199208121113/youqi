package com.lg.base.task.download;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.lg.base.R;
import com.lg.base.utils.APKUtil;

import java.io.File;
import java.text.DecimalFormat;
import java.util.Random;

public class DownloadTaskFilter implements TaskFilter<String, ProgressInfo> {
	protected TaskInfo taskInfo;
	protected Context context = null;
	public DownloadTaskFilter(Context ctx, TaskInfo taskInfo) {
		super();
		this.context = ctx;
		this.taskInfo = taskInfo;
	}

	protected int notificationId = 0xAAAA;
	protected NotificationManager nm = null;
	private Notification notification = null;
	private RemoteViews remoteView = null;

	@Override
	public void onPreExecute() {
		Context ctx = context;
		String notificationTitle = taskInfo.getName();
		nm = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
		notification = new Notification();
		notification.icon= R.drawable.ic_notification;
		notification.tickerText=notificationTitle;
		notification.defaults = Notification.DEFAULT_ALL;
		notification.flags = Notification.FLAG_AUTO_CANCEL;
		// 设置任务栏中下载进程显示的views
		remoteView = new RemoteViews(ctx.getPackageName(), R.layout.item_download);
		remoteView.setTextViewText(R.id.down_app_name, notificationTitle);
		notification.contentView = remoteView;

		PendingIntent contentIntent = PendingIntent.getActivity(ctx, 0, new Intent(), 0);
		notification.contentIntent = contentIntent;
		notificationId = new Random().nextInt();
		// 将下载任务添加到任务栏中
		nm.notify(notificationId, notification);
		notification.defaults = Notification.DEFAULT_LIGHTS;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onSuccess(String result) {
		File f= new File(result);
		if (f.exists() == false) {
			return;
		}
		//如果使用自定义视图则不需要使用setLatestEventInfo
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0, APKUtil.getInstallIntent(f), 0);
		notification.setLatestEventInfo(context, taskInfo.getName(),"下载完成,点击安装", contentIntent);
		notification.iconLevel=R.drawable.ic_notification;
		nm.notify(notificationId, notification);
		APKUtil.installApk(f, context);
	}

	@Override
	public void onException(Exception e) {
		notification.contentView.setTextViewText(R.id.down_app_percent, "下载失败,请重试");
		nm.notify(notificationId, notification);
	}

	private static final DecimalFormat df = new DecimalFormat("#.##");
	
	
	@Override
	public void onRunning(ProgressInfo prgress) {
		boolean indeterminate = prgress.getTotal() <= 0;
		remoteView.setTextViewText(R.id.down_app_name, taskInfo.getName());
		int cur = (int)(prgress.getCur() * 1.0 /prgress.getTotal() * 100);
		remoteView.setProgressBar(R.id.down_app_progress, 100, cur, indeterminate);
		final double receivedMB =  prgress.getCur()*1.0 / (1024*1024);
		final double totalMB = prgress.getTotal()*1.0 / (1024*1024);
		String text = df.format(receivedMB)+"M/"+df.format(totalMB)+"M";
		remoteView.setTextViewText(R.id.down_app_percent,text);
//		//加上实时速度
//		double speed = calcSpeed(prgress);
//		String speedText = df.format(speed)+"kb/s";
//		remoteView.setTextViewText(R.id.down_app_speed,speedText);
		notification.contentView = remoteView;
		nm.notify(notificationId, notification);
	}
	
	
	/*long lastReceivedTime = -1;
	long lastReceivedByte = -1;
	*//**
	 * 计算实时下载速度
	 * @param progress 计算下载速度
	 * @return
	 *//*
	private double calcSpeed(ProgressInfo progress){
		double speed = 0;
		if(lastReceivedTime == -1){
			speed = progress.getCur();
		}else{
			speed = (1000d / (System.currentTimeMillis()-lastReceivedTime))*((progress.getCur()-lastReceivedByte)*1.0/1024);
		}
		lastReceivedTime = System.currentTimeMillis();
		lastReceivedByte = progress.getCur();
		return speed; 
	}*/

}
