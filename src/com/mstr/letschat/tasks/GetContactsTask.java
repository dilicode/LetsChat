package com.mstr.letschat.tasks;

import java.lang.ref.WeakReference;
import java.util.List;

import android.content.Context;
import android.os.AsyncTask;

import com.mstr.letschat.databases.ContactTableHelper;
import com.mstr.letschat.model.Contact;

public class GetContactsTask extends AsyncTask<Void, Void, List<Contact>> {
	public static interface GetContactsListener {
		public void onContactsObtained(List<Contact> result);
	}
	
	private WeakReference<GetContactsListener> listenerWrapper;
	private WeakReference<Context> contextWrapper;
	
	public GetContactsTask(GetContactsListener listener, Context context) {
		listenerWrapper = new WeakReference<GetContactsListener>(listener);
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
		GetContactsListener listener = listenerWrapper.get();
		if (listener != null) {
			listener.onContactsObtained(result);
		}
	}
}