package com.mstr.letschat.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.mstr.letschat.service.MessageService;

public class NetworkReceiver extends BroadcastReceiver {
	public static final String EXTRA_DATA_NAME_NETWORK_CONNECTED = "com.mstr.letschat.NetworkConnected";
	
	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)) {
			ConnectivityManager conn = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo networkInfo = conn.getActiveNetworkInfo();
			
			Intent i = new Intent(MessageService.ACTION_NETWORK_STATUS, null, context, MessageService.class);
			i.putExtra(EXTRA_DATA_NAME_NETWORK_CONNECTED, (networkInfo != null && networkInfo.isConnected()));
			context.startService(i);
		}
	}
}