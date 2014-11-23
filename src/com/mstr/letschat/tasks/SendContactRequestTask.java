package com.mstr.letschat.tasks;

import java.lang.ref.WeakReference;

import android.app.Activity;
import android.os.AsyncTask;
import android.widget.Toast;

import com.mstr.letschat.R;
import com.mstr.letschat.UserSearchResultActivity;
import com.mstr.letschat.databases.ContactTableHelper;
import com.mstr.letschat.model.Contact;
import com.mstr.letschat.model.UserSearchResult;
import com.mstr.letschat.xmpp.XMPPContactHelper;

public class SendContactRequestTask extends AsyncTask<Void, Void, Integer> {
	public static final int REQUEST_SENT = 1;
	public static final int SENDING_REQUEST_ERROR = 2;
	public static final int CONTACT_ALREADY_ADDED = 3;
	
	private WeakReference<UserSearchResultActivity> activityWrapper;
	private int position;
	
	public SendContactRequestTask(UserSearchResultActivity activity, int position) {
		this.activityWrapper = new WeakReference<UserSearchResultActivity>(activity);
		this.position = position;
	}
	
	@Override
	protected Integer doInBackground(Void... params) {
		UserSearchResultActivity activity = activityWrapper.get();
		int result = 0;
		if (activity != null) {
			UserSearchResult user = activity.getUser(position);
			
			Contact contact = ContactTableHelper.getInstance(activity).queryByJid(user.getJid());
			if (contact != null) {
				result = CONTACT_ALREADY_ADDED;
			} else {
				if (XMPPContactHelper.getInstance().requestSubscription(user.getJid(), user.getNickname())) {
					result = REQUEST_SENT;
				} else {
					result = SENDING_REQUEST_ERROR;
				}
			}
		}
		
		return result;
	}
	
	@Override
	protected void onPostExecute(Integer result) {
		Activity activity = activityWrapper.get();
		if (activity != null) {
			switch (result) {
			case REQUEST_SENT:
				Toast.makeText(activity, R.string.contact_request_sent_message, Toast.LENGTH_SHORT).show();
				break;
				
			case SENDING_REQUEST_ERROR:
				Toast.makeText(activity, R.string.sending_contact_request_error_message, Toast.LENGTH_SHORT).show();
				break;
			}
		}
	}
}