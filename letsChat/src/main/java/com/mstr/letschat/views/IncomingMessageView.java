package com.mstr.letschat.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mstr.letschat.R;

public class IncomingMessageView extends MessageView {
	public IncomingMessageView(Context context) {
		super(context);
		
		init(context);
	}
	
	public IncomingMessageView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	
	public IncomingMessageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		init(context);
	}
	
	private void init(Context context) {
		LayoutInflater.from(context).inflate(R.layout.incoming_message_view, this);
		
		messageText = (TextView)findViewById(R.id.tv_message);
		timeText = (TextView)findViewById(R.id.tv_time);
		dateSectionText = (TextView)findViewById(R.id.tv_date);
		
		setOrientation(LinearLayout.VERTICAL);
	}
}