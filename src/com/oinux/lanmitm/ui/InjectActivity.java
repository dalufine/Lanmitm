package com.oinux.lanmitm.ui;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.oinux.lanmitm.ActionBarActivity;
import com.oinux.lanmitm.AppContext;
import com.oinux.lanmitm.R;
import com.oinux.lanmitm.service.HijackService;
import com.oinux.lanmitm.service.InjectService;

/**
 * 
 * @author oinux
 *
 */
public class InjectActivity extends ActionBarActivity {

	private static final String TAG = "InjectActivity";

	private CheckBox injectCheckBox;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState,
				com.oinux.lanmitm.R.layout.hijack_activity);

		setBarTitle(Html.fromHtml("<b>代码注入</b> - <small>"
				+ AppContext.getTarget().getIp() + "</small>"));

		injectCheckBox = (CheckBox) findViewById(R.id.hijack_check_box);
		if (AppContext.isInjectRunning) {
			injectCheckBox.setChecked(true);
		} else {
			injectCheckBox.setChecked(false);
		}
		injectCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				Intent intent = new Intent(InjectActivity.this,
						InjectService.class);
				if (isChecked) {
					startService(intent);
				} else {
					stopService(intent);
					NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
					nm.cancel(HijackService.INJECT_NOTICE);
				}
			}
		});
	}

	
	@Override
	public void onBackPressed() {
		finish();
		overridePendingTransition(R.anim.z_slide_in_top,
				R.anim.z_slide_out_bottom);
	}
}
