package com.mstr.letschat.tasks;

import com.mstr.letschat.SmackInvocationException;
import com.mstr.letschat.tasks.Response.Listener;
import com.mstr.letschat.xmpp.XMPPHelper;

public class SignupTask extends BaseAsyncTask<Void, Void, Boolean> {
	private String user;
	private String name;
	private String password;
	
	public SignupTask(Listener<Boolean> listener, String user, String password, String name) {
		super(listener);
		
		this.user = user;
		this.name = name;
		this.password = password;
	}
	
	@Override
	public Response<Boolean> doInBackground(Void... params) {
		try {
			XMPPHelper.getInstance().signupAndLogin(user, password, name);
			
			return Response.success(true); 
		} catch(SmackInvocationException e) {
			return Response.error(e);
		}
	}
	
	@Override
	protected void onPostExecute(Response<Boolean> response) {
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