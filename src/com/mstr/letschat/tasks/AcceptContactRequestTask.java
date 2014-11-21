package com.mstr.letschat.tasks;

import java.lang.ref.WeakReference;

import android.os.AsyncTask;

import com.mstr.letschat.ReceivedContactRequestListActivity;
import com.mstr.letschat.model.ContactRequest;
import com.mstr.letschat.xmpp.XMPPContactHelper;

public class AcceptContactRequestTask extends AsyncTask<Void, Void, Boolean> {
	private WeakReference<ReceivedContactRequestListActivity> activityWrapper;
	private int position;
	
	public AcceptContactRequestTask(ReceivedContactRequestListActivity activity, int position) {
		activityWrapper = new WeakReference<ReceivedContactRequestListActivity>(activity);
		this.position = position;
	}
	
	@Override
	protected Boolean doInBackground(Void... params) {
		Boolean result = false;
		ReceivedContactRequestListActivity activity = activityWrapper.get();
		if (activity != null) {
			ContactRequest request = activity.getContactRequest(position);
		
			// 1. grant subscription to initiator
			result = XMPPContactHelper.getInstance().grantSubscription(request.getJid());
			
			// 2. request permission to initiator
			result = XMPPContactHelper.getInstance().requestSubscription(request.getJid(), request.getNickname());
		}
		
		return result;
	}
}
