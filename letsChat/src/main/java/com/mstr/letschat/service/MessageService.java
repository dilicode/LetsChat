package com.mstr.letschat.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentProviderOperation;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import com.mstr.letschat.ChatActivity;
import com.mstr.letschat.R;
import com.mstr.letschat.SmackInvocationException;
import com.mstr.letschat.databases.ChatContract.ChatMessageTable;
import com.mstr.letschat.databases.ChatContract.ContactRequestTable;
import com.mstr.letschat.databases.ChatContract.ContactTable;
import com.mstr.letschat.databases.ChatContract.ConversationTable;
import com.mstr.letschat.databases.ChatMessageTableHelper;
import com.mstr.letschat.databases.ContactRequestTableHelper;
import com.mstr.letschat.databases.ContactTableHelper;
import com.mstr.letschat.databases.ConversationTableHelper;
import com.mstr.letschat.model.SubscribeInfo;
import com.mstr.letschat.providers.DatabaseContentProvider;
import com.mstr.letschat.receivers.NetworkReceiver;
import com.mstr.letschat.utils.NotificationUtils;
import com.mstr.letschat.utils.PreferenceUtils;
import com.mstr.letschat.utils.ProviderUtils;
import com.mstr.letschat.xmpp.PresencePacketListener;
import com.mstr.letschat.xmpp.SmackHelper;
import com.mstr.letschat.xmpp.SmackVCardHelper;
import com.mstr.letschat.xmpp.UserLocation;

import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.vcardtemp.packet.VCard;

import java.util.ArrayList;

public class MessageService extends Service {
	private static final String LOG_TAG = "MessageService";
	
	private IBinder binder = new LocalBinder();
	private Looper serviceLooper;
	private ServiceHandler serviceHandler;
	
	public static final int INCOMING_MESSAGE_NOTIFICATION_ID = 2;
	
	private static final int RECONNECT_MESSAGE_WHAT = 1;
	
	public static final String EXTRA_DATA_NAME_FROM_NICKNAME = "com.mstr.letschat.FromNickname";
	public static final String EXTRA_DATA_NAME_NOTIFICATION_TEXT = "com.mstr.letschat.NotificationText";
	public static final String EXTRA_DATA_NAME_FROM = "com.mstr.letschat.From";
	public static final String EXTRA_DATA_NAME_MESSAGE_BODY = "com.mstr.letschat.MessageBody";
	public static final String EXTRA_DATA_NAME_LOCATION = "com.mstr.letschat.Location";
	
	// Service Actions
	public static final String ACTION_CONNECT = "com.mstr.letschat.intent.action.CONNECT";
	public static final String ACTION_RECONNECT = "com.mstr.letschat.intent.action.RECONNECT";
	public static final String ACTION_MESSAGE_RECEIVED = "com.mstr.letschat.intent.action.MESSAGE_RECEIVED";
	public static final String ACTION_PRESENCE_RECEIVED = "com.mstr.letschat.intent.action.PRESENCE_RECEIVED";
	public static final String ACTION_NETWORK_STATUS = "com.mstr.letschat.intent.action.NETWORK_STATUS";
	
	// Broadcast Actions
	public static final String ACTION_CONTACT_REQUEST_RECEIVED = "com.mstr.letschat.intent.action.CONTACT_REQUEST_RECEIVED";
	
	private int reconnectCount = 0;
	
	// used to specify whom I am currently talking to
	private String conversationTarget;
	
	private SmackHelper smackHelper;
	
	private NotificationManager notificationManager;
	
	public class LocalBinder extends Binder {
		public MessageService getService() {
			return MessageService.this;
		}
	}
	
	private final class ServiceHandler extends Handler {
		public ServiceHandler(Looper looper) {
			super(looper);
		}
		
