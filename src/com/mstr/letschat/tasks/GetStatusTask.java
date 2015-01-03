package com.mstr.letschat.tasks;

import java.lang.ref.WeakReference;

import android.content.Context;

import com.mstr.letschat.SmackInvocationException;
import com.mstr.letschat.tasks.Response.Listener;
import com.mstr.letschat.xmpp.SmackHelper;

public class GetStatusTask extends BaseAsyncTask<Void, Void, String> {
	private WeakReference<Context> contextWrapper;
	
	public GetStatusTask(Listener<String> listener, Context context) {
		super(listener);
		contextWrapper = new WeakReference<Context>(context);
	}
	
	@Override
	protected Response<String> doInBackground(Void... params) {
		Context context = contextWrapper.get();
		if (context != null) {
			try {
				return Response.success(SmackHelper.getInstance(context).getStatus());
			} catch (SmackInvocationException e) {
				return Response.error(e);
			}
		} else {
			return null;
		}
	}
}