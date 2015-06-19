package com.mstr.letschat.tasks;

import java.lang.ref.WeakReference;

import android.content.Context;

import com.mstr.letschat.SmackInvocationException;
import com.mstr.letschat.model.UserProfile;
import com.mstr.letschat.tasks.Response.Listener;
import com.mstr.letschat.utils.AppLog;
import com.mstr.letschat.xmpp.SmackHelper;

public class SendContactRequestTask extends BaseAsyncTask<Void, Void, Boolean> {
	private WeakReference<UserProfile> userProfileWrapper;
	
	public SendContactRequestTask(Listener<Boolean> listener, Context context, UserProfile userProfile) {
		super(listener, context);
		
		userProfileWrapper = new WeakReference<UserProfile>(userProfile);
	}
	
	@Override
	protected Response<Boolean> doInBackground(Void... params) {
		Context context = getContext();
		UserProfile userProfile = userProfileWrapper.get();
		if (context != null && userProfile != null) {
			try {
				SmackHelper.getInstance(context).requestSubscription(userProfile.getJid(), userProfile.getNickname());
				
				return Response.success(true);
			} catch (SmackInvocationException e) {
				AppLog.e(String.format("send contact request to %s error", userProfile.getJid()), e);
				
				return Response.error(e);
			}
		} else {
			return null;
		}
	}
}