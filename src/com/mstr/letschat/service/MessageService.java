package com.mstr.letschat.service;

import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.util.StringUtils;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import com.mstr.letschat.SmackInvocationException;
import com.mstr.letschat.databases.ContactRequestTableHelper;
import com.mstr.letschat.databases.ContactTableHelper;
import com.mstr.letschat.model.Contact;
import com.mstr.letschat.model.ContactRequest;
import com.mstr.letschat.utils.UserUtils;
import com.mstr.letschat.xmpp.XMPPContactHelper;
import com.mstr.letschat.xmpp.XMPPHelper;

public class MessageService extends Service {
	private static final String LOG_TAG = "MessageService";
	
	private Looper serviceLooper;
	private ServiceHandler serviceHandler;
	
	public static final String EXTRA_DATA_NAME_CONTACT_REQUEST = "com.mstr.letschat.ContactRequest";
	public static final String EXTRA_DATA_NAME_CONTACT = "com.mstr.letschat.Contact";
	
	// Service Actions
	public static final String ACTION_CONNECT = "com.mstr.letschat.intent.action.CONNECT";
	public static final String ACTION_MESSAGE_RECEIVED = "com.mstr.letschat.intent.action.MESSAGE_RECEIVED";
	public static final String ACTION_PRESENCE_RECEIVED = "com.mstr.letschat.intent.action.PRESENCE_RECEIVED";
	
	// Broadcast Actions
	public static final String ACTION_NEW_CONTACT_REQUEST = "com.mstr.letschat.intent.action.NEW_CONTACT_REQUEST";
	public static final String ACTION_NEW_CONTACT = "com.mstr.letschat.intent.action.NEW_CONTACT";
	
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
				XMPPHelper.getInstance().connectAndLogin(StringUtils.parseName(user), password);
			} else {
				XMPPHelper.getInstance().connect();
			}
		} catch(SmackInvocationException e) {
			Log.e(LOG_TAG, String.format("connect error, %s", e.toString()));
		}
	}
	
	private void handlePrensencePacket(Intent intent) {
		String from = intent.getStringExtra(XMPPContactHelper.EXTRA_DATA_NAME_FROM);
		String origin = intent.getStringExtra(XMPPContactHelper.EXTRA_DATA_NAME_ORIGIN);
		int presenceTypeValue = intent.getIntExtra(XMPPContactHelper.EXTRA_DATA_NAME_PRESENCE_TYPE, 0);
		Presence.Type type = Presence.Type.values()[presenceTypeValue];
		
		Log.d(LOG_TAG, String.format("type %s from %s origin %s", type.name(), from, origin));
		
		switch (type) {
		case subscribe:
			processSubscribe(from, origin);
			
			break;
			
		default:
			break;
		}
	}
	
	private void processSubscribe(String from, String origin) {
		// get nick name
		String nickname = XMPPHelper.getInstance().getNickname(StringUtils.parseName(from));
		
		// this is a request sent from new user asking for permission
		if (!UserUtils.isLoginUser(this, origin)) {
			ContactRequest request = new ContactRequest(origin, nickname);
			// save request to db if not existing to avoid multiple requests from same user
			//ContactRequestTableHelper.getInstance(this).insertIfNonExisting(request);
			ContactRequestTableHelper.getInstance(this).insert(request);
			
			// send ordered broadcast that a new contact request is received
			Intent receiverIntent = new Intent(ACTION_NEW_CONTACT_REQUEST);
			receiverIntent.putExtra(EXTRA_DATA_NAME_CONTACT_REQUEST, request);
			receiverIntent.setPackage(getPackageName());
			sendOrderedBroadcast(receiverIntent, null);
		} else {
			try {
				// this is a request sent back to initiator, directly accept
				XMPPContactHelper.getInstance().grantSubscription(from, origin);
				
				// notify server its nickname
				XMPPContactHelper.getInstance().setName(from, nickname);
				
				// save new contact to db
				Contact contact = new Contact(from, nickname);
				ContactTableHelper.getInstance(this).insert(contact);
				
				// send ordered broadcast that a new contact is added
				Intent intent = new Intent(ACTION_NEW_CONTACT);
				intent.putExtra(EXTRA_DATA_NAME_CONTACT, contact);
				intent.setPackage(getPackageName());
				sendOrderedBroadcast(intent,null);
			} catch (SmackInvocationException e) {}
		}
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