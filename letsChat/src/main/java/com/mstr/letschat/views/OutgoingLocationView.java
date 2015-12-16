package com.mstr.letschat.views;

import android.content.Context;
import android.util.AttributeSet;

import com.mstr.letschat.R;

/**
 * Created by dilli on 12/2/2015.
 */
public class OutgoingLocationView extends LocationView {
    public OutgoingLocationView(Context context) {
        this(context, null);
    }

    public OutgoingLocationView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void showProgress(boolean sent) {}

    @Override
    protected int getLayoutResource() {
        return R.layout.outgoing_location_view;
    }
}