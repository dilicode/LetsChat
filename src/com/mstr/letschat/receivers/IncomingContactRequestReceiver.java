package com.mstr.letschat.receivers;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import com.mstr.letschat.ContactRequestListActivity;
import com.mstr.letschat.R;
import com.mstr.letschat.model.ContactRequest;
import com.mstr.letschat.service.MessageService;

public class IncomingContactRequestReceiver extends BroadcastReceiver {
	private static int INCOMING_CONTACT_REQUEST_NOTIFICATION_ID = 1;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		String action  = intent.getAction();
		if (action != null && action.equals(MessageService.ACTION_NEW_CONTACT_REQUEST)) {
			TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
			stackBuilder.addParentStack(ContactRequestListActivity.class);
			stackBuilder.addNextIntent(new Intent(context, ContactRequestListActivity.class));
			
			PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
			
			ContactRequest request = (ContactRequest)intent.getParcelableExtra(MessageService.EXTRA_DATA_NAME_CONTACT_REQUEST);
			NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
				.setSmallIcon(R.drawable.ic_launcher)
				.setContentTitle(context.getString(R.string.app_name))
				.setContentText(String.format("%s %s", request.getNickname(), context.getString(R.string.add_contact_text)))
				.setContentIntent(pendingIntent)
				.setAutoCancel(true);
			
			Notification notification = builder.build();
			notification.defaults = Notification.DEFAULT_ALL;
			NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
			notificationManager.notify(INCOMING_CONTACT_REQUEST_NOTIFICATION_ID, notification);	
		}
	}
}