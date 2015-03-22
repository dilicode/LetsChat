package com.mstr.letschat.tasks;

import org.jivesoftware.smackx.vcardtemp.packet.VCard;

import android.content.Context;

import com.mstr.letschat.SmackInvocationException;
import com.mstr.letschat.model.UserProfile;
import com.mstr.letschat.tasks.Response.Listener;
import com.mstr.letschat.xmpp.SmackHelper;

public class LoadProfileTask extends BaseAsyncTask<Void, Void, UserProfile> {
	public LoadProfileTask(Listener<UserProfile> listener, Context context) {
		super(listener, context);
	}
	
	@Override
	protected Response<UserProfile> doInBackground(Void... params) {
		Context context = getContext();
		if (context != null) {
			try {
				VCard vcard = SmackHelper.getInstance(context).loadVCard();
				
				return Response.success(new UserProfile(null, vcard));
			} catch (SmackInvocationException e) {
				return Response.error(e);
			}
		}
		
		return null;
	}
}
