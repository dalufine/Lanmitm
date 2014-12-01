package com.oinux.lanmitm;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class ActionBarActivity extends Activity {

	private TextView actionBarText;

	protected final void setBarTitle(CharSequence title) {
		actionBarText.setText(title);
	}

	public void onCreate(Bundle savedInstanceState, int layoutId) {
		setContentView(layoutId);
		actionBarText = (TextView) findViewById(R.id.actionbar_logo_text);
		super.onCreate(savedInstanceState);
	}
}
