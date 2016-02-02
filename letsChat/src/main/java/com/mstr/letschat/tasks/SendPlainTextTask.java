package com.mstr.letschat.tasks;

import android.content.ContentValues;
import android.content.Context;

import com.mstr.letschat.SmackInvocationException;
import com.mstr.letschat.databases.ChatMessageTableHelper;
import com.mstr.letschat.tasks.Response.Listener;
import com.mstr.letschat.xmpp.SmackHelper;

public class SendPlainTextTask extends SendMessageTask {
	public SendPlainTextTask(Listener<Boolean> listener, Context context, String to, String nickname, String body) {
		super(listener, context, to, nickname, body);
	}
	
	@Override
	protected ContentValues newMessage(long timeMillis) {
		return ChatMessageTableHelper.newPlainTextMessage(to, body, timeMillis, true);
	}

	@Override
	protected void doSend(Context context) throws SmackInvocationException {
		SmackHelper.getInstance(context).sendChatMessage(to, body, null);
	}
}