package com.oinux.lanmitm.entity;

public class LanHost {

	private String mac;
	private String ip;
	private String vendor;
	private String alias;

	public LanHost() {}

	public LanHost(String mac, String ip, String vendor) {
		this.mac = mac;
		this.ip = ip;
		this.vendor = vendor;
	}
	
	public LanHost(String mac, String ip, String vendor, String alias) {
		this.mac = mac;
		this.ip = ip;
		this.vendor = vendor;
		this.alias = alias;
	}
	
	public LanHost(String mac, String ip) {
		this.mac = mac;
		this.ip = ip;
	}
	
	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}
	
	public String getVendor() {
		return vendor;
	}

	public void setVendor(String vendor) {
		this.vendor = vendor;
	}

	public String getMac() {
		return mac;
	}

	public void setMac(String mac) {
		this.mac = mac;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

}
