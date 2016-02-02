package com.mstr.letschat.tasks;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;

import com.mstr.letschat.databases.ChatContract;
import com.mstr.letschat.databases.ChatMessageTableHelper;
import com.mstr.letschat.databases.ConversationTableHelper;
import com.mstr.letschat.providers.DatabaseContentProvider;
import com.mstr.letschat.utils.AppLog;

import java.util.ArrayList;

/**
 * Created by dilli on 1/29/2016.
 */
public abstract class SendMessageTask extends BaseAsyncTask<Void, Void, Boolean> {
    protected String to;
    protected String nickname;
    protected String body;

    public SendMessageTask(Response.Listener<Boolean> listener, Context context, String to, String nickname, String body) {
        super(listener, context);

        this.to = to;
        this.nickname = nickname;
        this.body = body;
    }

    @Override
    public Response<Boolean> doInBackground(Void... params) {
        Context context = getContext();
        if (context == null) {
            return null;
        }

        ContentValues values;
        try {
            values = newMessage(System.currentTimeMillis());
        } catch(Exception e) {
            return Response.error(e);
        }

        ContentResolver contentResolver = context.getContentResolver();
        Uri newMessageUri;
        try {
            newMessageUri = insertNewMessage(contentResolver, values);
        } catch(Exception e) {
            return Response.error(e);
        }

        try {
            doSend(context);
        } catch(Exception e) {
            AppLog.e(String.format("send message to %s error", to), e);

            contentResolver.update(newMessageUri, ChatMessageTableHelper.newFailureStatusContentValues(), null, null);
            return Response.error(e);
        }

        contentResolver.update(newMessageUri, ChatMessageTableHelper.newSuccessStatusContentValues(), null, null);
        return Response.success(true);
    }

    protected Uri insertNewMessage(ContentResolver contentResolver, ContentValues messageValues) throws RemoteException, OperationApplicationException {
        long timeMillis = messageValues.getAsLong(ChatContract.ChatMessageTable.COLUMN_NAME_TIME);

        ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();
        operations.add(ContentProviderOperation.newInsert(ChatContract.ChatMessageTable.CONTENT_URI).withValues(messageValues).build());

        Cursor cursor = contentResolver.query(ChatContract.ConversationTable.CONTENT_URI,
                new String[]{ChatContract.ConversationTable._ID}, ChatContract.ConversationTable.COLUMN_NAME_NAME + "=?", new String[]{to}, null);
        if (cursor.moveToFirst()) { // there is a conversation already
            Uri conversationItemUri = ContentUris.withAppendedId(ChatContract.ConversationTable.CONTENT_URI,
                    cursor.getInt(cursor.getColumnIndex(ChatContract.ConversationTable._ID)));

            ContentValues values = ConversationTableHelper.newUpdateContentValues(body, timeMillis);
            operations.add(ContentProviderOperation.newUpdate(conversationItemUri).withValues(values).build());
        } else {
            ContentValues values = ConversationTableHelper.newInsertContentValues(to, nickname, body, timeMillis, 0);
            operations.add(ContentProviderOperation.newInsert(ChatContract.ConversationTable.CONTENT_URI).withValues(values).build());
        }

        cursor.close();

        ContentProviderResult[] result = contentResolver.applyBatch(DatabaseContentProvider.AUTHORITY, operations);
        return result[0].uri;
    }

    protected abstract ContentValues newMessage(long sendTimeMillis) throws Exception;

    protected abstract void doSend(Context context) throws Exception;
}