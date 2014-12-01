package com.oinux.lanmitm.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.content.Intent;

import com.oinux.lanmitm.AppContext;
import com.oinux.lanmitm.ui.HttpActivity;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.SimpleWebServer;

public class HttpService extends BaseService {

	public static final String SERVER_LOG_CHANGE_INTENT = "server_log_change_intent";
	public static final String TAG = "httpserver";
	public static final int PORT = 10000;
	private static final String webRootPath = AppContext.getStoragePath()
			+ "/lanmitm/www";
	private NanoHTTPD httpd;

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (httpd != null && httpd.isAlive()) {
			httpd.stop();
		}
		File webRoot = new File(webRootPath);
		if (!webRoot.exists()) {
			webRoot.mkdirs();
			copyIndexHtml();
		}
		httpd = new SimpleWebServer(this, AppContext.getIp(), PORT, webRoot);
		try {
			httpd.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
		notice("httpserver后台运行中，点击管理",
				HTTP_SERVER_NOTICE, HttpActivity.class);
		AppContext.isHttpserverRunning = true;
		return super.onStartCommand(intent, flags, startId);
	}

	private void copyIndexHtml() {
		InputStream is = null;
		FileOutputStream fos = null;
		byte[] buffer = new byte[1024];
		int len = -1;
		try {
			fos = new FileOutputStream(webRootPath + "/index.html");
			is = this.getAssets().open("index.html");
			while ((len = is.read(buffer)) != -1) {
				fos.write(buffer, 0, len);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (fos != null)
				try {
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			if (is != null)
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
	}

	@Override
	public void onDestroy() {
		if (httpd != null && httpd.isAlive()) {
			httpd.stop();
		}
		AppContext.isHttpserverRunning = false;
		super.onDestroy();
	}
}
