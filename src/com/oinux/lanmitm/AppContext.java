package com.oinux.lanmitm;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import com.oinux.lanmitm.entity.LanHost;
import com.oinux.lanmitm.entity.Session;
import com.oinux.lanmitm.util.NetworkUtils;
import com.oinux.lanmitm.util.ShellUtils;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.Environment;

public class AppContext extends Application {

	public static final String LICENSE = "<h5><font color='#009966'><b>Lanmitm</b> v0.9 alpha by oinux</h5><br/>";
	private static Context mContext;
	private static String[] TOOLS_FILENAME = { "arpspoof", "tcpdump" };
	private static String[] TOOLS_COMMAND = { "chmod 755 [ROOT_PATH]/arpspoof",
			"chmod 755 [ROOT_PATH]/tcpdump" };
	private static SharedPreferences preferences = null;

	private static InetAddress mInetAddress;
	private static int int_gateway;
	private static int int_ip;
	private static int int_net_mask;
	private static LanHost mTarget = null;
	private static String gatewayMac;
	public static boolean isHttpserverRunning = false;
	public static boolean isHijackRunning = false;
	public static boolean isTcpdumpRunning = false;
	public static boolean isInjectRunning = false;

	private static String mStoragePath = null;
	private static List<Session> mHijackList = null;
	private static Session mCurrentHijack = null;
	private static StringBuilder serverLog;

	@Override
	public void onCreate() {

		mContext = this;

		boolean isRooted = ShellUtils.checkRootPermission();

		mStoragePath = Environment.getExternalStorageDirectory().toString();

		preferences = getSharedPreferences("app", Context.MODE_PRIVATE);
		if (isRooted && !preferences.getBoolean("installed_tools", false))
			copyTools();

		if (!NetworkUtils.isWifiConnected())
			return;

		initWifiInfo();

		super.onCreate();
	}

	public static void initWifiInfo() {
		WifiManager wifiManager = (WifiManager) mContext
				.getSystemService(Context.WIFI_SERVICE);
		int_ip = wifiManager.getDhcpInfo().ipAddress;
		int_net_mask = wifiManager.getDhcpInfo().netmask;
		int_gateway = wifiManager.getDhcpInfo().gateway;
		try {
			mInetAddress = InetAddress.getByName(NetworkUtils
					.netfromInt(int_ip));
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}

		gatewayMac = wifiManager.getConnectionInfo().getBSSID()
				.replace('-', ':');
	}

	public static StringBuilder getServerLog() {
		if (serverLog == null)
			serverLog = new StringBuilder();
		return serverLog;
	}

	public static String getStoragePath() {
		return mStoragePath;
	}

	public static LanHost getTarget() {
		return mTarget;
	}

	public static void setTarget(LanHost target) {
		mTarget = target;
	}

	public static List<Session> getHijackList() {
		return mHijackList;
	}

	public static void setHijackList(List<Session> hijackList) {
		mHijackList = hijackList;
	}

	public static Session getCurrentHijack() {
		return mCurrentHijack;
	}

	public static void setCurrentHijack(Session currentHijack) {
		mCurrentHijack = currentHijack;
	}

	public static InetAddress getInetAddress() {
		return mInetAddress;
	}

	public static int getIntGateway() {
		return int_gateway;
	}

	public static String getGateway() {
		return NetworkUtils.netfromInt(int_gateway);
	}

	public static String getGatewayMac() {
		return gatewayMac;
	}

	public static int getIntIp() {
		return int_gateway;
	}

	public static String getIp() {
		return NetworkUtils.netfromInt(int_ip);
	}

	public static int getHostCount() {
		return NetworkUtils.countHost(int_net_mask);
	}

	public static int getIntNetMask() {
		return int_net_mask;
	}

	private void copyTools() {
		InputStream is = null;
		FileOutputStream fos = null;
		byte[] buffer = new byte[1024];
		int len = -1;

		try {
			for (String filename : TOOLS_FILENAME) {
				fos = new FileOutputStream(getFilesDir().getAbsolutePath()
						+ '/' + filename);
				is = this.getAssets().open(filename);
				while ((len = is.read(buffer)) != -1) {
					fos.write(buffer, 0, len);
				}
				fos.close();
				is.close();
			}

			for (String cmd : TOOLS_COMMAND) {
				ShellUtils.execCommand(cmd.replace("[ROOT_PATH]", getFilesDir()
						.getAbsolutePath()), true, true);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		preferences.edit().putBoolean("installed_tools", true).commit();
	}

	public static void putString(String key, String value) {
		preferences.edit().putString(key, value).commit();
	}

	public static String getString(String key, String defValue) {
		return preferences.getString(key, defValue);
	}

	public static void putInt(String key, int value) {
		preferences.edit().putInt(key, value).commit();
	}

	public static int getInt(String key, int defValue) {
		return preferences.getInt(key, defValue);
	}

	public static void putBoolean(String key, Boolean value) {
		preferences.edit().putBoolean(key, value).commit();
	}

	public static Boolean getBoolean(String key, Boolean value) {
		return preferences.getBoolean(key, false);
	}

	public static Context getContext() {
		return mContext;
	}
}
