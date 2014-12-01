package com.oinux.lanmitm.service;

import com.oinux.lanmitm.R;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

public class BaseService extends Service {

	public static final int HTTP_SERVER_NOTICE = 1;
	public static final int HIJACK_NOTICE = 2;
	public static final int SNIFFER_NOTICE = 3;
	public static final int INJECT_NOTICE = 4;
	public static final int ARPSPOOF_NOTICE = 0;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@SuppressWarnings("deprecation")
	protected void notice(String tickerText, int id, Class<?> cls) {
		NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		Notification n = new Notification(R.drawable.ic_launch_notice,
				getString(R.string.app_name), System.currentTimeMillis());
		n.flags = Notification.FLAG_NO_CLEAR;

		Intent intent = new Intent(this, cls);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
				| Intent.FLAG_ACTIVITY_NEW_TASK);
		PendingIntent contentIntent = PendingIntent.getActivity(this,
				R.string.app_name, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		n.setLatestEventInfo(this, this.getString(R.string.app_name),
				tickerText, contentIntent);
		nm.notify(id, n);
	}
}
