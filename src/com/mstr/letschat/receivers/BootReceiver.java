package com.mstr.letschat.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.mstr.letschat.service.MessageService;
import com.mstr.letschat.utils.UserUtils;

public class BootReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if (UserUtils.getUser(context) != null && UserUtils.getPassword(context) != null) {
			Intent serviceIntent = new Intent(context, MessageService.class);
			serviceIntent.setAction(MessageService.ACTION_CONNECT);
			
			context.startService(serviceIntent);
		}
	}
}