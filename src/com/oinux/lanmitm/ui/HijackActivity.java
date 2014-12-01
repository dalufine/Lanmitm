package com.oinux.lanmitm.ui;

import java.util.ArrayList;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.oinux.lanmitm.ActionBarActivity;
import com.oinux.lanmitm.AppContext;
import com.oinux.lanmitm.R;
import com.oinux.lanmitm.entity.Session;
import com.oinux.lanmitm.service.HijackService;

public class HijackActivity extends ActionBarActivity {

	private static final String TAG = "HijackActivity";

	private SessionListAdapter mAdapter = null;
	private ListView mListView;
	private CheckBox hijackCheckBox;
	private UpdateReceiver updateReceiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState,
				com.oinux.lanmitm.R.layout.hijack_activity);

		setBarTitle(Html.fromHtml("<b>会话劫持</b> - <small>"
				+ AppContext.getTarget().getIp() + "</small>"));

		hijackCheckBox = (CheckBox) findViewById(R.id.hijack_check_box);
		if (AppContext.isHijackRunning) {
			hijackCheckBox.setChecked(true);
		} else {
			hijackCheckBox.setChecked(false);
		}
		hijackCheckBox
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						Intent intent = new Intent(HijackActivity.this,
								HijackService.class);
						if (isChecked) {
							startService(intent);
						} else {
							stopService(intent);
							NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
							nm.cancel(HijackService.HIJACK_NOTICE);
						}
					}
				});

		updateReceiver = new UpdateReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(HijackService.DATASET_CHANGED);
		filter.addAction(HijackService.DATASET_COOKIES_CHANGED);
		registerReceiver(updateReceiver, filter);

		if (AppContext.getHijackList() == null) {
			AppContext.setHijackList(new ArrayList<Session>());
		}
		mListView = (ListView) findViewById(R.id.listView);
		mAdapter = new SessionListAdapter();
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					final int position, long id) {
				AppContext.setCurrentHijack(AppContext.getHijackList().get(
						position));
				Intent intent = new Intent(HijackActivity.this,
						BrowserActivity.class);
				intent.putExtra("view_type",
						BrowserActivity.BROWSER_CURRENT_HIJACK);
				startActivity(intent);
			}
		});
	}

	class UpdateReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(HijackService.DATASET_CHANGED)
					|| intent.getAction().equals(
							HijackService.DATASET_COOKIES_CHANGED)) {
				mAdapter.notifyDataSetChanged();
			}
		}
	}

	public class SessionListAdapter extends BaseAdapter {

		public class SessionHolder {
			ImageView favicon;
			TextView pathText;
			TextView domainText;
		}

		@Override
		public int getCount() {
			return AppContext.getHijackList().size();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View row = convertView;
			SessionHolder holder;
			Session session = AppContext.getHijackList().get(position);

			if (row == null) {
				LayoutInflater inflater = (LayoutInflater) HijackActivity.this
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				row = inflater
						.inflate(R.layout.hijack_list_item, parent, false);
				holder = new SessionHolder();
				holder.favicon = (ImageView) (row != null ? row
						.findViewById(R.id.server_icon) : null);
				holder.pathText = (TextView) (row != null ? row
						.findViewById(R.id.server_path) : null);
				holder.domainText = (TextView) (row != null ? row
						.findViewById(R.id.server_domain) : null);
				if (row != null)
					row.setTag(holder);
			} else
				holder = (SessionHolder) row.getTag();

			String tmp = session.getPath();
			if (holder.pathText != null)
				holder.pathText.setText(tmp);
			tmp = session.getDomain();
			if (holder.domainText != null)
				holder.domainText.setText(tmp);

			return row;
		}

		@Override
		public Session getItem(int position) {
			return AppContext.getHijackList().get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public void notifyDataSetChanged() {
			super.notifyDataSetChanged();
		}
	}

	@Override
	public void onBackPressed() {
		if (updateReceiver != null) {
			unregisterReceiver(updateReceiver);
		}
		if (!AppContext.isHijackRunning) {
			AppContext.setHijackList(new ArrayList<Session>());
		}
		finish();
		overridePendingTransition(R.anim.z_slide_in_top,
				R.anim.z_slide_out_bottom);
	}
}
