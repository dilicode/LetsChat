package com.mstr.letschat.tasks;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import android.os.AsyncTask;

import com.mstr.letschat.model.UserSearchResult;
import com.mstr.letschat.xmpp.XMPPHelper;

public class SearchUserTask extends AsyncTask<Void, Void, ArrayList<UserSearchResult>> {
	public static interface SearchUserListener {
		public void onSearchResult(ArrayList<UserSearchResult> result);
	}
	
	private WeakReference<SearchUserListener> listener;
	private String username;
	
	public SearchUserTask(SearchUserListener listener, String username) {
		this.listener = new WeakReference<SearchUserListener>(listener);
		this.username = username;
	}

	@Override
	protected ArrayList<UserSearchResult> doInBackground(Void... params) {
		return XMPPHelper.search(username);
	}
	
	@Override
	protected void onPostExecute(ArrayList<UserSearchResult> result) {
		super.onPostExecute(result);
		
		SearchUserListener l = listener.get();
		if (l != null) {
			l.onSearchResult(result);
		}
	}
}