package com.oinux.lanmitm.widget;


import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.oinux.lanmitm.R;

public class YesOrNoDialog extends Dialog {

	public YesOrNoDialog(Context context, int theme) {
		super(context, theme);
	}

	public YesOrNoDialog(Context context) {
		super(context);
	}

	public static class Builder {

		private Context context;
		private String title;
		private String message;
		private String positiveButtonText;
		private String negativeButtonText;

		private DialogInterface.OnClickListener positiveButtonClickListener,
				negativeButtonClickListener;

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

		public Builder setMessage(String message) {
			this.message = message;
			return this;
		}

		public Builder setMessage(int message) {
			this.message = (String) context.getText(message);
			return this;
		}

		public Builder setPositiveButton(int positiveButtonText,
				DialogInterface.OnClickListener listener) {
			this.positiveButtonText = (String) context
					.getText(positiveButtonText);
			this.positiveButtonClickListener = listener;
			return this;
		}

		public Builder setPositiveButton(String positiveButtonText,
				DialogInterface.OnClickListener listener) {
			this.positiveButtonText = positiveButtonText;
			this.positiveButtonClickListener = listener;
			return this;
		}

		public Builder setNegativeButton(int negativeButtonText,
				DialogInterface.OnClickListener listener) {
			this.negativeButtonText = (String) context
					.getText(negativeButtonText);
			this.negativeButtonClickListener = listener;
			return this;
		}

		public Builder setNegativeButton(String negativeButtonText,
				DialogInterface.OnClickListener listener) {
			this.negativeButtonText = negativeButtonText;
			this.negativeButtonClickListener = listener;
			return this;
		}

		public YesOrNoDialog create() {
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			final YesOrNoDialog dialog = new YesOrNoDialog(context,
					R.style.Dialog_Radio);
			View layout = inflater.inflate(R.layout.dialog_alert, null);
			dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
			dialog.addContentView(layout, new LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
			if (positiveButtonText != null) {
				((Button) layout.findViewById(R.id.positive_btn))
						.setText(positiveButtonText);
				if (positiveButtonClickListener != null) {
					((Button) layout.findViewById(R.id.positive_btn))
							.setOnClickListener(new View.OnClickListener() {
								public void onClick(View v) {
									positiveButtonClickListener.onClick(dialog,
											DialogInterface.BUTTON_POSITIVE);
								}
							});
				}
			} else {
				layout.findViewById(R.id.positive_btn).setVisibility(View.GONE);
			}
			if (negativeButtonText != null) {
				((Button) layout.findViewById(R.id.negative_btn))
						.setText(negativeButtonText);
				if (negativeButtonClickListener != null) {
					((Button) layout.findViewById(R.id.negative_btn))
							.setOnClickListener(new View.OnClickListener() {
								public void onClick(View v) {
									negativeButtonClickListener.onClick(dialog,
											DialogInterface.BUTTON_NEGATIVE);
								}
							});
				}
			} else {
				layout.findViewById(R.id.negative_btn).setVisibility(View.GONE);
			}
			if (message != null) {
				((TextView) layout.findViewById(R.id.content)).setText(message);
			}
			if (title != null) {
				((TextView) layout.findViewById(R.id.title)).setText(title);
			}
			dialog.setContentView(layout);
			return dialog;
		}
	}
}