package com.mstr.letschat.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public abstract class MessageView extends LinearLayout {
	protected TextView messageText;
	protected TextView timeText;
	protected TextView dateSectionText;
	
	public MessageView(Context context) {
		super(context);
	}
	
	public MessageView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	
	public MessageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	public void setMessageText(String text) {
		messageText.setText(text);
	}
	
	public void setTimeText(String text) {
		timeText.setText(text);
	}
	
	public void displayDateSection(String text) {
		dateSectionText.setVisibility(View.VISIBLE);
		dateSectionText.setText(text);
	}
	
	public void hideDateSection() {
		dateSectionText.setVisibility(View.GONE);
	}
}