package com.oinux.lanmitm.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.impl.cookie.BasicClientCookie;
/**
 * 
 * @author oinux
 *
 */
public class Session implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	private String ip;
	private String clientIp;
	private String domain;
	private String userAgent;
	private String path;
	private Map<String, BasicClientCookie> mCookies;
	
	/**
	 * 为了历史记录增加
	 */
	private Date dateTime;

	public Date getDateTime() {
		return dateTime;
	}

	public void setDateTime(Date dateTime) {
		this.dateTime = dateTime;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getClientIp() {
		return clientIp;
	}

	public void setClientIp(String clientIp) {
		this.clientIp = clientIp;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getUserAgent() {
		return userAgent;
	}

	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public Map<String, BasicClientCookie> getCookies() {
		if(mCookies == null){
			mCookies = new HashMap<String, BasicClientCookie>();
		}
		return mCookies;
	}

	public void setCookies(Map<String, BasicClientCookie> cookies) {
		this.mCookies = cookies;
	}
}
