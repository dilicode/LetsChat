package com.mstr.letschat.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mstr.letschat.R;

/**
 * Created by dilli on 12/1/2015.
 */
public abstract class PlainTextView extends MessageView {
    protected TextView messageText;

    public PlainTextView(Context context) {
        this(context, null);
    }

    public PlainTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setMessageText(String text) {
        messageText.setText(text);
    }

    @Override
    protected void init(Context context) {
        super.init(context);
        messageText = (TextView)findViewById(R.id.tv_message);

        setOrientation(LinearLayout.VERTICAL);
    }
}