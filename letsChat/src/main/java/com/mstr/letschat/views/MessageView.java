package com.mstr.letschat.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mstr.letschat.R;

public abstract class MessageView extends LinearLayout {
	protected TextView time;
	protected TextView dateSeparator;

	public MessageView(Context context) {
		this(context, null);
	}
	
	public MessageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	protected void init(Context context) {
		dateSeparator = (TextView)findViewById(R.id.tv_date);

		int padding = context.getResources().getDimensionPixelOffset(R.dimen.message_list_item_padding);
		setPadding(padding, padding, padding, padding);
	}

	public void setTimeText(String text) {
		time.setText(text);
	}
	
	public void displayDateSeparator(String text) {
		dateSeparator.setVisibility(View.VISIBLE);
		dateSeparator.setText(text);
	}
	
	public void hideDateSeparator() {
		dateSeparator.setVisibility(View.GONE);
	}

	public abstract void showProgress(boolean sent);
}