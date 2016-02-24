package com.mstr.letschat.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ProgressBar;

import com.mstr.letschat.R;

/**
 * Created by dilli on 12/2/2015.
 */
public class OutgoingLocationView extends LocationView {
    private ProgressBar progressBar;

    public OutgoingLocationView(Context context) {
        this(context, null);
    }

    public OutgoingLocationView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void init(Context context) {
        super.init(context);

        progressBar = (ProgressBar)findViewById(R.id.sending_progress);
    }

    public void showProgress(boolean sent) {
        if (sent) {
            progressBar.setVisibility(View.INVISIBLE);
        } else {
            progressBar.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.outgoing_location_view;
    }
}