package com.mstr.letschat.tasks;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;

import com.mstr.letschat.SmackInvocationException;
import com.mstr.letschat.databases.ChatContract.ChatMessageTable;
import com.mstr.letschat.databases.ChatContract.ConversationTable;
import com.mstr.letschat.databases.ChatMessageTableHelper;
import com.mstr.letschat.databases.ConversationTableHelper;
import com.mstr.letschat.providers.CustomProvider;
import com.mstr.letschat.tasks.Response.Listener;
import com.mstr.letschat.xmpp.SmackHelper;

public class SendChatMessageTask extends BaseAsyncTask<Void, Void, Boolean> {
	private WeakReference<Context> contextWrapper;
	
	private String to;
	private String nickname;
	private String body;
	
	public SendChatMessageTask(Listener<Boolean> listener, Context context, String to, String nickname, String body) {
		super(listener);
		
		contextWrapper = new WeakReference<Context>(context);
		this.to = to;
		this.nickname = nickname;
		this.body = body;
	}
	
	@Override
	public Response<Boolean> doInBackground(Void... params) {
		Context context = contextWrapper.get();
		if (context != null) {
			ContentResolver contentResolver = context.getContentResolver();
			Uri newMessageUri = insertNewMessage(contentResolver);
			try {
				SmackHelper.getInstance(context).sendChatMessage(to, body);
			} catch(SmackInvocationException e) {
				contentResolver.update(newMessageUri, ChatMessageTableHelper.newFailureStatusContentValues(), null, null);
				
				return Response.error(e);
			}
			
			contentResolver.update(newMessageUri, ChatMessageTableHelper.newSuccessStatusContentValues(), null, null);
			
			return Response.success(true);
		}
		
		return null;
	}
	
	private Uri insertNewMessage(ContentResolver contentResolver) {
		long timeMillis = System.currentTimeMillis();
		
		ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();
		ContentValues messageValues = ChatMessageTableHelper.newOutgoingMessageContentValues(to, body, timeMillis);
		operations.add(ContentProviderOperation.newInsert(ChatMessageTable.CONTENT_URI).withValues(messageValues).build());
		
		Cursor cursor = contentResolver.query(ConversationTable.CONTENT_URI,
				new String[]{ConversationTable._ID}, ConversationTable.COLUMN_NAME_NAME + "=?", new String[]{to}, null);
		if (cursor.moveToFirst()) { // there is a conversation already
			Uri conversationItemUri = ContentUris.withAppendedId(ConversationTable.CONTENT_URI,
					cursor.getInt(cursor.getColumnIndex(ConversationTable._ID)));
			
			ContentValues values = ConversationTableHelper.newUpdateContentValues(body, timeMillis);
			operations.add(ContentProviderOperation.newUpdate(conversationItemUri).withValues(values).build());
		} else {
			ContentValues values = ConversationTableHelper.newInsertContentValues(to, nickname, body, timeMillis, 0);
			operations.add(ContentProviderOperation.newInsert(ConversationTable.CONTENT_URI).withValues(values).build());
		}
		
		cursor.close();
		
		try {
			ContentProviderResult[] result = contentResolver.applyBatch(CustomProvider.AUTHORITY, operations);
			return result[0].uri;
		} catch (Exception e) {
			throw new SQLException("Failed to insert chat message");
		}
	}	
	
	@Override
	public void onPostExecute(Response<Boolean> response) {
		Listener<Boolean> listener = getListener();
		
		if (listener != null) {
			if (response.isSuccess()) {
				listener.onResponse(response.getResult());
			} else {
				listener.onErrorResponse(response.getException());
			}
		}
	}
}