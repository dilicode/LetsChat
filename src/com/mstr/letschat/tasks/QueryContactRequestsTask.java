package com.mstr.letschat.tasks;

import java.lang.ref.WeakReference;
import java.util.List;

import android.content.Context;

import com.mstr.letschat.databases.ContactRequestTableHelper;
import com.mstr.letschat.model.ContactRequest;
import com.mstr.letschat.tasks.Response.Listener;

public class QueryContactRequestsTask extends BaseAsyncTask<Void, Void, List<ContactRequest>> {
	private WeakReference<Context> contextWrapper;
	
	public QueryContactRequestsTask(Listener<List<ContactRequest>> listener, Context context) {
		super(listener);
		contextWrapper = new WeakReference<Context>(context);
	}
	
	@Override
	protected Response<List<ContactRequest>> doInBackground(Void... params) {
		Context context = contextWrapper.get();
		
		if (context != null) {
			return Response.success(ContactRequestTableHelper.getInstance(context).query());
		}
		
		return null;
	}
	
	@Override
	protected void onPostExecute(Response<List<ContactRequest>> response) {
		Listener<List<ContactRequest>> listener = getListener();
		
		if (listener != null && response != null) {
			if (response.isSuccess()) {
				listener.onResponse(response.getResult());
			} else {
				listener.onErrorResponse(response.getException());
			}
		}
	}
}