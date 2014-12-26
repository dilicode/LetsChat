package com.mstr.letschat.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import com.mstr.letschat.ChatActivity;
import com.mstr.letschat.R;

public class NotificationUtils {
	public static void notify(Context context, String title, String text, int id, PendingIntent pendingIntent) {
		NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
			.setSmallIcon(R.drawable.ic_launcher)
			.setContentTitle(title)
			.setContentText(text)
			.setContentIntent(pendingIntent)
			.setAutoCancel(true);
		
		Notification notification = builder.build();
		notification.defaults = Notification.DEFAULT_ALL;
		NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.notify(id, notification);
	}
	
	public static PendingIntent getChatActivityPendingIntent(Context context, String to, String nickname) {
		TaskStackBuilder taskStackbuilder = TaskStackBuilder.create(context);
		taskStackbuilder.addParentStack(ChatActivity.class);
		Intent intent = new Intent(context, ChatActivity.class);
		intent.putExtra(ChatActivity.EXTRA_DATA_NAME_TO, to);
		intent.putExtra(ChatActivity.EXTRA_DATA_NAME_NICKNAME, nickname);
		taskStackbuilder.addNextIntent(intent);
		
		return taskStackbuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
	}
}