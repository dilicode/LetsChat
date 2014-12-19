package com.mstr.letschat.tasks;

import java.lang.ref.WeakReference;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.mstr.letschat.SmackInvocationException;
import com.mstr.letschat.databases.ChatContract.ContactRequestTable;
import com.mstr.letschat.databases.ChatContract.ContactTable;
import com.mstr.letschat.databases.ContactRequestTableHelper;
import com.mstr.letschat.databases.ContactTableHelper;
import com.mstr.letschat.model.Contact;
import com.mstr.letschat.tasks.Response.Listener;
import com.mstr.letschat.xmpp.SmackHelper;

public class AcceptContactRequestTask extends BaseAsyncTask<Void, Void, Contact> {
	private WeakReference<Context> contextWrapper;
	private WeakReference<Uri> uriWrapper;
	
	public AcceptContactRequestTask(Listener<Contact> listener, Context context, Uri uri) {
		super(listener);
		contextWrapper = new WeakReference<Context>(context);
		uriWrapper = new WeakReference<Uri>(uri);
	}
	
	@Override
	protected Response<Contact> doInBackground(Void... params) {
		Uri requestUri = uriWrapper.get();
		Context context = contextWrapper.get();
		if (requestUri != null && context != null) {
			Cursor cursor = context.getContentResolver().query(requestUri, 
					new String[]{ContactRequestTable.COLUMN_NAME_NICKNAME, ContactRequestTable.COLUMN_NAME_JID},
					null, null, null);
			if (cursor.moveToFirst()) {
				String from = cursor.getString(cursor.getColumnIndex(ContactRequestTable.COLUMN_NAME_JID));
				String fromNickname = cursor.getString(cursor.getColumnIndex(ContactRequestTable.COLUMN_NAME_NICKNAME));
				
				try {
					// 1. grant subscription to initiator
					SmackHelper.getInstance(context).approveSubscription(from);
					
					// 2. request permission to initiator
					SmackHelper.getInstance(context).addContact(from, fromNickname);
				} catch(SmackInvocationException e) {
					return Response.error(e);
				}
				
				// 3. save new contact to db
				Uri contactUri = context.getContentResolver().insert(ContactTable.CONTENT_URI, ContactTableHelper.newContentValues(from, fromNickname));
				Contact contact = new Contact((int)ContentUris.parseId(contactUri), from, fromNickname);
				
				// 4. update request status in db as accepted
				ContentValues values = ContactRequestTableHelper.newContentValuesWithAcceptedStatus();
				context.getContentResolver().update(ContactRequestTable.CONTENT_URI, values, ContactRequestTable.COLUMN_NAME_JID + " = ?", new String[]{from});
				
				return Response.success(contact);
			}
		}
		
		return null;
	}
	
	@Override
	protected void onPostExecute(Response<Contact> response) {
		Listener<Contact> listener = getListener();
		
		if (listener != null && response != null) {
			if (response.isSuccess()) {
				listener.onResponse(response.getResult());
			} else {
				listener.onErrorResponse(response.getException());
			}
		}
	}
}