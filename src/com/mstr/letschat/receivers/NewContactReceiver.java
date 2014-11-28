package com.mstr.letschat.receivers;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import com.mstr.letschat.ChatActivity;
import com.mstr.letschat.R;
import com.mstr.letschat.model.Contact;
import com.mstr.letschat.service.MessageService;

public class NewContactReceiver extends BroadcastReceiver {
	private static final int NEW_CONTACT_NOTIFICATION_ID = 2;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if (action != null && action.equals(MessageService.ACTION_NEW_CONTACT)) {
			Contact contact = (Contact)intent.getParcelableExtra(MessageService.EXTRA_DATA_NAME_CONTACT);
			
			TaskStackBuilder taskStackbuilder = TaskStackBuilder.create(context);
			taskStackbuilder.addParentStack(ChatActivity.class);
			Intent chatActivityIntent = new Intent(context, ChatActivity.class);
			chatActivityIntent.putExtra(MessageService.EXTRA_DATA_NAME_CONTACT, contact);
			taskStackbuilder.addNextIntent(chatActivityIntent);
			
			PendingIntent pendingIntent = taskStackbuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
			
			NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
				.setSmallIcon(R.drawable.ic_launcher)
				.setContentTitle(contact.getNickname())
				.setContentText(context.getString(R.string.acceptance_text))
				.setContentIntent(pendingIntent)
				.setAutoCancel(true);
			
			Notification notification = builder.build();
			notification.defaults = Notification.DEFAULT_ALL;
			((NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE)).notify(NEW_CONTACT_NOTIFICATION_ID, notification);
		}
	}
}