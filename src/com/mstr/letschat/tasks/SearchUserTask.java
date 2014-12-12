package com.mstr.letschat.tasks;

import java.lang.ref.WeakReference;

import org.jivesoftware.smack.util.StringUtils;

import android.content.Context;

import com.mstr.letschat.SmackInvocationException;
import com.mstr.letschat.databases.ContactTableHelper;
import com.mstr.letschat.model.UserProfile;
import com.mstr.letschat.tasks.Response.Listener;
import com.mstr.letschat.utils.UserUtils;
import com.mstr.letschat.xmpp.XMPPHelper;

public class SearchUserTask extends BaseAsyncTask<Void, Void, UserProfile> {
	private WeakReference<Context> contextWrapper;
	private String username;
	
	public SearchUserTask(Listener<UserProfile> listener, Context context, String username) {
		super(listener);
		
		contextWrapper = new WeakReference<Context>(context);
		this.username = username;
	}

	@Override
	protected Response<UserProfile> doInBackground(Void... params) {
		Context context = contextWrapper.get();
		if (context != null) {
			try {
				UserProfile user = XMPPHelper.getInstance().searchByCompleteUsername(username);
				
				if (user != null) {
					if (user.getUserName().equals(UserUtils.getUser(context))) {
						user.setStatus(UserProfile.STATUS_MYSELF);
					} else if (ContactTableHelper.getInstance(context).isContact(user.getJid())) {
						user.setStatus(UserProfile.STATUS_CONTACT);
					} else {
						user.setStatus(UserProfile.STATUS_NOT_CONTACT);
					}
				}
				return Response.success(user);
			} catch(SmackInvocationException e) {
				return Response.error(e);
			}
		} else {
			return null;
		}
	}
	
	@Override
	protected void onPostExecute(Response<UserProfile> response) {
		Listener<UserProfile> listener = getListener();
		
		if (listener != null && response != null) {
			if (response.isSuccess()) {
				listener.onResponse(response.getResult());
			} else {
				listener.onErrorResponse(response.getException());
			}
		}
	}
}