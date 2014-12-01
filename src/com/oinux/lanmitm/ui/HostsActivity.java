package com.oinux.lanmitm.ui;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.oinux.lanmitm.ActionBarActivity;
import com.oinux.lanmitm.AppContext;
import com.oinux.lanmitm.HostAdapter;
import com.oinux.lanmitm.R;
import com.oinux.lanmitm.entity.LanHost;
import com.oinux.lanmitm.service.HijackService;
import com.oinux.lanmitm.service.InjectService;
import com.oinux.lanmitm.service.SnifferService;
import com.oinux.lanmitm.util.NetworkUtils;
import com.oinux.lanmitm.widget.RadioDialog;

/**
 * 
 * 
 * @author oinux
 *
 */
public class HostsActivity extends ActionBarActivity {

	private static final byte[] NETBIOS_REQUEST = { (byte) 0x82, (byte) 0x28,
			(byte) 0x0, (byte) 0x0, (byte) 0x0, (byte) 0x1, (byte) 0x0,
			(byte) 0x0, (byte) 0x0, (byte) 0x0, (byte) 0x0, (byte) 0x0,
			(byte) 0x20, (byte) 0x43, (byte) 0x4B, (byte) 0x41, (byte) 0x41,
			(byte) 0x41, (byte) 0x41, (byte) 0x41, (byte) 0x41, (byte) 0x41,
			(byte) 0x41, (byte) 0x41, (byte) 0x41, (byte) 0x41, (byte) 0x41,
			(byte) 0x41, (byte) 0x41, (byte) 0x41, (byte) 0x41, (byte) 0x41,
			(byte) 0x41, (byte) 0x41, (byte) 0x41, (byte) 0x41, (byte) 0x41,
			(byte) 0x41, (byte) 0x41, (byte) 0x41, (byte) 0x41, (byte) 0x41,
			(byte) 0x41, (byte) 0x41, (byte) 0x41, (byte) 0x0, (byte) 0x0,
			(byte) 0x21, (byte) 0x0, (byte) 0x1 };

	private static final short NETBIOS_UDP_PORT = 137;
	private static final Pattern ARP_TABLE_PARSER = Pattern
			.compile(
					"^([\\d]{1,3}\\.[\\d]{1,3}\\.[\\d]{1,3}\\.[\\d]{1,3})\\s+([0-9-a-fx]+)\\s+([0-9-a-fx]+)\\s+([a-f0-9]{2}:[a-f0-9]{2}:[a-f0-9]{2}:[a-f0-9]{2}:[a-f0-9]{2}:[a-f0-9]{2})\\s+([^\\s]+)\\s+(.+)$",
					Pattern.CASE_INSENSITIVE);
	private static final int DATASET_CHANGED = 1;
	private static final int DATASET_HOST_ALIAS_CHANGED = 2;

	public static boolean stop = true;

	private Thread arpReader;
	private Thread discoveryThread;
	private NetworkInterface networkInterface = null;

	private ListView hostListview;
	private HostAdapter hostAdapter;
	private List<LanHost> mHosts;
	private Handler mHandler;

	private TextView headerText;
	private ProgressBar actionProgress;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState, R.layout.hosts_activity);

		setBarTitle(Html.fromHtml("<b>主机列表</b>"));
		headerText = (TextView) findViewById(R.id.header_text);
		actionProgress = (ProgressBar) findViewById(R.id.header_progress);
		actionProgress.setVisibility(View.VISIBLE);

		mHosts = new ArrayList<LanHost>();
		hostListview = (ListView) findViewById(R.id.host_listview);
		hostAdapter = new HostAdapter(this, mHosts);
		hostListview.setAdapter(hostAdapter);

		if (!NetworkUtils.isWifiConnected())
			return;

		try {
			networkInterface = NetworkInterface.getByInetAddress(AppContext
					.getInetAddress());
		} catch (SocketException e) {
			e.printStackTrace();
		}

		hostListview.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				LanHost host = (LanHost) parent.getItemAtPosition(position);
				AppContext.setTarget(host);
				RadioDialog.Builder builder = new RadioDialog.Builder(
						HostsActivity.this);
				builder.setTitle("选择功能")
						.setRadio1("数据嗅探", false,
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										dialog.dismiss();
										if (AppContext.isTcpdumpRunning) {
											stopService(new Intent(
													HostsActivity.this,
													SnifferService.class));
										}
										Intent intent = new Intent(
												HostsActivity.this,
												SniffActivity.class);
										startActivity(intent);
										overridePendingTransition(
												R.anim.zoom_in,
												android.R.anim.fade_out);
									}
								})
						.setRadio2("会话劫持", false,
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										dialog.dismiss();
										if (AppContext.isHijackRunning) {
											stopService(new Intent(
													HostsActivity.this,
													HijackService.class));
											if (AppContext.getHijackList() != null)
												AppContext.getHijackList()
														.clear();
										}
										Intent intent = new Intent(
												HostsActivity.this,
												HijackActivity.class);
										startActivity(intent);
										overridePendingTransition(
												R.anim.zoom_in,
												android.R.anim.fade_out);
									}
								})
