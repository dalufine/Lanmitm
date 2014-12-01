package com.oinux.lanmitm.receiver;

import com.oinux.lanmitm.AppContext;
import com.oinux.lanmitm.util.NetworkUtils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.util.Log;

public class NetworkReceiver extends BroadcastReceiver {

	private static final String TAG = "NetworkReceiver";
	private boolean disconnect = true;

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		Log.v(TAG, "网络状态改变>>");
		if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)
				&& NetworkUtils.isWifiConnected() && disconnect) {
			disconnect = false;
			AppContext.initWifiInfo();
			Log.v(TAG, "连接WiFi");
		} else if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)
				&& !NetworkUtils.isWifiConnected()) {
			disconnect = true;
			Log.v(TAG, "断开WiFi");
		}
	}
}
