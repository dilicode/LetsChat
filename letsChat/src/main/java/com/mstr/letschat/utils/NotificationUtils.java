package com.mstr.letschat.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.support.v4.app.NotificationCompat;

import com.mstr.letschat.R;

public class NotificationUtils {
	public static void notify(Context context, String title, String text, int id, PendingIntent pendingIntent) {
		NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
			.setSmallIcon(R.drawable.ic_notification)
			.setContentTitle(title)
			.setContentText(text)
			.setContentIntent(pendingIntent)
			.setAutoCancel(true);

		Notification notification = builder.build();
		notification.defaults = Notification.DEFAULT_ALL;
		NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.notify(id, notification);
	}
}