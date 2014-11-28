package com.mstr.letschat.tasks;

import java.lang.ref.WeakReference;

import android.content.Context;

import com.mstr.letschat.SmackInvocationException;
import com.mstr.letschat.databases.ContactRequestTableHelper;
import com.mstr.letschat.databases.ContactTableHelper;
import com.mstr.letschat.model.Contact;
import com.mstr.letschat.model.ContactRequest;
import com.mstr.letschat.tasks.Response.Listener;
import com.mstr.letschat.xmpp.XMPPContactHelper;

public class AcceptContactRequestTask extends BaseAsyncTask<Void, Void, Contact> {
	private WeakReference<Context> contextWrapper;
	private WeakReference<ContactRequest> requestWrapper;
	
	public AcceptContactRequestTask(Listener<Contact> listener, Context context, ContactRequest request) {
		super(listener);
		contextWrapper = new WeakReference<Context>(context);
		requestWrapper = new WeakReference<ContactRequest>(request);
	}
	
	@Override
	protected Response<Contact> doInBackground(Void... params) {
		ContactRequest request = requestWrapper.get();
		Context context = contextWrapper.get();
		if (request != null && context != null) {
			try {
				String origin = request.getOrigin();
				String nickname = request.getNickname();
				
				// 1. grant subscription to initiator
				XMPPContactHelper.getInstance().grantSubscription(origin);
				
				// 2. request permission to initiator
				XMPPContactHelper.getInstance().requestSubscription(origin);
				
				// 3. notify server its nickname
				XMPPContactHelper.getInstance().setName(origin, request.getNickname());
				
				// 4. update request status in db as accepted
				ContactRequestTableHelper.getInstance(context).updateAsAccepted(request.getOrigin());
				
				// 5. save new contact to db
				Contact contact = new Contact(origin, nickname);
				ContactTableHelper.getInstance(context).insert(contact);
				
				return Response.success(contact);
			} catch(SmackInvocationException e) {
				return Response.error(e);
			}
		} else {
			return null;
		}
	}
	
	@Override
	protected void onPostExecute(Response<Contact> response) {
		Listener<Contact> listener = getListener();
		
		if (listener != null && response != null) {
			if (response.isSuccess()) {
				listener.onResponse(response.getResult());
			} else {
				listener.onErrorResponse(response.getException());
			}
		}
	}
}