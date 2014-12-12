package com.mstr.letschat.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.mstr.letschat.service.MessageService;

public class NetworkReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		ConnectivityManager conn = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = conn.getActiveNetworkInfo();
		
		Intent i = new Intent(MessageService.ACTION_NETWORK_STATUS, null, context, MessageService.class);
		i.putExtra(MessageService.EXTRA_DATA_NAME_NETWORK_CONNECTED, (networkInfo != null && networkInfo.isConnected()));
		context.startService(i);
	}
}