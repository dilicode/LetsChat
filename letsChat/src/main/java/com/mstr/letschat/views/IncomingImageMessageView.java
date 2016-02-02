package com.mstr.letschat.views;

import android.content.Context;
import android.util.AttributeSet;

import com.mstr.letschat.R;

/**
 * Created by dilli on 1/29/2016.
 */
public class IncomingImageMessageView extends ImageMessageView {
    public IncomingImageMessageView(Context context) {
        super(context);
    }

    public IncomingImageMessageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.incoming_image_view;
    }

    @Override
    public void showProgress(boolean sent) {}
}