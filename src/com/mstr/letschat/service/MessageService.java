package com.mstr.letschat.service;

import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.RosterPacket.ItemType;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
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
import com.mstr.letschat.databases.ChatContract.ChatMessageTable;
import com.mstr.letschat.databases.ChatContract.ContactRequestTable;
import com.mstr.letschat.databases.ChatContract.ContactTable;
import com.mstr.letschat.databases.ChatMessageTableHelper;
import com.mstr.letschat.databases.ContactRequestTableHelper;
import com.mstr.letschat.databases.ContactTableHelper;
import com.mstr.letschat.receivers.IncomingContactRequestReceiver;
import com.mstr.letschat.receivers.NetworkReceiver;
import com.mstr.letschat.utils.UserUtils;
import com.mstr.letschat.xmpp.MessagePacketListener;
import com.mstr.letschat.xmpp.PresencePacketListener;
import com.mstr.letschat.xmpp.SmackHelper;

public class MessageService extends Service {
	private static final String LOG_TAG = "MessageService";
	
	private Looper serviceLooper;
	private ServiceHandler serviceHandler;
	
	public static final int CONTACT_REQUEST_APPROVED_NOTIFICATION_ID = 2;
	
	private static final int RECONNECT_MESSAGE_WHAT = 1;
	
	public static final String EXTRA_DATA_NAME_FROM = "com.mstr.letschat.From";
	
	// Service Actions
	public static final String ACTION_CONNECT = "com.mstr.letschat.intent.action.CONNECT";
	public static final String ACTION_RECONNECT = "com.mstr.letschat.intent.action.RECONNECT";
	public static final String ACTION_MESSAGE_RECEIVED = "com.mstr.letschat.intent.action.MESSAGE_RECEIVED";
	public static final String ACTION_PRESENCE_RECEIVED = "com.mstr.letschat.intent.action.PRESENCE_RECEIVED";
	public static final String ACTION_NETWORK_STATUS = "com.mstr.letschat.intent.action.NETWORK_STATUS";
	
	// Broadcast Actions
	public static final String ACTION_CONTACT_REQUEST_RECEIVED = "com.mstr.letschat.intent.action.CONTACT_REQUEST_RECEIVED";
	
	private int reconnectCount = 0;
	
	private SmackHelper smackHelper;
	
	private final class ServiceHandler extends Handler {
		public ServiceHandler(Looper looper) {
			super(looper);
		}
		
