package com.mstr.letschat.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;

import com.mstr.letschat.utils.XMPPUtils;

public class MessageService extends Service {
	private Looper serviceLooper;
	private ServiceHandler serviceHandler;
	
	private final IBinder binder = new LocalBinder();
	
	public static final int MSG_ADD_CONTACT = 1;
	
	public static final String KEY_USER = "user";
	public static final String KEY_NAME = "name";
	
	public final class LocalBinder extends Binder {
		public MessageService getService() {
			return MessageService.this;
		}
	}
	
	private final class ServiceHandler extends Handler {
		public ServiceHandler(Looper looper) {
			super(looper);
		}
		
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_ADD_CONTACT:
				Bundle data = msg.getData();
				String user = data.getString(KEY_USER);
				String name = data.getString(KEY_NAME);
				
				XMPPUtils.addContact(user, name);
				break;
			}
		}
	}
	
	@Override
	public void onCreate() {
		HandlerThread thread = new HandlerThread("MessageService");
		thread.start();
		
		serviceLooper = thread.getLooper();
		serviceHandler = new ServiceHandler(serviceLooper);
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}
	
	public void addContact(String user, String name) {
		Bundle data = new Bundle();
		data.putString(KEY_USER, user);
		data.putString(KEY_NAME, name);
		
		Message msg = serviceHandler.obtainMessage();
		msg.what = MSG_ADD_CONTACT;
		msg.setData(data);
		
		serviceHandler.sendMessage(msg);
	}
	
	@Override
	public void onDestroy() {
		serviceLooper.quit();
	}
}