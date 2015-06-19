package com.mstr.letschat.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.mstr.letschat.R;

public class OutgoingMessageView extends MessageView {
	private ProgressBar progressBar;
	
	public OutgoingMessageView(Context context) {
		super(context);
		
		init(context);
	}
	
	public OutgoingMessageView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	
	public OutgoingMessageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		init(context);
	}
	
	public void init(Context context) {
		LayoutInflater.from(context).inflate(R.layout.outgoing_message_view, this);
		
		messageText = (TextView)findViewById(R.id.tv_message);
		timeText = (TextView)findViewById(R.id.tv_time);
		dateSectionText = (TextView)findViewById(R.id.tv_date);
		progressBar = (ProgressBar)findViewById(R.id.message_sending_progress);
		
		setOrientation(LinearLayout.VERTICAL);
	}
	
	public void showProgress(boolean sent) {
		if (sent) {
			progressBar.setVisibility(View.INVISIBLE);
		} else {
			progressBar.setVisibility(View.VISIBLE);
		}
	}
}
