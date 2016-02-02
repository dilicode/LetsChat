package com.mstr.letschat.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.mstr.letschat.R;

/**
 * Created by dilli on 1/29/2016.
 */
public abstract class ImageMessageView extends MessageView {
    private ImageView image;

    public ImageMessageView(Context context) {
        this(context, null);
    }

    public ImageMessageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void init(Context context) {
        super.init(context);
        image = (ImageView)findViewById(R.id.image);

        setOrientation(LinearLayout.VERTICAL);
    }

    public ImageView getImageView() {
        return image;
    }
}