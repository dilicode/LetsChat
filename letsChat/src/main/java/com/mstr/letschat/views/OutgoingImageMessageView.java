package com.mstr.letschat.views;

import android.content.Context;
import android.util.AttributeSet;

import com.mstr.letschat.R;

/**
 * Created by dilli on 1/29/2016.
 */
public class OutgoingImageMessageView extends ImageMessageView {
    public OutgoingImageMessageView(Context context) {
        super(context);
    }

    public OutgoingImageMessageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.outgoing_image_view;
    }

    @Override
    public void showProgress(boolean sent) {}
}