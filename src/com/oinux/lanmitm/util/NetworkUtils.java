package com.oinux.lanmitm.util;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import com.oinux.lanmitm.AppContext;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkUtils {

	private static Map<String, String> mVendors = null;

	/**
	 * 计算子网主机数目
	 * 
	 * @param int_net_mask
	 * @return
	 */
	public static int countHost(int int_net_mask) {
		int n = ~int_net_mask;
		return ((n >> 24) & 0xff) + (((n >> 16) & 0xff) << 8)
				+ (((n >> 8) & 0xff) << 16) + ((n & 0xff) << 24);
	}

	/**
	 * 根据Int类型IP返回String类型IP
	 * 
	 * @param int_ip
	 * @return
	 */
	public static String netfromInt(int int_ip) {
		return new StringBuilder().append((int_ip) & 0xff).append('.')
				.append((int_ip >> 8) & 0xff).append('.')
				.append((int_ip >> 16) & 0xff).append('.')
				.append((int_ip >> 24) & 0xff).toString();
	}

	/**
	 * 
	 * 查找下一个IP
	 * 
	 * @param int_ip
	 * @param step
	 * @return
	 */
	public static int nextIntIp(int int_ip) {
		int next_ip = -1;
		byte[] ip_byte = intIpToByte(int_ip);
		int i = ip_byte.length - 1;

		while (i >= 0 && ip_byte[i] == (byte) 0xff) {
			ip_byte[i] = 0;
			i--;
		}
		if (i >= 0)
			ip_byte[i]++;
		else
			return next_ip;

		next_ip = byteIpToInt(ip_byte);

		return next_ip;
	}

	/**
	 * Int类型IP转成byte数组
	 * 
	 * @param int_ip
	 * @return
	 */
	public static byte[] intIpToByte(int int_ip) {
		byte[] ip_byte = new byte[4];

		ip_byte[0] = (byte) (int_ip & 0xff);
		ip_byte[1] = (byte) (0xff & int_ip >> 8);
		ip_byte[2] = (byte) (0xff & int_ip >> 16);
		ip_byte[3] = (byte) (0xff & int_ip >> 24);

		return ip_byte;
	}

	/**
	 * byte数组Ip转成Int类型Ip
	 * 
	 * @param ip_byte
	 * @return
	 */
	public static int byteIpToInt(byte[] ip_byte) {
		return ((ip_byte[3] & 0xff) << 24) + ((ip_byte[2] & 0xff) << 16)
				+ ((ip_byte[1] & 0xff) << 8) + (ip_byte[0] & 0xff);
	}

	/**
	 * String mac转byte[]
	 * 
	 * @param mac_string
	 * @return
	 */
	public static byte[] stringMacToByte(String mac_string) {
		byte[] mac_byte = new byte[6];
		if (mac_string == null)
			return mac_byte;
		String[] mac_strs = mac_string.split(":");
		if (mac_strs.length != 6) {
			mac_strs = mac_string.split("-");
		}
		for (int i = 0; i < 6; i++) {
			mac_byte[i] = Integer.valueOf(mac_strs[i], 16).byteValue();
		}
		return mac_byte;
	}

	public static String vendorFromMac(byte[] mac) {
		if (mVendors == null) {
			try {
				mVendors = new HashMap<String, String>();
				InputStream is = AppContext.getContext().getAssets()
						.open("nmap-mac-prefixes");
				DataInputStream in = new DataInputStream(is);
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(in));
				String line;
				while ((line = reader.readLine()) != null) {
					line = line.trim();
					if (!line.startsWith("#") && !line.isEmpty()) {
						String[] tokens = line.split(" ", 2);
						if (tokens.length == 2)
							mVendors.put(tokens[0], tokens[1]);
					}
				}
				in.close();
				is.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (mac != null && mac.length >= 3)
			return mVendors.get(String.format("%02X%02X%02X", mac[0], mac[1],
					mac[2]));
		else
			return null;
	}

	public static boolean isWifiConnected() {
		ConnectivityManager manager = (ConnectivityManager) AppContext
				.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = manager
				.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		return info != null && info.isConnected() && info.isAvailable();
	}
	
}
