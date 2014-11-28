package com.mstr.letschat.tasks;

import com.mstr.letschat.SmackInvocationException;
import com.mstr.letschat.tasks.Response.Listener;
import com.mstr.letschat.xmpp.XMPPHelper;

public class SendChatMessageTask extends BaseAsyncTask<Void, Void, Boolean> {
	private String to;
	private String body;
	
	public SendChatMessageTask(Listener<Boolean> listener, String to, String body) {
		super(listener);
		
		this.to = to;
		this.body = body;
	}
	
	@Override
	public Response<Boolean> doInBackground(Void... params) {
		try {
			XMPPHelper.getInstance().sendChatMessage(to, body);
			
			return Response.success(true);
		} catch(SmackInvocationException e) {
			return Response.error(e);
		}
	}
	
	@Override
	public void onPostExecute(Response<Boolean> response) {
		Listener<Boolean> listener = getListener();
		
		if (listener != null) {
			if (response.isSuccess()) {
				listener.onResponse(response.getResult());
			} else {
				listener.onErrorResponse(response.getException());
			}
		}
	}
}