package com.mstr.letschat.tasks;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;

import com.mstr.letschat.R;
import com.mstr.letschat.bitmapcache.BitmapUtils;
import com.mstr.letschat.databases.ChatMessageTableHelper;
import com.mstr.letschat.utils.FileUtils;
import com.mstr.letschat.xmpp.SmackHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by dilli on 1/28/2016.
 */
public class SendImageTask extends SendMessageTask {
    private String fileName;
    private Uri uri;
    private File file;

    public SendImageTask(Response.Listener<Boolean> listener, Context context, String to, String nickname, String body, Uri uri, String fileName) {
        super(listener, context, to, nickname, body);

        this.uri = uri;
        this.fileName = fileName;
    }

    private File createSentImageFile(Context context, String fileName, Bitmap bitmap) throws IOException {
        file = new File(FileUtils.getSentImagesDir(context), fileName + FileUtils.IMAGE_EXTENSION);
        FileOutputStream outputStream = new FileOutputStream(file);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        outputStream.flush();
        outputStream.close();

        return file;
    }

    @Override
    protected ContentValues newMessage(long sendTimeMillis) throws IOException {
        Context context = getContext();
        int size = context.getResources().getDimensionPixelSize(R.dimen.sent_image_size);
        Bitmap bitmap = BitmapUtils.decodeSampledBitmapFromStream(context, uri, size, size);
        file = createSentImageFile(context, fileName, bitmap);

        return ChatMessageTableHelper.newImageMessage(to, body, sendTimeMillis, file.getAbsolutePath(), true);
    }

    @Override
    protected void doSend(Context context) throws Exception {
        if (file != null) {
            SmackHelper.getInstance(context).sendImage(file, to);
        } else {
            throw new Exception("no image found");
        }
    }
}