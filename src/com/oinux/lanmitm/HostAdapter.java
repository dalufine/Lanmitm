package com.oinux.lanmitm;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.oinux.lanmitm.entity.LanHost;

public class HostAdapter extends BaseAdapter {

	private Context mContext;
	private List<LanHost> mLanHosts;

	public HostAdapter(Context ctx, List<LanHost> lanHosts) {
		this.mContext = ctx;
		this.mLanHosts = lanHosts;
	}

	@Override
	public int getCount() {
		return mLanHosts.size();
	}

	@Override
	public LanHost getItem(int position) {
		return mLanHosts.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = View.inflate(mContext, R.layout.host_list_item, null);

		}
		TextView ipText = (TextView) convertView.findViewById(R.id.host_ip);
		TextView macText = (TextView) convertView.findViewById(R.id.host_mac);
		TextView vendorText = (TextView) convertView
				.findViewById(R.id.host_vendor);
		LanHost host = mLanHosts.get(position);
		String alias = host.getAlias();
		if (alias != null && !alias.isEmpty()) {
			ipText.setText(alias);
		} else {
			ipText.setText(host.getIp());
		}
		macText.setText(host.getMac());
		vendorText.setText(host.getVendor());
		return convertView;
	}
}
