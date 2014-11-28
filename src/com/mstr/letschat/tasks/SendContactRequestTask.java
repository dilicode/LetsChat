package com.mstr.letschat.tasks;

import java.lang.ref.WeakReference;

import android.content.Context;

import com.mstr.letschat.SmackInvocationException;
import com.mstr.letschat.model.UserSearchResult;
import com.mstr.letschat.tasks.Response.Listener;
import com.mstr.letschat.utils.UserUtils;
import com.mstr.letschat.xmpp.XMPPContactHelper;

public class SendContactRequestTask extends BaseAsyncTask<Void, Void, Boolean> {
	private WeakReference<Context> contextWrapper;
	private WeakReference<UserSearchResult> userWrapper;
	
	public SendContactRequestTask(Listener<Boolean> listener, Context context, UserSearchResult user) {
		super(listener);
		
		contextWrapper = new WeakReference<Context>(context);
		userWrapper = new WeakReference<UserSearchResult>(user);
	}
	
	@Override
	protected Response<Boolean> doInBackground(Void... params) {
		Context context = contextWrapper.get();
		UserSearchResult user = userWrapper.get();
		if (context != null && user != null) {
			try {
				XMPPContactHelper.getInstance().requestSubscription(user.getJid(), UserUtils.getUser(context));
				
				return Response.success(true);
			} catch (SmackInvocationException e) {
				return Response.error(e);
			}
		} else {
			return null;
		}
	}
	
	@Override
	protected void onPostExecute(Response<Boolean> response) {
		Listener<Boolean> listener = getListener();
		
		if (listener != null && response != null) {
			if (response.isSuccess()) {
				listener.onResponse(response.getResult());
			} else {
				listener.onErrorResponse(response.getException());
			}
		}
	}
}