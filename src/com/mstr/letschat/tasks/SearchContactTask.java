package com.mstr.letschat.tasks;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import android.os.AsyncTask;

import com.mstr.letschat.model.Contact;
import com.mstr.letschat.utils.XMPPUtils;

public class SearchContactTask extends AsyncTask<Void, Void, ArrayList<Contact>> {
	public static interface SearchContactListener {
		public void onSearchResult(ArrayList<Contact> result);
	}
	
	private WeakReference<SearchContactListener> listener;
	private String username;
	
	public SearchContactTask(SearchContactListener listener, String username) {
		this.listener = new WeakReference<SearchContactListener>(listener);
		this.username = username;
	}

	@Override
	protected ArrayList<Contact> doInBackground(Void... params) {
		return XMPPUtils.search(username);
	}
	
	@Override
	protected void onPostExecute(ArrayList<Contact> result) {
		super.onPostExecute(result);
		
		SearchContactListener l = listener.get();
		if (l != null) {
			l.onSearchResult(result);
		}
	}
}