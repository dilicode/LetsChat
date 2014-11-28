package com.mstr.letschat.tasks;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import android.content.Context;

import com.mstr.letschat.SmackInvocationException;
import com.mstr.letschat.databases.ContactTableHelper;
import com.mstr.letschat.model.UserSearchResult;
import com.mstr.letschat.tasks.Response.Listener;
import com.mstr.letschat.xmpp.XMPPHelper;

public class SearchUserTask extends BaseAsyncTask<Void, Void, ArrayList<UserSearchResult>> {
	private WeakReference<Context> contextWrapper;
	private String username;
	
	public SearchUserTask(Listener<ArrayList<UserSearchResult>> listener, Context context, String username) {
		super(listener);
		
		contextWrapper = new WeakReference<Context>(context);
		this.username = username;
	}

	@Override
	protected Response<ArrayList<UserSearchResult>> doInBackground(Void... params) {
		Context context = contextWrapper.get();
		if (context != null) {
			try {
				ArrayList<UserSearchResult> result = XMPPHelper.getInstance().search(username);
				
				ContactTableHelper.getInstance(context).torename(result);
				return Response.success(result);
			} catch(SmackInvocationException e) {
				return Response.error(e);
			}
		} else {
			return null;
		}
	}
	
	@Override
	protected void onPostExecute(Response<ArrayList<UserSearchResult>> response) {
		Listener<ArrayList<UserSearchResult>> listener = getListener();
		
		if (listener != null && response != null) {
			if (response.isSuccess()) {
				listener.onResponse(response.getResult());
			} else {
				listener.onErrorResponse(response.getException());
			}
		}
	}
}