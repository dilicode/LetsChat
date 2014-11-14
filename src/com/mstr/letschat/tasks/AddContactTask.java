package com.mstr.letschat.tasks;

import java.lang.ref.WeakReference;

import android.os.AsyncTask;

import com.mstr.letschat.UserSearchResultActivity;
import com.mstr.letschat.databases.ContactTableHelper;
import com.mstr.letschat.model.UserSearchResult;
import com.mstr.letschat.xmpp.XMPPContactHelper;

public class AddContactTask extends AsyncTask<Void, Void, Boolean> {
	private WeakReference<UserSearchResultActivity> activityWrapper;
	private int position;
	
	public AddContactTask(UserSearchResultActivity activity, int position) {
		this.activityWrapper = new WeakReference<UserSearchResultActivity>(activity);
		this.position = position;
	}
	
	@Override
	protected Boolean doInBackground(Void... params) {
		UserSearchResultActivity activity = activityWrapper.get();
		if (activity != null) {
			UserSearchResult user = activity.getUser(position);
			
			return XMPPContactHelper.getInstance().addContact(user) && 
					ContactTableHelper.getInstance(activity).insert(user);
		}
		
		return false;
	}
	
	protected void onPostExecute(Boolean result) {
		UserSearchResultActivity activity = activityWrapper.get();
		if (activity != null) {
			activity.onContactAdded(position);
		}
	}
}