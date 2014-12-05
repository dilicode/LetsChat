package com.mstr.letschat.tasks;

import java.lang.ref.WeakReference;

import android.content.Context;

import com.mstr.letschat.SmackInvocationException;
import com.mstr.letschat.tasks.Response.Listener;
import com.mstr.letschat.utils.UserUtils;
import com.mstr.letschat.xmpp.XMPPContactHelper;

public class SendContactRequestTask extends BaseAsyncTask<Void, Void, Boolean> {
	private WeakReference<Context> contextWrapper;
	private WeakReference<String> toWrapper;
	
	public SendContactRequestTask(Listener<Boolean> listener, Context context, String to) {
		super(listener);
		
		contextWrapper = new WeakReference<Context>(context);
		toWrapper = new WeakReference<String>(to);
	}
	
	@Override
	protected Response<Boolean> doInBackground(Void... params) {
		Context context = contextWrapper.get();
		String to = toWrapper.get();
		if (context != null && to != null) {
			try {
				XMPPContactHelper.getInstance().requestSubscription(to, UserUtils.getNickname(context), true);
				
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