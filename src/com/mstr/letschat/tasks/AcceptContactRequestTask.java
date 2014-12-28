package com.mstr.letschat.tasks;

import java.lang.ref.WeakReference;

import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.mstr.letschat.SmackInvocationException;
import com.mstr.letschat.databases.ChatContract.ContactRequestTable;
import com.mstr.letschat.model.Contact;
import com.mstr.letschat.tasks.Response.Listener;
import com.mstr.letschat.utils.ProviderUtils;
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
			ContentResolver contentResolver = context.getContentResolver();
			Cursor cursor = contentResolver.query(requestUri, 
					new String[]{ContactRequestTable.COLUMN_NAME_NICKNAME, ContactRequestTable.COLUMN_NAME_JID},
					null, null, null);
			if (cursor.moveToFirst()) {
				String jid = cursor.getString(cursor.getColumnIndex(ContactRequestTable.COLUMN_NAME_JID));
				String nickname = cursor.getString(cursor.getColumnIndex(ContactRequestTable.COLUMN_NAME_NICKNAME));
				
				try {
					SmackHelper smackHelper = SmackHelper.getInstance(context);
					// 1. grant subscription to initiator
					smackHelper.approveSubscription(jid);
					
					// 2. request permission to initiator
					smackHelper.addContact(jid, nickname);
				} catch(SmackInvocationException e) {
					return Response.error(e);
				}
				
				// 3. save new contact into db
				ContentProviderResult[] result = ProviderUtils.addNewContact(context, jid, nickname);
				Uri contactUri = result[0].uri;
				Contact contact = new Contact((int)ContentUris.parseId(contactUri), jid, nickname);
				return Response.success(contact);
			}
			
			cursor.close();
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