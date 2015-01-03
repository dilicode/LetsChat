package com.mstr.letschat.tasks;

import java.lang.ref.WeakReference;

import org.jivesoftware.smackx.vcardtemp.packet.VCard;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.mstr.letschat.SmackInvocationException;
import com.mstr.letschat.databases.ChatContract.ContactRequestTable;
import com.mstr.letschat.model.UserProfile;
import com.mstr.letschat.tasks.Response.Listener;
import com.mstr.letschat.utils.ProviderUtils;
import com.mstr.letschat.xmpp.SmackHelper;
import com.mstr.letschat.xmpp.SmackVCardHelper;

public class AcceptContactRequestTask extends BaseAsyncTask<Void, Void, UserProfile> {
	private WeakReference<Context> contextWrapper;
	private WeakReference<Uri> uriWrapper;
	
	public AcceptContactRequestTask(Listener<UserProfile> listener, Context context, Uri uri) {
		super(listener);
		contextWrapper = new WeakReference<Context>(context);
		uriWrapper = new WeakReference<Uri>(uri);
	}
	
	@Override
	protected Response<UserProfile> doInBackground(Void... params) {
		Uri requestUri = uriWrapper.get();
		Context context = contextWrapper.get();
		if (requestUri != null && context != null) {
			ContentResolver contentResolver = context.getContentResolver();
			Cursor cursor = contentResolver.query(requestUri, 
					new String[]{ContactRequestTable.COLUMN_NAME_NICKNAME, ContactRequestTable.COLUMN_NAME_JID},
					null, null, null);
			try {
				if (cursor.moveToFirst()) {
					String jid = cursor.getString(cursor.getColumnIndex(ContactRequestTable.COLUMN_NAME_JID));
					String nickname = cursor.getString(cursor.getColumnIndex(ContactRequestTable.COLUMN_NAME_NICKNAME));
					
					SmackHelper smackHelper = SmackHelper.getInstance(context);
					// 1. grant subscription to initiator
					smackHelper.approveSubscription(jid);
					
					// 2. request permission to initiator
					smackHelper.addContact(jid, nickname);
					
					// 3. load VCard
					VCard vCard = smackHelper.getVCard(jid);
					String status = vCard.getField(SmackVCardHelper.FIELD_STATUS);
					
					// 4. save new contact into db
					ProviderUtils.addNewContact(context, jid, nickname, status);
					
					return Response.success(new UserProfile(nickname, jid, status, UserProfile.TYPE_CONTACT));
				}
			} catch(SmackInvocationException e) {
				return Response.error(e);
			} finally {
				cursor.close();
			} 
		}
		
		return null;
	}
}