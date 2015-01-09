package com.mstr.letschat.tasks;

import android.content.Context;

import com.mstr.letschat.SmackInvocationException;
import com.mstr.letschat.tasks.Response.Listener;
import com.mstr.letschat.utils.AppLog;
import com.mstr.letschat.xmpp.SmackHelper;

public class GetStatusTask extends BaseAsyncTask<Void, Void, String> {
	public GetStatusTask(Listener<String> listener, Context context) {
		super(listener, context);
	}
	
	@Override
	protected Response<String> doInBackground(Void... params) {
		Context context = getContext();
		if (context != null) {
			try {
				return Response.success(SmackHelper.getInstance(context).getStatus());
			} catch (SmackInvocationException e) {
				AppLog.e(String.format("get login user status error %s", e.toString()), e);
				
				return Response.error(e);
			}
		} else {
			return null;
		}
	}
}