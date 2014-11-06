package com.mstr.letschat.tasks;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import android.os.AsyncTask;

import com.mstr.letschat.model.ContactSearchResult;
import com.mstr.letschat.utils.XMPPUtils;

public class SearchContactTask extends AsyncTask<Void, Void, ArrayList<ContactSearchResult>> {
	public static interface SearchContactListener {
		public void onSearchResult(ArrayList<ContactSearchResult> result);
	}
	
	private WeakReference<SearchContactListener> listener;
	private String username;
	
	public SearchContactTask(SearchContactListener listener, String username) {
		this.listener = new WeakReference<SearchContactListener>(listener);
		this.username = username;
	}

	@Override
	protected ArrayList<ContactSearchResult> doInBackground(Void... params) {
		return XMPPUtils.search(username);
	}
	
	@Override
	protected void onPostExecute(ArrayList<ContactSearchResult> result) {
		super.onPostExecute(result);
		
		SearchContactListener l = listener.get();
		if (l != null) {
			l.onSearchResult(result);
		}
	}
}