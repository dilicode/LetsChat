package com.mstr.letschat.tasks;

import java.lang.ref.WeakReference;

import android.content.Context;

import com.mstr.letschat.SmackInvocationException;
import com.mstr.letschat.databases.ChatMessageTableHelper;
import com.mstr.letschat.databases.ChatContract.ChatMessageTable;
import com.mstr.letschat.tasks.Response.Listener;
import com.mstr.letschat.xmpp.SmackHelper;

public class SendChatMessageTask extends BaseAsyncTask<Void, Void, Boolean> {
	private WeakReference<Context> contextWrapper;
	
	private String to;
	private String body;
	
	public SendChatMessageTask(Listener<Boolean> listener, Context context, String to, String body) {
		super(listener);
		
		contextWrapper = new WeakReference<Context>(context);
		this.to = to;
		this.body = body;
	}
	
	@Override
	public Response<Boolean> doInBackground(Void... params) {
		Context context = contextWrapper.get();
		if (context != null) {
			try {
				context.getContentResolver().insert(ChatMessageTable.CONTENT_URI, 
						ChatMessageTableHelper.newOutgoingMessageContentValues(to, body));
				
				SmackHelper.getInstance(context).sendChatMessage(to, body);
				
				return Response.success(true);
			} catch(SmackInvocationException e) {
				return Response.error(e);
			}
		}
		
		return null;
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