package com.mstr.letschat.tasks;

import java.lang.ref.WeakReference;
import java.util.List;

import android.content.Context;
import android.os.AsyncTask;

import com.mstr.letschat.databases.IncomingRequestTableHelper;
import com.mstr.letschat.model.ContactRequest;

public class QueryReceivedContactRequestsTask extends AsyncTask<Void, Void, List<ContactRequest>> {
	
	public static interface QueryReceivedContactRequestsListener {
		public void onRequestsReturned(List<ContactRequest> result);
	}
	
	private WeakReference<QueryReceivedContactRequestsListener> listenerWrapper;
	private WeakReference<Context> contextWrapper;
	
	public QueryReceivedContactRequestsTask(Context context, QueryReceivedContactRequestsListener listener) {
		contextWrapper = new WeakReference<Context>(context);
		listenerWrapper = new WeakReference<QueryReceivedContactRequestsListener>(listener);
	}
	
	@Override
	protected List<ContactRequest> doInBackground(Void... params) {
		Context context = contextWrapper.get();
		
		if (context != null) {
			return IncomingRequestTableHelper.getInstance(context).query();
		}
		
		return null;
	}
	
	protected void onPostExecute(List<ContactRequest> result) {
		QueryReceivedContactRequestsListener listener = listenerWrapper.get();
		
		if (listener != null) {
			listener.onRequestsReturned(result);
		}
	}
}