		@Override
		public void handleMessage(android.os.Message msg) {
			Intent intent = (Intent)msg.obj;
			String action = intent.getAction();
			
			if (action.equals(ACTION_CONNECT)) {
				connect();
				return;
			}
			
			if (action.equals(ACTION_RECONNECT)) {
				reconnect();
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
		
		notificationManager = ((NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE));
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
		return binder;
	}
	
	public void startConversation(final String name) {
		conversationTarget = name;
		
		// cancel any notification if existing after we start a conversation
		notificationManager.cancel(INCOMING_MESSAGE_NOTIFICATION_ID);

		serviceHandler.post(new Runnable() {
			@Override
			public void run() {
				String selection = ConversationTable.COLUMN_NAME_NAME + "=?";
				String[] selectionArgs = new String[]{name};

				Cursor cursor = getContentResolver().query(ConversationTable.CONTENT_URI, new String[]{ConversationTable.COLUMN_NAME_UNREAD},
						selection, selectionArgs, null);
				if (cursor.moveToFirst()) {
					int unreadCount = cursor.getInt(cursor.getColumnIndex(ConversationTable.COLUMN_NAME_UNREAD));
					if (unreadCount > 0) {
						// clear any unread messages
						ContentValues values = new ContentValues();
						values.put(ConversationTable.COLUMN_NAME_UNREAD, 0);
						getContentResolver().update(ConversationTable.CONTENT_URI, values, selection, selectionArgs);
					}
				}
				cursor.close();
			}
		});
	}
	
	public void stopConversation() {
		conversationTarget = null;
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		smackHelper.onDestroy();
		
		serviceLooper.quit();
	}
	
	private void reconnect() {
		if (connect()) {
			reconnectCount = 0;
		}
	}
	
	private boolean connect() {
		String user = PreferenceUtils.getUser(this);
		String password = PreferenceUtils.getPassword(this);
		if (user != null && password != null) {
			try {
				smackHelper.login(user, password);
			
				return true;
			} catch(SmackInvocationException e) {
				Log.e(LOG_TAG, String.format("login error %s", user), e);
			}
		}
		
		return false;
	}

	private void handlePrensencePacket(Intent intent) {
		Presence.Type type = Presence.Type.values()[intent.getIntExtra(PresencePacketListener.EXTRA_DATA_NAME_TYPE, -1)];
		
		switch (type) {
		case subscribe:
			processSubscribePresence(intent);
			break;
			
		case available:
			processAvailablePresence(intent);
			break;
			
		default:
			break;
		}
	}
	
	private void processSubscribePresence(Intent intent) {
		String from = intent.getStringExtra(EXTRA_DATA_NAME_FROM);
		
		SubscribeInfo subInfo = null;
		try {
			subInfo = smackHelper.processSubscribe(from);
		} catch (SmackInvocationException e) {
			Log.e(LOG_TAG, String.format("process subscribe error, %s", from), e);
			return;
		}
		
		int subType = subInfo.getType();
		// this is a request sent from new user asking for permission
		if (subType == SubscribeInfo.TYPE_WAIT_FOR_APPROVAL) {
			processSubscribeFromNewUser(from, subInfo.getNickname());
		} else if (subType == SubscribeInfo.TYPE_APPROVED) { // this is a request sent back to initiator
			processApprovedSubscribe(from, subInfo.getNickname());
		}
	}
	
	private void processSubscribeFromNewUser(String from, String fromNickname) {
		// save request to db
		getContentResolver().insert(ContactRequestTable.CONTENT_URI,
				ContactRequestTableHelper.newContentValues(from, fromNickname));
		
		// send ordered broadcast that a new contact request is received
		Intent receiverIntent = new Intent(ACTION_CONTACT_REQUEST_RECEIVED);
		receiverIntent.putExtra(EXTRA_DATA_NAME_NOTIFICATION_TEXT, 
				String.format("%s %s", fromNickname, getString(R.string.add_contact_text)));
		receiverIntent.setPackage(getPackageName());
		sendOrderedBroadcast(receiverIntent, null);
	}
	
	private void processApprovedSubscribe(String from, String fromNickname) {
		VCard vCard = null;
		try {
			vCard = smackHelper.loadVCard(from);
		} catch (SmackInvocationException e) {
			Log.e(LOG_TAG, String.format("get vcard error %s", from), e);
			return;
		}
		
		// save new contact into db
		ProviderUtils.addNewContact(this, from, fromNickname, vCard.getField(SmackVCardHelper.FIELD_STATUS));
		
		// show notification that contact request has been approved
		showContactRequestApprovedNotification(from, fromNickname);
	}
	
	private void processAvailablePresence(Intent intent) {
		String from = intent.getStringExtra(EXTRA_DATA_NAME_FROM);
		String status = intent.getStringExtra(PresencePacketListener.EXTRA_DATA_NAME_STATUS);
		
		// update status 
		if (status != null) {
			String selection = ContactTable.COLUMN_NAME_JID + "=?";
			String[] selectionArgs = new String[]{from};
			Cursor cursor = getContentResolver().query(ContactTable.CONTENT_URI, new String[]{ContactTable.COLUMN_NAME_STATUS}, selection, selectionArgs, null);
			if (cursor.moveToFirst()) {
				String oldStatus = cursor.getString(cursor.getColumnIndex(ContactTable.COLUMN_NAME_STATUS));
				if (!status.equals(oldStatus)) {
					getContentResolver().update(ContactTable.CONTENT_URI, ContactTableHelper.newUpdateStatusContentValues(status), selection, selectionArgs);
				}
			}
			cursor.close();
		}
	}
	
	private void showContactRequestApprovedNotification(String from, String fromNickname) {
		PendingIntent pendingIntent = ChatActivity.getChatActivityPendingIntent(this, from, fromNickname);
		
		NotificationUtils.notify(this, fromNickname,
				getString(R.string.acceptance_text),
				INCOMING_MESSAGE_NOTIFICATION_ID, pendingIntent);
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
			reconnect();
		} else {
			// remove any pending reconnect messages if any
			serviceHandler.removeMessages(RECONNECT_MESSAGE_WHAT);
			
			smackHelper.onNetworkDisconnected();
		}
	}
	
