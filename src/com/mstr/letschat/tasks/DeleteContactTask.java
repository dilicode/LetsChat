package com.mstr.letschat.tasks;

import java.lang.ref.WeakReference;

import android.content.Context;

import com.mstr.letschat.SmackInvocationException;
import com.mstr.letschat.tasks.Response.Listener;
import com.mstr.letschat.xmpp.SmackHelper;

public class DeleteContactTask extends BaseAsyncTask<Void, Void, Boolean> {
	private WeakReference<Context> contextWrapper;
	private String jid;
	
	public DeleteContactTask(Listener<Boolean> listener, Context context, String jid) {
		super(listener);
		
		contextWrapper = new WeakReference<Context>(context);
		this.jid = jid;
	}
	
	@Override
	protected Response<Boolean> doInBackground(Void... params) {
		Context context = contextWrapper.get();
		
		if (context != null) {
			try {
				SmackHelper.getInstance(context).delete(jid);
				
				//ContactTableHelper.getInstance(context).delete(jid);
				
				return Response.success(true);
			} catch(SmackInvocationException e) {
				return Response.error(e);
			}
		} else {
			return null;
		}
	}
	
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