//						.setRadio3("代码注入", false,
//								new DialogInterface.OnClickListener() {
//									@Override
//									public void onClick(DialogInterface dialog,
//											int which) {
//										dialog.dismiss();
//										if (AppContext.isInjectRunning) {
//											stopService(new Intent(
//													HostsActivity.this,
//													InjectService.class));
//										}
//										Intent intent = new Intent(
//												HostsActivity.this,
//												InjectActivity.class);
//										startActivity(intent);
//										overridePendingTransition(
//												R.anim.zoom_in,
//												android.R.anim.fade_out);
//									}
//								})
								.create().show();
			}
		});

		WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();

		mHosts.clear();
		LanHost gateWay = new LanHost(AppContext.getGatewayMac(),
				AppContext.getGateway(),
				NetworkUtils.vendorFromMac(NetworkUtils
						.stringMacToByte(AppContext.getGatewayMac())), wifiInfo
						.getSSID().replace("\"", ""));
		mHosts.add(gateWay);
		LanHost myself = new LanHost(wifiManager.getConnectionInfo()
				.getMacAddress(), AppContext.getIp(),
				NetworkUtils.vendorFromMac(NetworkUtils
						.stringMacToByte(wifiInfo.getMacAddress())),
				android.os.Build.MODEL);
		mHosts.add(myself);

		mHandler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				if (msg.what == DATASET_CHANGED) {
					mHosts.add((LanHost) msg.obj);
					hostAdapter.notifyDataSetChanged();
					headerText.setText("发现局域网中的 " + mHosts.size() + " 台主机");
				} else if (msg.what == DATASET_HOST_ALIAS_CHANGED) {
					int i = msg.arg1;
					LanHost host = mHosts.get(i);
					host.setAlias((String) msg.obj);
					hostAdapter.notifyDataSetChanged();
				}
				super.handleMessage(msg);
			}
		};
	}

	private void startDiscovery() {
		stop = false;

		if (discoveryThread != null && !discoveryThread.isAlive()) {
			discoveryThread.interrupt();
			discoveryThread = null;
		}
		discoveryThread = new DiscoveryThread();
		discoveryThread.start();

		if (arpReader != null && !arpReader.isAlive()) {
			arpReader.interrupt();
			arpReader = null;
		}
		arpReader = new ArpReadThread();
		arpReader.start();
	}

	class RecvThread extends Thread {

		String target_ip;

		public RecvThread(String target_ip) {
			this.target_ip = target_ip;
		}

		public void run() {
			byte[] buffer = new byte[128];
			DatagramSocket socket = null;
			String name;
			try {
				InetAddress inetAddress = InetAddress.getByName(target_ip);
				DatagramPacket packet = new DatagramPacket(buffer,
						buffer.length, inetAddress, NETBIOS_UDP_PORT), query = new DatagramPacket(
						NETBIOS_REQUEST, NETBIOS_REQUEST.length, inetAddress,
						NETBIOS_UDP_PORT);
				socket = new DatagramSocket();
				socket.setSoTimeout(200);

				for (int i = 0; i < 3; i++) {
					socket.send(query);
					socket.receive(packet);

					byte[] data = packet.getData();
					if (data != null && data.length >= 74) {
						String response = new String(data, "ASCII");
						name = response.substring(57, 73).trim();

						for (int k = 0; k < mHosts.size(); k++) {
							LanHost h = mHosts.get(k);
							if (h.getIp().equals(target_ip)) {
								mHandler.obtainMessage(
										DATASET_HOST_ALIAS_CHANGED, k, 0, name)
										.sendToTarget();
								break;
							}
						}
						break;
					}
				}
			} catch (SocketTimeoutException ste) {
			} catch (IOException e) {
			} finally {
				if (socket != null)
					socket.close();
			}

		}
	}

	/**
	 * 
	 * 多线程按照IP地址递增扫描 使用线程池 固定大小10
	 * 
	 * @author oinux
	 *
	 */
	class DiscoveryThread extends Thread {

		ExecutorService executor;

		public void run() {
			if (executor != null && !executor.isShutdown()) {
				executor.shutdownNow();
				executor = null;
			}
			executor = Executors.newFixedThreadPool(10);

			int next_int_ip = 0;
			try {
				while (!stop) {
					next_int_ip = AppContext.getIntNetMask()
							& AppContext.getIntGateway();
					for (int i = 0; i < AppContext.getHostCount() && !stop; i++) {
						next_int_ip = NetworkUtils.nextIntIp(next_int_ip);
						if (next_int_ip != -1) {
							String ip = NetworkUtils.netfromInt(next_int_ip);
							try {
								executor.execute(new UDPThread(ip));
							} catch (RejectedExecutionException e) {
								break;
							} catch (OutOfMemoryError m) {
								break;
							}
						}
					}
					Thread.sleep(5000);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			} finally {
				if (executor != null)
					executor.shutdownNow();
			}
		}
	}

	/**
	 * 读取arp缓存文件
	 * 
	 * 每隔三秒一次
	 * 
	 * @author oinux
	 *
	 */
	class ArpReadThread extends Thread {

		ExecutorService executor;

		public void run() {
			if (executor != null && !executor.isShutdown()) {
				executor.shutdownNow();
				executor = null;
			}
			executor = Executors.newFixedThreadPool(5);

			RandomAccessFile fileReader = null;
			try {
				fileReader = new RandomAccessFile("/proc/net/arp", "r");
				StringBuilder sb = new StringBuilder();
				int len = -1;
				String line = null;
				Matcher matcher = null;

				while (!stop) {
					fileReader.seek(0);
					while (!stop && (len = fileReader.read()) >= 0) {
						sb.append((char) len);
						if (len != '\n')
							continue;
						line = sb.toString();
						sb.setLength(0);

						if ((matcher = ARP_TABLE_PARSER.matcher(line)) != null
								&& matcher.find()) {
							String address = matcher.group(1), flags = matcher
									.group(3), hwaddr = matcher.group(4), device = matcher
									.group(6);
							if (device
									.equals(networkInterface.getDisplayName())
									&& !hwaddr.equals("00:00:00:00:00:00")
									&& flags.contains("2")) {
								// Log.v("mac", address + ">>" + hwaddr);
								boolean contains = false;

								for (LanHost h : mHosts) {
									if (h.getMac().equals(hwaddr)
											|| h.getIp().equals(address)) {
										contains = true;
										break;
									}
								}
								if (!contains) {
									byte[] mac_bytes = NetworkUtils
											.stringMacToByte(hwaddr);
									String vendor = NetworkUtils
											.vendorFromMac(mac_bytes);
									LanHost host = new LanHost(hwaddr, address,
											vendor);
									mHandler.obtainMessage(DATASET_CHANGED,
											host).sendToTarget();
									executor.execute(new RecvThread(address));
								}
							}
						}
					}
					Thread.sleep(3000);
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					if (fileReader != null)
						fileReader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				if (executor != null)
					executor.shutdownNow();
			}
		}
	}

	/**
	 * 
	 * 发送NETBIOS数据包
	 * 
	 * @author oinux
	 *
	 */
	class UDPThread extends Thread {

		String target_ip;

		public UDPThread(String target_ip) {
			this.target_ip = target_ip;
		}

		public void run() {
			DatagramSocket socket = null;
			try {
				socket = new DatagramSocket();
				// Log.d(TAG, target_ip);
				InetAddress address = InetAddress.getByName(target_ip);
				DatagramPacket packet = new DatagramPacket(NETBIOS_REQUEST,
						NETBIOS_REQUEST.length, address, NETBIOS_UDP_PORT);
				socket.setSoTimeout(200);
				socket.send(packet);
				socket.close();
			} catch (UnknownHostException e) {
			} catch (IOException e) {
			} finally {
				if (socket != null)
					socket.close();
			}
		}
	}

	private void stopDiscovery() {
		stop = true;
		if (arpReader != null && arpReader.isAlive()) {
			arpReader.interrupt();
			arpReader = null;
		}
		if (discoveryThread != null && !discoveryThread.isAlive()) {
			discoveryThread.interrupt();
			discoveryThread = null;
		}
	}

	@Override
	public void onBackPressed() {
		stopDiscovery();
		finish();
		overridePendingTransition(R.anim.z_slide_in_top,
				R.anim.z_slide_out_bottom);
	}

	@Override
	protected void onResume() {
		startDiscovery();
		super.onResume();
	}

	@Override
	protected void onPause() {
		stopDiscovery();
		super.onPause();
	}
}
