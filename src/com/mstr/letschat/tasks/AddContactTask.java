package com.mstr.letschat.tasks;

import java.lang.ref.WeakReference;

import com.mstr.letschat.utils.XMPPUtils;

import android.os.AsyncTask;

public class AddContactTask extends AsyncTask<Void, Void, Boolean> {
	
	public static interface AddContactListener {
		public void onContactAdded(boolean result);
	}
	
	private WeakReference<AddContactListener> listener;
	private String user;
	private String name;
	
	public AddContactTask(AddContactListener listener, String user, String name) {
		this.listener = new WeakReference<AddContactListener>(listener);
		this.user = user;
		this.name = name;
	}
	
	@Override
	protected Boolean doInBackground(Void... params) {
		return XMPPUtils.addContact(user, name);
	}
	
	protected void onPostExecute(Boolean result) {
		AddContactListener l = listener.get();
		if (l != null) {
			l.onContactAdded(result);
		}
	}
}