		@Override
		public void handleMessage(android.os.Message msg) {
			Intent intent = (Intent)msg.obj;
			String action = intent.getAction();
			
			if (action.equals(ACTION_CONNECT)) {
				connect(intent);
				return;
			}
			
			if (action.equals(ACTION_RECONNECT)) {
				reconnect(intent);
				return;
			}
			
			if (action.equals(ACTION_PRESENCE_RECEIVED)) {
				handlePrensencePacket(intent);
				return;
			}
			
			if (action.equals(ACTION_NETWORK_STATUS)) {
				handleNetworkStatus(intent);
				return;
			}
			
			if (action.equals(ACTION_MESSAGE_RECEIVED)) {
				handleMessagePacket(intent);
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
		
		smackHelper = SmackHelper.getInstance(this);
	}
	
	public int onStartCommand(Intent intent, int flags, int startId) {
		android.os.Message msg = serviceHandler.obtainMessage();
		msg.arg1 = startId;
		// null intent object is passed in when system tries to restart service after its process is killed,
		// so auto-connect in this case
		intent = intent != null ? intent : new Intent(ACTION_CONNECT);
		msg.obj = intent;
		
		String action = intent.getAction();
		if (action != null && action.equals(ACTION_RECONNECT)) {
			msg.what = RECONNECT_MESSAGE_WHAT;
			serviceHandler.sendMessageDelayed(msg, getReconnectTimeout());
		} else {
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
		
		smackHelper.onDestroy();
		
		serviceLooper.quit();
	}
	
	private void reconnect(Intent intent) {
		if (connect(intent)) {
			reconnectCount = 0;
		}
	}
	
	private boolean connect(Intent intent) {
		String user = UserUtils.getUser(this);
		String password = UserUtils.getPassword(this);
		if (user != null && password != null) {
			try {
				smackHelper.login(user, password);
			
				return true;
			} catch(SmackInvocationException e) {}
		}
		
		return false;
	}

	private void handlePrensencePacket(Intent intent) {
		Presence.Type type = Presence.Type.values()[intent.getIntExtra(PresencePacketListener.EXTRA_DATA_NAME_TYPE, -1)];
		
		switch (type) {
		case subscribe:
			processSubscribe(intent);
			break;
			
		default:
			break;
		}
	}
	
	private void processSubscribe(Intent intent) {
		String from = intent.getStringExtra(EXTRA_DATA_NAME_FROM);
		RosterEntry rosterEntry = smackHelper.getRosterEntry(from);
		ItemType rosterType = rosterEntry != null ? rosterEntry.getType() : null;
		
		// this is a request sent from new user asking for permission
		if (rosterEntry == null || rosterType == ItemType.none) {
			processSubscribeFromNewUser(from);
		} else if (rosterType == ItemType.to) { // this is a request sent back to initiator, directly approve
			processSubsequentSubscribe(from, rosterEntry.getName());
		}
	}
	
	private void processSubscribeFromNewUser(String from) {
		// get the nickname
		String fromNickname = smackHelper.getNickname(from);
		
		// save request to db
		getContentResolver().insert(ContactRequestTable.CONTENT_URI,
				ContactRequestTableHelper.newContentValues(from, fromNickname));
		
		// send ordered broadcast that a new contact request is received
		Intent receiverIntent = new Intent(ACTION_CONTACT_REQUEST_RECEIVED);
		receiverIntent.putExtra(IncomingContactRequestReceiver.EXTRA_DATA_NAME_NOTIFICATION_TEXT, 
				String.format("%s %s", fromNickname, getString(R.string.add_contact_text)));
		receiverIntent.setPackage(getPackageName());
		sendOrderedBroadcast(receiverIntent, null);
	}
	
	private void processSubsequentSubscribe(String from, String fromNickname) {
		try {
			smackHelper.approveSubscription(from);
		} catch (SmackInvocationException e) {
			Log.e(LOG_TAG, String.format("send subscribed error, %s", e.toString()));
			return;
		}
		
		// save new contact to db
		getContentResolver().insert(ContactTable.CONTENT_URI,
				ContactTableHelper.newContentValues(from, fromNickname));
		
		// show notification that contact request has been approved
		showContactRequestApprovedNotification(from, fromNickname);
		
		// are there any pending requests from sender? update them to accepted
		ContentValues values = ContactRequestTableHelper.newContentValuesWithAcceptedStatus();
		getContentResolver().update(ContactRequestTable.CONTENT_URI, values,
				ContactRequestTable.COLUMN_NAME_JID + " = ?", new String[]{from});
	}
	
	private void showContactRequestApprovedNotification(String from, String fromNickname) {
		TaskStackBuilder taskStackbuilder = TaskStackBuilder.create(this);
		taskStackbuilder.addParentStack(ChatActivity.class);
		Intent intent = new Intent(this, ChatActivity.class);
		intent.putExtra(ChatActivity.EXTRA_DATA_NAME_TO, from);
		intent.putExtra(ChatActivity.EXTRA_DATA_NAME_NICKNAME, fromNickname);
		taskStackbuilder.addNextIntent(intent);
		
		PendingIntent pendingIntent = taskStackbuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
		
		NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
			.setSmallIcon(R.drawable.ic_launcher)
			.setContentTitle(fromNickname)
			.setContentText(getString(R.string.acceptance_text))
			.setContentIntent(pendingIntent)
			.setAutoCancel(true);
		
		Notification notification = builder.build();
		notification.defaults = Notification.DEFAULT_ALL;
		((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).notify(CONTACT_REQUEST_APPROVED_NOTIFICATION_ID, notification);
	}
	
	private long getReconnectTimeout() {
		/**
		 * The reconnection mechanism will try to reconnect periodically:
		 	
			For the first minute it will attempt to connect once every ten seconds.
			For the next five minutes it will attempt to connect once a minute.
			If that fails it will indefinitely try to connect once every five minutes.
		 */
		reconnectCount ++;
		return reconnectCount <= 6 ? 10 * 1000 : (reconnectCount <= 11 ? 60 * 1000 : 5 * 60 * 1000);
	}
	
	private void handleNetworkStatus(Intent intent) {
		boolean connected = intent.getBooleanExtra(NetworkReceiver.EXTRA_DATA_NAME_NETWORK_CONNECTED, false);
		
		Log.d(LOG_TAG, String.format("network connected: %b", connected));
		
		// reconnect when network is connected
		if (connected) {
			connect(intent);
		} else {
			// remove any pending reconnect messages if any
			serviceHandler.removeMessages(RECONNECT_MESSAGE_WHAT);
			
			smackHelper.onNetworkDisconnected();
		}
	}
	
	private void handleMessagePacket(Intent intent) {
		String from = intent.getStringExtra(EXTRA_DATA_NAME_FROM);
		String body = intent.getStringExtra(MessagePacketListener.EXTRA_DATA_NAME_Message_BODY);
		
		getContentResolver().insert(ChatMessageTable.CONTENT_URI, 
				ChatMessageTableHelper.newIncomingMessageContentValues(from, body));
	}
}