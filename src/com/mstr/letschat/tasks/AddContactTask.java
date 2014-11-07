package com.mstr.letschat.tasks;

import java.lang.ref.WeakReference;

import android.os.AsyncTask;

import com.mstr.letschat.ContactSearchResultActivity;
import com.mstr.letschat.model.ContactSearchResult;
import com.mstr.letschat.utils.XMPPUtils;

public class AddContactTask extends AsyncTask<Void, Void, Boolean> {
	private WeakReference<ContactSearchResultActivity> activityWrapper;
	private int position;
	
	public AddContactTask(ContactSearchResultActivity activity, int position) {
		this.activityWrapper = new WeakReference<ContactSearchResultActivity>(activity);
		this.position = position;
	}
	
	@Override
	protected Boolean doInBackground(Void... params) {
		ContactSearchResultActivity activity = activityWrapper.get();
		if (activity != null) {
			ContactSearchResult contact = (ContactSearchResult)activity.getListView().getItemAtPosition(position);
			return XMPPUtils.addContact(contact.getUser(), contact.getName());
		} else {
			return false;
		}
	}
	
	protected void onPostExecute(Boolean result) {
		ContactSearchResultActivity activity = activityWrapper.get();
		if (activity != null) {
			activity.onContactAdded(position);
		}
	}
}