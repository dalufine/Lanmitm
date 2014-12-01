package com.oinux.lanmitm.ui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.http.impl.cookie.BasicClientCookie;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.oinux.lanmitm.AppContext;
import com.oinux.lanmitm.R;
import com.oinux.lanmitm.entity.Session;

/**
 * 
 * @author oinux
 *
 */
@SuppressLint("SetJavaScriptEnabled")
public class BrowserActivity extends Activity implements OnClickListener {

	// private static final String DEFAULT_USER_AGENT =
	// "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_5) AppleWebKit/537.4 (KHTML, like Gecko) Chrome/22.0.1229.94 Safari/537.4";
	public static final int BROWSER_CURRENT_HIJACK = 1;
	public static final int BROWSER_HISTORY_HIJACK = 2;
	public static final int BROWSER_COMMON = 3;

	public static final String COOKIES_PATH = AppContext.getStoragePath()
			+ "/lanmitm/cookies";

	private SimpleDateFormat dateFormat = new SimpleDateFormat(
			"yyyyMMddHHmmss", Locale.getDefault());
	private WebSettings mSettings = null;
	private WebView mWebView = null;
	private ProgressBar progressBar;
	private ImageButton backBtn;
	private ImageButton forwardBtn;
	private ImageButton refreshBtn;
	private ImageButton saveBtn;
	private EditText urlEditText;
	private Session session;
	private int viewType = BROWSER_COMMON;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.browser_activity);

		progressBar = (ProgressBar) findViewById(R.id.hijack_web_progress);

		backBtn = (ImageButton) findViewById(R.id.web_back_btn);
		backBtn.setOnClickListener(this);
		forwardBtn = (ImageButton) findViewById(R.id.web_forward_btn);
		forwardBtn.setOnClickListener(this);
		refreshBtn = (ImageButton) findViewById(R.id.web_refresh_btn);
		refreshBtn.setOnClickListener(this);
		saveBtn = (ImageButton) findViewById(R.id.web_save_btn);
		saveBtn.setOnClickListener(this);

		urlEditText = (EditText) findViewById(R.id.url_edit_text);
		urlEditText.setOnEditorActionListener(new OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_DONE
						|| actionId == EditorInfo.IME_ACTION_GO
						|| actionId == EditorInfo.IME_ACTION_NEXT) {
					mWebView.loadUrl(urlEditText.getText().toString());
				}
				return false;
			}
		});

		mWebView = (WebView) findViewById(R.id.webView1);
		mSettings = mWebView.getSettings();

		mSettings.setJavaScriptEnabled(true);
		mSettings.setAppCacheEnabled(false);
		mSettings.setBuiltInZoomControls(false);
		// mSettings.setUserAgentString(DEFAULT_USER_AGENT);

		CookieSyncManager.createInstance(this);
		CookieManager.getInstance().removeAllCookie();

		mWebView.setWebViewClient(new WebViewClient() {

			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				view.loadUrl(url);
				urlEditText.setText(view.getUrl());
				return true;
			}

		});

		mWebView.setWebChromeClient(new WebChromeClient() {

			@Override
			public void onProgressChanged(WebView view, int newProgress) {
				if (newProgress == 100) {
					progressBar.setVisibility(View.GONE);
				} else {
					if (progressBar.getVisibility() == View.GONE)
						progressBar.setVisibility(View.VISIBLE);
					progressBar.setProgress(newProgress);
				}
				super.onProgressChanged(view, newProgress);
			}
		});

		viewType = getIntent().getIntExtra("view_type", BROWSER_COMMON);

		switch (viewType) {
		case BROWSER_COMMON:
			saveBtn.setVisibility(View.GONE);
			mWebView.loadUrl(getIntent().getStringExtra("url"));
			break;
		case BROWSER_HISTORY_HIJACK:
			saveBtn.setVisibility(View.GONE);
		case BROWSER_CURRENT_HIJACK:
			session = AppContext.getCurrentHijack();
			if (session != null) {
				String domain = null, rawcookie = null;

				for (BasicClientCookie cookie : session.getCookies().values()) {
					domain = cookie.getDomain();
					rawcookie = cookie.getName() + "=" + cookie.getValue()
							+ "; domain=" + domain + "; path=/";
					CookieManager.getInstance().setCookie(domain, rawcookie);
				}

				CookieSyncManager.getInstance().sync();

				if (session.getUserAgent() != null
						&& !session.getUserAgent().isEmpty())
					mSettings.setUserAgentString(session.getUserAgent());

				mWebView.loadUrl("http://" + session.getDomain());
			}
			break;
		default:
			break;
		}
		urlEditText.setText(mWebView.getUrl());
	}

	@Override
	protected void onDestroy() {
		mWebView.clearCache(true);
		mWebView.removeAllViews();
		mWebView.destroy();
		super.onDestroy();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.web_back_btn:
			mWebView.goBack();
			urlEditText.setText(mWebView.getOriginalUrl());
			break;
		case R.id.web_forward_btn:
			mWebView.goForward();
			urlEditText.setText(mWebView.getOriginalUrl());
			break;
		case R.id.web_refresh_btn:
			mWebView.reload();
			urlEditText.setText(mWebView.getOriginalUrl());
			break;
		case R.id.web_save_btn:
			saveSession();
			break;
		default:
			break;
		}
	}

	private void saveSession() {
		Date dateTime = new Date();
		StringBuilder sb = new StringBuilder();
		sb.append("{\"ip\":\"");
		sb.append(session.getIp());
		sb.append("\",");
		sb.append("\"domain\":\"");
		sb.append(session.getDomain());
		sb.append("\",");
		sb.append("\"userAgent\":\"");
		sb.append(session.getUserAgent());
		sb.append("\",");
		sb.append("\"path\":\"");
		sb.append(session.getPath());
		sb.append("\",");
		Map<String, BasicClientCookie> cookies = session.getCookies();
		if (cookies != null) {
			sb.append("\"cookies\":{");
			Set<Entry<String, BasicClientCookie>> entrySet = cookies.entrySet();
			Iterator<Entry<String, BasicClientCookie>> it = entrySet.iterator();
			while (it.hasNext()) {
				Entry<String, BasicClientCookie> cookie = it.next();
				sb.append("\"");
				sb.append(cookie.getKey());
				sb.append("\"");
				sb.append(":{\"name\":\"");
				sb.append(cookie.getValue().getName());
				sb.append("\",\"domain\":\"");
				sb.append(cookie.getValue().getDomain());
				sb.append("\",\"value\":\"");
				sb.append(cookie.getValue().getValue());
				sb.append("\"},");
			}
			sb.deleteCharAt(sb.length() - 1);
			sb.append("}");
		}
		sb.append(",\"dateTime\":\"");
		sb.append(dateFormat.format(dateTime));
		sb.append("\"}");
		FileOutputStream outputStream = null;
		File storageFile = new File(COOKIES_PATH);
		String fileName = storageFile.getAbsolutePath() + "/"
				+ session.getDomain() + "_" + dateFormat.format(dateTime)
				+ ".lanmitm";
		try {
			if (!storageFile.exists())
				storageFile.mkdirs();
			outputStream = new FileOutputStream(fileName);
			outputStream.write(sb.toString().getBytes());
			outputStream.flush();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (outputStream != null)
				try {
					outputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
		Toast.makeText(this, "已保存至" + fileName, Toast.LENGTH_LONG).show();
	}
}
