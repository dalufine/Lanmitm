package com.oinux.lanmitm.widget;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.RadioButton;
import android.widget.TextView;

import com.oinux.lanmitm.R;

public class RadioDialog extends Dialog {

	public RadioDialog(Context context, int theme) {
		super(context, theme);
	}

	public RadioDialog(Context context) {
		super(context);
	}

	public static class Builder {

		private Context context;
		private String title;
		private String radioText1;
		private String radioText2;
		private String radioText3;
		private String radioText4;
		private boolean radioCheck1;
		private boolean radioCheck2;
		private boolean radioCheck3;
		private boolean radioCheck4;

		private DialogInterface.OnClickListener radioClickListener1,
				radioClickListener2, radioClickListener3, radioClickListener4;

		public Builder(Context context) {
			this.context = context;
		}

		public Builder setTitle(String title) {
			this.title = title;
			return this;
		}

		public Builder setTitle(int title) {
			this.title = (String) context.getText(title);
			return this;
		}

		public Builder setRadio1(int radioText, boolean checked,
				DialogInterface.OnClickListener listener) {
			this.radioText1 = (String) context.getText(radioText);
			this.radioClickListener1 = listener;
			this.radioCheck1 = checked;
			return this;
		}

		public Builder setRadio2(int radioText, boolean checked,
				DialogInterface.OnClickListener listener) {
			this.radioText2 = (String) context.getText(radioText);
			this.radioClickListener2 = listener;
			this.radioCheck2 = checked;
			return this;
		}

		public Builder setRadio3(int radioText, boolean checked,
				DialogInterface.OnClickListener listener) {
			this.radioText3 = (String) context.getText(radioText);
			this.radioClickListener3 = listener;
			this.radioCheck3 = checked;
			return this;
		}

		public Builder setRadio4(int radioText, boolean checked,
				DialogInterface.OnClickListener listener) {
			this.radioText3 = (String) context.getText(radioText);
			this.radioClickListener3 = listener;
			this.radioCheck3 = checked;
			return this;
		}

		public Builder setRadio1(String radioText, boolean checked,
				DialogInterface.OnClickListener listener) {
			this.radioText1 = radioText;
			this.radioClickListener1 = listener;
			this.radioCheck1 = checked;
			return this;
		}

		public Builder setRadio2(String radioText, boolean checked,
				DialogInterface.OnClickListener listener) {
			this.radioText2 = radioText;
			this.radioClickListener2 = listener;
			this.radioCheck2 = checked;
			return this;
		}

		public Builder setRadio3(String radioText, boolean checked,
				DialogInterface.OnClickListener listener) {
			this.radioText3 = radioText;
			this.radioClickListener3 = listener;
			this.radioCheck3 = checked;
			return this;
		}

		public Builder setRadio4(String radioText, boolean checked,
				DialogInterface.OnClickListener listener) {
			this.radioText4 = radioText;
			this.radioClickListener4 = listener;
			this.radioCheck4 = checked;
			return this;
		}

		public RadioDialog create() {
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			final RadioDialog dialog = new RadioDialog(context,
					R.style.Dialog_Radio);
			View layout = inflater.inflate(R.layout.dialog_radio, null);
			dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
			dialog.addContentView(layout, new LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
			if (radioText1 != null) {
				RadioButton radioBtn1 = (RadioButton) layout
						.findViewById(R.id.dialog_radio1);
				radioBtn1.setChecked(radioCheck1);
				radioBtn1.setText(radioText1);
				if (radioClickListener1 != null) {
					radioBtn1.setOnClickListener(new View.OnClickListener() {
						public void onClick(View v) {
							radioClickListener1.onClick(dialog,
									DialogInterface.BUTTON_POSITIVE);
						}
					});
				}
			} else {
				layout.findViewById(R.id.dialog_radio1)
						.setVisibility(View.GONE);
			}
			if (radioText2 != null) {
				RadioButton radioBtn2 = (RadioButton) layout
						.findViewById(R.id.dialog_radio2);
				radioBtn2.setChecked(radioCheck2);
				radioBtn2.setText(radioText2);
				if (radioClickListener2 != null) {
					radioBtn2.setOnClickListener(new View.OnClickListener() {
						public void onClick(View v) {
							radioClickListener2.onClick(dialog,
									DialogInterface.BUTTON_NEGATIVE);
						}
					});
				}
			} else {
				layout.findViewById(R.id.dialog_radio2)
						.setVisibility(View.GONE);
			}
			if (radioText3 != null) {
				RadioButton radioBtn3 = (RadioButton) layout
						.findViewById(R.id.dialog_radio3);
				radioBtn3.setChecked(radioCheck3);
				radioBtn3.setText(radioText3);
				if (radioClickListener3 != null) {
					radioBtn3.setOnClickListener(new View.OnClickListener() {
						public void onClick(View v) {
							radioClickListener3.onClick(dialog,
									DialogInterface.BUTTON_NEUTRAL);
						}
					});
				}
			} else {
				layout.findViewById(R.id.dialog_radio3)
						.setVisibility(View.GONE);
			}
			if (radioText4 != null) {
				RadioButton radioBtn4 = (RadioButton) layout
						.findViewById(R.id.dialog_radio4);
				radioBtn4.setChecked(radioCheck4);
				radioBtn4.setText(radioText4);
				if (radioClickListener4 != null) {
					radioBtn4.setOnClickListener(new View.OnClickListener() {
						public void onClick(View v) {
							radioClickListener4.onClick(dialog,
									DialogInterface.BUTTON_NEUTRAL);
						}
					});
				}
			} else {
				layout.findViewById(R.id.dialog_radio4)
						.setVisibility(View.GONE);
			}
			if (title != null) {
				((TextView) layout.findViewById(R.id.title)).setText(title);
			}
			dialog.setContentView(layout);
			return dialog;
		}
	}
}