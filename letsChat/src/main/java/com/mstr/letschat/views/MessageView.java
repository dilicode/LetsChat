package com.mstr.letschat.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mstr.letschat.R;
import com.mstr.letschat.databases.ChatMessageTableHelper;

public abstract class MessageView extends LinearLayout {
	protected TextView time;
	protected TextView dateSeparator;

	public MessageView(Context context) {
		this(context, null);
	}
	
	public MessageView(Context context, AttributeSet attrs) {
		super(context, attrs, R.attr.messageViewStyle);
		init(context);
	}

	protected void init(Context context) {
		LayoutInflater.from(context).inflate(getLayoutResource(), this);
		time = (TextView)findViewById(R.id.tv_time);
		dateSeparator = (TextView)findViewById(R.id.tv_date);
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

	public static MessageView newView(int type, Context context) {
		MessageView view = null;
		switch (type) {
			case ChatMessageTableHelper.TYPE_INCOMING_PLAIN_TEXT:
				view = new IncomingPlainTextView(context);
				break;

			case ChatMessageTableHelper.TYPE_OUTGOING_PLAIN_TEXT:
				view = new OutgoingPlainTextView(context);
				break;

			case ChatMessageTableHelper.TYPE_INCOMING_LOCATION:
				view = new IncomingLocationView(context);
				((LocationView)view).initializeMapView();
				break;

			case ChatMessageTableHelper.TYPE_OUTGOING_LOCATION:
				view = new OutgoingLocationView(context);
				((LocationView)view).initializeMapView();
				break;

			case ChatMessageTableHelper.TYPE_INCOMING_IMAGE:
				view = new IncomingImageMessageView(context);
				break;

			case ChatMessageTableHelper.TYPE_OUTGOING_IMAGE:
				view = new OutgoingImageMessageView(context);
				break;
		}

		if (view != null) {
			view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
		}

		return view;
	}

	protected abstract int getLayoutResource();
}