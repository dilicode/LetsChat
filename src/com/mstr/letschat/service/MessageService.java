package com.mstr.letschat.service;

import java.util.ArrayList;
import java.util.List;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;

public class MessageService extends Service {
	private Messenger messenger;
	private List<Messenger> clients = new ArrayList<Messenger>();
	
	public static final int MSG_REGISTER_CLIENT = 1;
	public static final int MSG_UNREGISTER_CLIENT = 2;
	
	private final class ServiceHandler extends Handler {
		public ServiceHandler(Looper looper) {
			super(looper);
		}
		
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_REGISTER_CLIENT:
				clients.add(msg.replyTo);
				break;
			case MSG_UNREGISTER_CLIENT:
				clients.remove(msg.replyTo);
				break;
			}
		}
	}
	
	@Override
	public void onCreate() {
		HandlerThread thread = new HandlerThread("MessageService");
		thread.start();
		
		messenger = new Messenger(new ServiceHandler(thread.getLooper()));
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return messenger.getBinder();
	}
}