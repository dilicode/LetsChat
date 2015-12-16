package com.mstr.letschat.tasks;

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
import com.mstr.letschat.providers.DatabaseContentProvider;
import com.mstr.letschat.tasks.Response.Listener;
import com.mstr.letschat.utils.AppLog;
import com.mstr.letschat.xmpp.SmackHelper;

import org.jivesoftware.smack.packet.PacketExtension;

import java.util.ArrayList;

public class SendChatMessageTask extends BaseAsyncTask<Void, Void, Boolean> {
	protected String to;
	protected String nickname;
	protected String body;

	protected PacketExtension packetExtension;
	
	public SendChatMessageTask(Listener<Boolean> listener, Context context, String to, String nickname, String body) {
		super(listener, context);
		
		this.to = to;
		this.nickname = nickname;
		this.body = body;
	}
	
	@Override
	public Response<Boolean> doInBackground(Void... params) {
		Context context = getContext();
		if (context != null) {
			ContentResolver contentResolver = context.getContentResolver();
			Uri newMessageUri = insertNewMessage(contentResolver);
			try {
				SmackHelper.getInstance(context).sendChatMessage(to, body, packetExtension);
			} catch(SmackInvocationException e) {
				AppLog.e(String.format("send chat message to %s error", to), e);
				
				contentResolver.update(newMessageUri, ChatMessageTableHelper.newFailureStatusContentValues(), null, null);
				
				return Response.error(e);
			}
			
			contentResolver.update(newMessageUri, ChatMessageTableHelper.newSuccessStatusContentValues(), null, null);
			
			return Response.success(true);
		}
		
		return null;
	}
	
	protected Uri insertNewMessage(ContentResolver contentResolver) {
		long timeMillis = System.currentTimeMillis();

		ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();
		ContentValues messageValues = getNewMessage(timeMillis);
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
			ContentProviderResult[] result = contentResolver.applyBatch(DatabaseContentProvider.AUTHORITY, operations);
			return result[0].uri;
		} catch (Exception e) {
			throw new SQLException("Failed to insert chat message");
		}
	}

	protected ContentValues getNewMessage(long timeMillis) {
		return ChatMessageTableHelper.newPlainTextMessage(to, body, timeMillis, true);
	}
}