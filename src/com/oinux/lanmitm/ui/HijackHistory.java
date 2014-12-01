package com.oinux.lanmitm.ui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.http.impl.cookie.BasicClientCookie;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.oinux.lanmitm.ActionBarActivity;
import com.oinux.lanmitm.AppContext;
import com.oinux.lanmitm.R;
import com.oinux.lanmitm.entity.Session;

public class HijackHistory extends ActionBarActivity implements OnClickListener {

	private static final String TAG = "HijackActivity";

	private SimpleDateFormat parseDateFormat = new SimpleDateFormat(
			"yyyyMMddHHmmss", Locale.getDefault());
	private SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd HH:mm",
			Locale.getDefault());

	private SessionListAdapter mAdapter = null;
	private ListView mListView;
	private List<Session> hijackList;
	private ImageView delBtn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState,
				com.oinux.lanmitm.R.layout.hijack_history);

		setBarTitle(Html.fromHtml("<b>劫持历史</b>"));

		hijackList = new ArrayList<Session>();

		mListView = (ListView) findViewById(R.id.listView);
		mAdapter = new SessionListAdapter();
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					final int position, long id) {
				AppContext.setCurrentHijack(hijackList.get(position));
				Intent intent = new Intent(HijackHistory.this,
						BrowserActivity.class);
				intent.putExtra("view_type",
						BrowserActivity.BROWSER_HISTORY_HIJACK);
				startActivity(intent);
			}
		});

		delBtn = (ImageView) findViewById(R.id.actionbar_del);
		delBtn.setOnClickListener(this);

		loadSession();
	}

	public class SessionListAdapter extends BaseAdapter {

		public class SessionHolder {
			ImageView favicon;
			TextView pathText;
			TextView domainText;
			TextView dateText;
		}

		@Override
		public int getCount() {
			return hijackList.size();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View row = convertView;
			SessionHolder holder;
			Session session = hijackList.get(position);

			if (row == null) {
				LayoutInflater inflater = (LayoutInflater) HijackHistory.this
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
				holder.dateText = (TextView) (row != null ? row
						.findViewById(R.id.hijack_date) : null);
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

			if (holder.dateText != null) {
				holder.dateText
						.setText(dateFormat.format(session.getDateTime()));
				holder.dateText.setVisibility(View.VISIBLE);
			}

			return row;
		}

		@Override
		public Session getItem(int position) {
			return hijackList.get(position);
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

	private void loadSession() {
		File dic = new File(BrowserActivity.COOKIES_PATH);
		if (dic.exists()) {
			for (File file : dic.listFiles()) {
				if (file.getName().endsWith(".lanmitm")) {
					FileInputStream inputStream = null;
					try {
						byte[] buff = new byte[1024];
						inputStream = new FileInputStream(file);
						int len = -1;
						StringBuilder sb = new StringBuilder();
						while ((len = inputStream.read(buff)) != -1) {
							sb.append(new String(buff, 0, len));
						}
						Session session = parseJson(sb.toString());
						if (session != null)
							hijackList.add(session);
						mAdapter.notifyDataSetChanged();
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					} finally {
						if (inputStream != null) {
							try {
								inputStream.close();
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					}
				}
			}
		}
	}

	private Session parseJson(final String json) {
		Session session = null;
		try {
			JSONObject jsonObject = new JSONObject(json);
			session = new Session();
			session.setIp(jsonObject.optString("ip"));
			session.setDomain(jsonObject.optString("domain"));
			session.setPath(jsonObject.optString("path"));
			session.setUserAgent(jsonObject.optString("userAgent"));
			session.setDateTime(parseDateFormat.parse(jsonObject
					.optString("dateTime")));
			Map<String, BasicClientCookie> cookies = new HashMap<String, BasicClientCookie>();
			JSONObject cookiesObject = jsonObject.optJSONObject("cookies");
			JSONArray cookieArray = cookiesObject.names();
			for (int i = 0; i < cookieArray.length(); i++) {
				JSONObject cookieObject = cookiesObject
						.getJSONObject(cookieArray.getString(i));
				BasicClientCookie cookie = new BasicClientCookie(
						cookieObject.optString("name"),
						cookieObject.optString("value"));
				cookie.setDomain(cookieObject.optString("domain"));
				cookies.put(cookieArray.getString(i), cookie);
			}
			session.setCookies(cookies);
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return session;
	}

	@Override
	public void onBackPressed() {
		finish();
		overridePendingTransition(R.anim.z_slide_in_top,
				R.anim.z_slide_out_bottom);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.actionbar_del:
			File dic = new File(BrowserActivity.COOKIES_PATH);
			if (dic.exists()) {
				for (File file : dic.listFiles()) {
					if (file.getName().endsWith(".lanmitm")) {
						file.delete();
					}
				}
			}
			hijackList.clear();
			mAdapter.notifyDataSetChanged();
			break;

		default:
			break;
		}
	}
}
