package com.oinux.lanmitm.service;

import com.oinux.lanmitm.AppContext;
import com.oinux.lanmitm.proxy.HttpProxy;
import com.oinux.lanmitm.ui.InjectActivity;
import com.oinux.lanmitm.util.ShellUtils;

import android.content.Intent;

public class InjectService extends BaseService {

	private static final String[] PORT_REDIRECT_CMD = {
			"iptables -t nat -F",
			"iptables -F",
			"iptables -t nat -I POSTROUTING -s 0/0 -j MASQUERADE",
			"iptables -P FORWARD ACCEPT",
			"iptables -t nat -A PREROUTING -j DNAT -p tcp --dport 80 --to "
					+ AppContext.getIp() + ":" + HttpProxy.HTTP_PROXY_PORT };

	private static final String[] UN_PORT_REDIRECT_CMD = {
			"iptables -t nat -F",
			"iptables -F",
			"iptables -t nat -I POSTROUTING -s 0/0 -j MASQUERADE",
			"iptables -t nat -D PREROUTING -j DNAT -p tcp --dport 80 --to "
					+ AppContext.getIp() + ":" + HttpProxy.HTTP_PROXY_PORT };

	private HttpProxy mHttpProxy;
	private String inject = "ouyangwenguang";

	public static final String DATASET_CHANGED = "HIJACK_DATASET_CHANGED";
	public static final String DATASET_COOKIES_CHANGED = "HIJACK_COOKIES_CHANGED";

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		if (!ShellUtils.checkRootPermission())
			return super.onStartCommand(intent, flags, startId);

		startInject();

		return super.onStartCommand(intent, flags, startId);
	}

	private void startInject() {
		HttpProxy.stop = false;

		new Thread() {
			@Override
			public void run() {
				ShellUtils.execCommand(PORT_REDIRECT_CMD, true, true);
			}
		}.start();

		mHttpProxy = HttpProxy.getInstance();
		mHttpProxy.setInject(inject); 
		mHttpProxy.start();

		Intent intent = new Intent(this, ArpService.class);
		intent.putExtra("arp_cheat_way", ArpService.ONE_WAY_HOST);
		startService(intent);

		notice("代码注入后台运行中，点击管理", INJECT_NOTICE, InjectActivity.class);

		AppContext.isInjectRunning = true;
	}

	private void stopHijack() {
		stopService(new Intent(this, ArpService.class));

		new Thread() {
			@Override
			public void run() {
				ShellUtils.execCommand(UN_PORT_REDIRECT_CMD, true, true);
			}
		}.start();

		if (!AppContext.isHijackRunning) {
			HttpProxy.stop = true;
			if (mHttpProxy != null) {
				mHttpProxy.interrupt();
				mHttpProxy = null;
			}
		}
		AppContext.isInjectRunning = false;
	}

	@Override
	public void onDestroy() {
		stopHijack();
		super.onDestroy();
	}
}
