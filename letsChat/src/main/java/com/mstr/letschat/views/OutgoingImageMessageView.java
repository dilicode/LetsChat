package com.mstr.letschat.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ProgressBar;

import com.mstr.letschat.R;

/**
 * Created by dilli on 1/29/2016.
 */
public class OutgoingImageMessageView extends ImageMessageView {
    private ProgressBar progressBar;

    public OutgoingImageMessageView(Context context) {
        super(context);
    }

    public OutgoingImageMessageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void init(Context context) {
        super.init(context);

        progressBar = (ProgressBar)findViewById(R.id.sending_progress);
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.outgoing_image_view;
    }

    @Override
    public void showProgress(boolean sent) {
        if (sent) {
            progressBar.setVisibility(View.INVISIBLE);
        } else {
            progressBar.setVisibility(View.VISIBLE);
        }
    }
}