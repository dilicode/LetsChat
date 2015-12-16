package com.mstr.letschat.views;

import android.content.Context;
import android.util.AttributeSet;

import com.mstr.letschat.R;

/**
 * Created by dilli on 12/2/2015.
 */
public class IncomingLocationView extends LocationView {
    public IncomingLocationView(Context context) {
        this(context, null);
    }

    public IncomingLocationView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void showProgress(boolean sent) {
        throw new UnsupportedOperationException("progress is not displayed for incoming messages");
    }

    protected int getLayoutResource() {
        return R.layout.incoming_location_view;
    }
}