	private void handleMessagePacket(Intent intent) {
		String from = intent.getStringExtra(EXTRA_DATA_NAME_FROM);
		String body = intent.getStringExtra(EXTRA_DATA_NAME_MESSAGE_BODY);
		UserLocation location = intent.getParcelableExtra(EXTRA_DATA_NAME_LOCATION);
		long timeMillis = System.currentTimeMillis();

		ContentValues messageValues = null;
		if (location == null) { // this is a plain text message
			messageValues = ChatMessageTableHelper.newPlainTextMessage(from, body, timeMillis, false);
		} else {
			messageValues = ChatMessageTableHelper.newLocationMessage(from, body, timeMillis, location, false);
		}

		ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();
		operations.add(ContentProviderOperation.newInsert(ChatMessageTable.CONTENT_URI).withValues(messageValues).build());

		int unreadCount = 0;
		String nickname = null;
		
		Cursor cursor = getContentResolver().query(ConversationTable.CONTENT_URI,
				new String[]{ConversationTable._ID, ConversationTable.COLUMN_NAME_UNREAD, ConversationTable.COLUMN_NAME_NICKNAME},
				ConversationTable.COLUMN_NAME_NAME + "=?", new String[]{from}, null);
		if (cursor.moveToFirst()) { // there is a conversation already
			unreadCount = cursor.getInt(cursor.getColumnIndex(ConversationTable.COLUMN_NAME_UNREAD));
			nickname = cursor.getString(cursor.getColumnIndex(ConversationTable.COLUMN_NAME_NICKNAME));
			
			Uri conversationItemUri = ContentUris.withAppendedId(ConversationTable.CONTENT_URI,
					cursor.getInt(cursor.getColumnIndex(ConversationTable._ID)));
			
			ContentValues values = ConversationTableHelper.newUpdateContentValues(body, timeMillis);
			// if not in conversation with from currently, increase unread count by 1
			if (!isInConversationWith(from)) {
				unreadCount ++;
				values.put(ConversationTable.COLUMN_NAME_UNREAD, unreadCount);
			}
			operations.add(ContentProviderOperation.newUpdate(conversationItemUri).withValues(values).build());
		} else { // insert a new conversation
			// query user nick name
			try {
				nickname = getNickname(from);
			} catch (SmackInvocationException e) {
				cursor.close();
				
				Log.e(LOG_TAG, String.format("get nickname error %s", from), e);
				
				return;
			}
			unreadCount = isInConversationWith(from) ? 0 : 1;
			
			ContentValues values = ConversationTableHelper.newInsertContentValues(from, nickname, body, timeMillis, unreadCount);
			operations.add(ContentProviderOperation.newInsert(ConversationTable.CONTENT_URI).withValues(values).build());
		}
		
		cursor.close();
		
		// commit the changes as a transaction
		try {
			getContentResolver().applyBatch(DatabaseContentProvider.AUTHORITY, operations);
		} catch (Exception e) {
			Log.e(LOG_TAG, "applybatch error", e);
			return;
		}
		
		// show notification
		if (!isInConversationWith(from)) {
			PendingIntent pendingIntent = ChatActivity.getChatActivityPendingIntent(this, from, nickname);
			String notifyText = unreadCount == 1 ? body : String.format("%s %s", unreadCount, getString(R.string.new_messages));
			
			NotificationUtils.notify(this, nickname, notifyText, INCOMING_MESSAGE_NOTIFICATION_ID, pendingIntent);
		}
	}
	
	private String getNickname(String from) throws SmackInvocationException {
		// query user nick name
		Cursor cursor = getContentResolver().query(ContactTable.CONTENT_URI, new String[]{ContactTable.COLUMN_NAME_NICKNAME}, 
				ContactTable.COLUMN_NAME_JID + "=?", new String[]{from}, null);
		String nickname = null;
		if (cursor.moveToFirst()) { // message comes from a contact
			nickname = cursor.getString(cursor.getColumnIndex(ContactTable.COLUMN_NAME_NICKNAME));
		} else { // message from a non-contact
			nickname = smackHelper.getNickname(from);
		}
		
		cursor.close();
		
		return nickname;
	}
	
	private boolean isInConversationWith(String jid) {
		return jid.equals(conversationTarget);
	}
}