package com.oinux.lanmitm.ui;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.oinux.lanmitm.ActionBarActivity;
import com.oinux.lanmitm.AppContext;
import com.oinux.lanmitm.R;
import com.oinux.lanmitm.service.HttpService;

public class HttpActivity extends ActionBarActivity implements OnClickListener {

	private CheckBox httpServerCheckBox;
	private View headView;
	private TextView serverLogText;
	private ScrollView logScrollView;
	private Button cloneBtn;

	private LogReceiver logReceiver;
	private Handler handler;
	private String url;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState, R.layout.http_server);
		setBarTitle(Html.fromHtml("<b>HttpServer配置</b>"));

		cloneBtn = (Button) findViewById(R.id.httpserver_clone_btn);
		cloneBtn.setOnClickListener(this);

		headView = findViewById(R.id.http_server_head);
		serverLogText = (TextView) findViewById(R.id.http_server_log);
		serverLogText.setMovementMethod(new ScrollingMovementMethod());

		url = "http://" + AppContext.getIp() + ":" + HttpService.PORT;

		if (!AppContext.isHttpserverRunning)
			serverLogText.setText(Html.fromHtml("<br/>这里是日志窗口：<br/><br/>"
					+ "—http文档目录:存储卡/lanmitm/www<br/>" + "—开启后请在浏览器中输入<br/>"
					+ "—" + url + " 访问页面<br/><br/>"));
		else
			serverLogText.setText(AppContext.getServerLog().toString());

		serverLogText.setOnClickListener(this);

		logScrollView = (ScrollView) findViewById(R.id.http_server_log_scroll);

		httpServerCheckBox = (CheckBox) findViewById(R.id.http_server_check_box);
		if (AppContext.isHttpserverRunning) {
			httpServerCheckBox.setChecked(true);
			headView.setVisibility(View.VISIBLE);
		} else {
			httpServerCheckBox.setChecked(false);
		}
		httpServerCheckBox
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						if (isChecked) {
							startService(new Intent(HttpActivity.this,
									HttpService.class));
							headView.setVisibility(View.VISIBLE);
							Animation animation = new AlphaAnimation(0.0f, 1.0f);
							animation.setDuration(500);
							headView.startAnimation(animation);
						} else {
							stopService(new Intent(HttpActivity.this,
									HttpService.class));
							NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
							nm.cancel(HttpService.HTTP_SERVER_NOTICE);
							Animation animation = new AlphaAnimation(1.0f, 0.0f);
							animation.setDuration(500);
							headView.startAnimation(animation);
							headView.setVisibility(View.GONE);
						}
					}
				});

		handler = new Handler();

		logReceiver = new LogReceiver();
		IntentFilter filter = new IntentFilter(
				HttpService.SERVER_LOG_CHANGE_INTENT);
		registerReceiver(logReceiver, filter);
	}

	class LogReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if ("server_log_change_intent".equals(intent.getAction())) {
				serverLogText.setText(AppContext.getServerLog().toString());
				handler.post(new Runnable() {
					@Override
					public void run() {
						logScrollView.fullScroll(ScrollView.FOCUS_DOWN);
					}
				});
			}
		}
	}

	@Override
	protected void onDestroy() {
		if (logReceiver != null)
			unregisterReceiver(logReceiver);
		if (!AppContext.isHttpserverRunning)
			AppContext.getServerLog().setLength(0);
		super.onDestroy();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.http_server_log:
			Intent intent = new Intent(this, BrowserActivity.class);
			intent.putExtra("view_type", BrowserActivity.BROWSER_COMMON);
			intent.putExtra("url", url);
			startActivity(intent);
			break;
		case R.id.httpserver_clone_btn:
			Toast.makeText(this, "还没写", Toast.LENGTH_SHORT).show();
		default:
			break;
		}
	}

}
