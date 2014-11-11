package com.mstr.letschat.service;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.mstr.letschat.model.ChatMessage;
import com.mstr.letschat.utils.UserUtils;
import com.mstr.letschat.xmpp.XMPPHelper;

public class MessageService extends Service implements PacketListener {
	private static final String LOG_TAG = "MessageService";
	
	private Looper serviceLooper;
	private ServiceHandler serviceHandler;
	
	public static final String ACTION_CONNECT = "com.mstr.letschat.intent.action.CONNECT";
	public static final String ACTION_POST_LOGIN = "com.mstr.letschat.intent.action.POST_LOGIN";
	public static final String ACTION_MESSAGE_RECEIVED = "com.mstr.letschat.intent.action.MESSAGE_RECEIVED";
	
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
			
			if (action.equals(ACTION_POST_LOGIN)) {
				handlePostLoginMessage(intent);
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
			if (XMPPHelper.login(user, password)) {
				XMPPHelper.addPacketListener(this);
			}
		}
	}
	
	private void handlePostLoginMessage(Intent intent) {
		XMPPHelper.addPacketListener(this);
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
	}
}