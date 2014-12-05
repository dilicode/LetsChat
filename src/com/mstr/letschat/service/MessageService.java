package com.mstr.letschat.service;

import org.jivesoftware.smack.packet.Presence;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.mstr.letschat.ChatActivity;
import com.mstr.letschat.R;
import com.mstr.letschat.SmackInvocationException;
import com.mstr.letschat.databases.ChatContract.ContactRequestTable;
import com.mstr.letschat.databases.ChatContract.ContactTable;
import com.mstr.letschat.utils.DatabaseUtils;
import com.mstr.letschat.utils.UserUtils;
import com.mstr.letschat.xmpp.XMPPContactHelper;
import com.mstr.letschat.xmpp.XMPPContactHelper.PresenceExtensionData;
import com.mstr.letschat.xmpp.XMPPHelper;

public class MessageService extends Service {
	private static final String LOG_TAG = "MessageService";
	
	private Looper serviceLooper;
	private ServiceHandler serviceHandler;
	
	public static final int CONTACT_REQUEST_APPROVED_NOTIFICATION_ID = 2;
	
	public static final String EXTRA_DATA_NAME_NOTIFICATION_TEXT = "com.mstr.letschat.NotificationText";
	
	// Service Actions
	public static final String ACTION_CONNECT = "com.mstr.letschat.intent.action.CONNECT";
	public static final String ACTION_MESSAGE_RECEIVED = "com.mstr.letschat.intent.action.MESSAGE_RECEIVED";
	public static final String ACTION_PRESENCE_RECEIVED = "com.mstr.letschat.intent.action.PRESENCE_RECEIVED";
	
	// Broadcast Actions
	public static final String ACTION_CONTACT_REQUEST_RECEIVED = "com.mstr.letschat.intent.action.CONTACT_REQUEST_RECEIVED";
	
	private final class ServiceHandler extends Handler {
		public ServiceHandler(Looper looper) {
			super(looper);
		}
		
		@Override
		public void handleMessage(android.os.Message msg) {
			Intent intent = (Intent)msg.obj;
			String action = intent.getAction();
			
			if (action.equals(ACTION_CONNECT)) {
				handleConnectMessage(intent);
				return;
			}
			
			if (action.equals(ACTION_PRESENCE_RECEIVED)) {
				handlePrensencePacket(intent);
				return;
			}
		}
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		HandlerThread thread = new HandlerThread("MessageService");
		thread.start();
		
		serviceLooper = thread.getLooper();
		serviceHandler = new ServiceHandler(serviceLooper);
	}
	
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent != null) {
			android.os.Message msg = serviceHandler.obtainMessage();
			msg.arg1 = startId;
			msg.obj = intent;
			serviceHandler.sendMessage(msg);
		}
		
		return START_STICKY;
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		serviceLooper.quit();
	}
	
	private void handleConnectMessage(Intent intent) {
		String user = UserUtils.getUser(this);
		String password = UserUtils.getPassword(this);
		try {
			if (user != null && password != null) {
				XMPPHelper.getInstance().login(user, password);
			}
		} catch(SmackInvocationException e) {
			Log.e(LOG_TAG, String.format("connect error, %s", e.toString()));
		}
	}
	
	private void handlePrensencePacket(Intent intent) {
		Presence.Type type = Presence.Type.values()[intent.getIntExtra(XMPPContactHelper.EXTRA_DATA_NAME_TYPE, -1)];
		
		switch (type) {
		case subscribe:
			processSubscribe(intent);
			break;
			
		default:
			break;
		}
	}
	
	private void processSubscribe(Intent intent) {
		String from = intent.getStringExtra(XMPPContactHelper.EXTRA_DATA_NAME_FROM);
		PresenceExtensionData extensionData = (PresenceExtensionData)intent.getParcelableExtra(XMPPContactHelper.EXTRA_DATA_NAME_EXTENSION_DATA);
		String fromNickname = extensionData.getFromNickname();
		Log.d(LOG_TAG, String.format("from %s needApproval %s", from, extensionData.needApproval()));
		
		// this is a request sent from new user asking for permission
		if (extensionData.needApproval()) {
			// save request to db
			getContentResolver().insert(ContactRequestTable.CONTENT_URI,
					DatabaseUtils.newContactRequestContentValues(from, fromNickname));
			
			// send ordered broadcast that a new contact request is received
			Intent receiverIntent = new Intent(ACTION_CONTACT_REQUEST_RECEIVED);
			receiverIntent.putExtra(EXTRA_DATA_NAME_NOTIFICATION_TEXT, 
					String.format("%s %s", fromNickname, getString(R.string.add_contact_text)));
			receiverIntent.setPackage(getPackageName());
			sendOrderedBroadcast(receiverIntent, null);
		} else {
			try {
				// this is a request sent back to initiator, directly approve
				XMPPContactHelper.getInstance().approveSubscription(from);
			} catch (SmackInvocationException e) {
				Log.e(LOG_TAG, String.format("send subscribed error, %s", e.toString()));
				return;
			}
			
			// save new contact to db
			getContentResolver().insert(ContactTable.CONTENT_URI, 
					DatabaseUtils.newContactContentValues(from, fromNickname));
			
			// show notification that contact request has been approved
			showContactRequestApprovedNotification(fromNickname);
		}
	}
	
	private void showContactRequestApprovedNotification(String title) {
		TaskStackBuilder taskStackbuilder = TaskStackBuilder.create(this);
		taskStackbuilder.addParentStack(ChatActivity.class);
		Intent chatActivityIntent = new Intent(this, ChatActivity.class);
		taskStackbuilder.addNextIntent(chatActivityIntent);
		
		PendingIntent pendingIntent = taskStackbuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
		
		NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
			.setSmallIcon(R.drawable.ic_launcher)
			.setContentTitle(title)
			.setContentText(getString(R.string.acceptance_text))
			.setContentIntent(pendingIntent)
			.setAutoCancel(true);
		
		Notification notification = builder.build();
		notification.defaults = Notification.DEFAULT_ALL;
		((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).notify(CONTACT_REQUEST_APPROVED_NOTIFICATION_ID, notification);
	}
	
	/*private void handlePostLoginMessage(Intent intent) {
		XMPPHelper.getInstance().addPacketListener(this);
	}
	
	@Override
	public void processPacket(Packet packet) throws NotConnectedException {
		Message msg = (Message)packet;
		
		ChatMessage chatMessage = ChatMessage.newIncomingMessage();
		chatMessage.setJid(msg.getFrom());
		chatMessage.setBody(msg.getBody());
		
		Intent i = new Intent(ACTION_MESSAGE_RECEIVED);
		i.putExtra("message", chatMessage);
		LocalBroadcastManager.getInstance(this).sendBroadcast(i);
	}*/
}