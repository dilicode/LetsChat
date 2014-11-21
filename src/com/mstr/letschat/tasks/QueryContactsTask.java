package com.mstr.letschat.tasks;

import java.lang.ref.WeakReference;
import java.util.List;

import android.content.Context;
import android.os.AsyncTask;

import com.mstr.letschat.databases.ContactTableHelper;
import com.mstr.letschat.model.Contact;

public class QueryContactsTask extends AsyncTask<Void, Void, List<Contact>> {
	public static interface QueryContactsListener {
		public void onContactsReturnd(List<Contact> result);
	}
	
	private WeakReference<QueryContactsListener> listenerWrapper;
	private WeakReference<Context> contextWrapper;
	
	public QueryContactsTask(QueryContactsListener listener, Context context) {
		listenerWrapper = new WeakReference<QueryContactsListener>(listener);
		contextWrapper = new WeakReference<Context>(context);
	}
	
	@Override
	protected List<Contact> doInBackground(Void... params) {
		Context context = contextWrapper.get();
		if (context != null) {
			return ContactTableHelper.getInstance(context).query();
		}
		
		return null;
	}
	
	public void onPostExecute(List<Contact> result) {
		QueryContactsListener listener = listenerWrapper.get();
		if (listener != null) {
			listener.onContactsReturnd(result);
		}
	}
}