package com.mstr.letschat.views;

import android.content.Context;
import android.util.AttributeSet;

import com.mstr.letschat.R;

public class IncomingPlainTextView extends PlainTextView {
	public IncomingPlainTextView(Context context) {
		this(context, null);
	}
	
	public IncomingPlainTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	protected int getLayoutResource() {
		return R.layout.incoming_plain_text_view;
	}

	public void showProgress(boolean sent) {
		throw new UnsupportedOperationException("progress is not displayed for incoming messages");
	}
}