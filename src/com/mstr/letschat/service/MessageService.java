package com.mstr.letschat.service;

import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.util.StringUtils;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import com.mstr.letschat.databases.ContactTableHelper;
import com.mstr.letschat.databases.ReceivedContactRequestTableHelper;
import com.mstr.letschat.databases.SentContactRequestTableHelper;
import com.mstr.letschat.model.Contact;
import com.mstr.letschat.model.ContactRequest;
import com.mstr.letschat.model.UserSearchResult;
import com.mstr.letschat.utils.UserUtils;
import com.mstr.letschat.xmpp.XMPPContactHelper;
import com.mstr.letschat.xmpp.XMPPHelper;

public class MessageService extends Service {
	private static final String LOG_TAG = "MessageService";
	
	private Looper serviceLooper;
	private ServiceHandler serviceHandler;
	
	public static final String EXTRA_DATA_NAME_JID = "com.mstr.letschat.Jid";
	public static final String EXTRA_DATA_NAME_CONTACT_REQUEST = "com.mstr.letschat.ContactRequest";
	public static final String EXTRA_DATA_NAME_NEW_CONTACT = "com.mstr.letschat.NewContact";
	
	// Service Actions
	public static final String ACTION_CONNECT = "com.mstr.letschat.intent.action.CONNECT";
	public static final String ACTION_MESSAGE_RECEIVED = "com.mstr.letschat.intent.action.MESSAGE_RECEIVED";
	public static final String ACTION_CONTACT_REQUEST_RECEIVED = "com.mstr.letschat.intent.action.CONTACT_REQUEST_RECEIVED";
	
	// Broadcast Actions
	public static final String ACTION_CONTACT_ADDED = "com.mstr.letschat.intent.action.CONTACT_ADDED";
	
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
			
			if (action.equals(ACTION_CONTACT_REQUEST_RECEIVED)) {
				handleContactRequestMessage(intent);
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
		Log.d(LOG_TAG, "handleStartupMessage");
		
		String user = UserUtils.getUser(this);
		String password = UserUtils.getPassword(this);
		if (user != null && password != null) {
			XMPPHelper.getInstance().connectAndLogin(user, password);
		} else {
			XMPPHelper.getInstance().connect();
		}
	}
	
	private void handleContactRequestMessage(Intent intent) {
		String jid = intent.getStringExtra(EXTRA_DATA_NAME_JID);
		int requestType = intent.getIntExtra(XMPPContactHelper.EXTRA_DATA_NAME_CONTACT_REQUEST_TYPE, 0);
		
		switch (requestType) {
		case XMPPContactHelper.CONTACT_REQUEST_TYPE_SUBSCRIBE:
			processSubscribeRequest(jid);
			break;
			
		case XMPPContactHelper.CONTACT_REQUEST_TYPE_SUBSCRIBED:
			processSubscribedRequest(jid);
			break;
			
		}
	}
	
	private void processSubscribeRequest(String jid) {
		// this is a request sent from initiator asking for permission
		if (!SentContactRequestTableHelper.getInstance(this).checkExistence(jid)) {
			// get nick name
			String username = StringUtils.parseName(jid);
			UserSearchResult userSearchResult = XMPPHelper.getInstance().searchByCompleteUsername(username);
			String nickname = userSearchResult != null ? userSearchResult.getNickname() : username;
			
			// save request to db
			ContactRequest request = new ContactRequest(jid, nickname);
			ReceivedContactRequestTableHelper.getInstance(this).insert(request);
			
			// send ordered broadcast
			Intent receiverIntent = new Intent(ACTION_CONTACT_REQUEST_RECEIVED);
			receiverIntent.putExtra(EXTRA_DATA_NAME_CONTACT_REQUEST, request);
			receiverIntent.setPackage(getPackageName());
			sendOrderedBroadcast(receiverIntent, null);
		} else { // this is a request sent back to initiator, directly accept 
			XMPPContactHelper.getInstance().grantSubscription(jid);
		}
	}
	
	private void processSubscribedRequest(String jid) {
		RosterEntry rosterEntry = XMPPContactHelper.getInstance().getEntry(jid);
		String nickname = rosterEntry.getName();
		
		Contact contact = new Contact(jid, nickname);
		// save new contact to db and send ordered broadcast
		if (ContactTableHelper.getInstance(this).insert(contact)) {
			Intent intent = new Intent(ACTION_CONTACT_ADDED);
			intent.putExtra(EXTRA_DATA_NAME_NEW_CONTACT, contact);
			intent.setPackage(getPackageName());
			
			sendOrderedBroadcast(intent,null);
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