package com.mstr.letschat;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.mstr.letschat.model.ChatMessage;
import com.mstr.letschat.model.Contact;
import com.mstr.letschat.service.MessageService;
import com.mstr.letschat.tasks.Response.Listener;
import com.mstr.letschat.tasks.SendChatMessageTask;

public class ChatActivity extends Activity implements OnClickListener, Listener<Boolean> {
	private EditText messageText;
	private ListView messageListView;
	
	private Contact contact;
	
	private ArrayAdapter<String> adapter;
	
	private BroadcastReceiver messageReceiver;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//contact = getIntent().getParcelableExtra(MessageService.EXTRA_DATA_NAME_CONTACT);
		
		setContentView(R.layout.activity_chat);
	
		messageText = (EditText)findViewById(R.id.et_message);
		findViewById(R.id.btn_send).setOnClickListener(this);
		
		messageListView = (ListView)findViewById(R.id.message_list);
		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1);
		messageListView.setAdapter(adapter);
		
		messageReceiver = new BroadcastReceiver() {
			public void onReceive(Context context, Intent intent) {
				String action = intent.getAction();
				if (action != null && action.equals(MessageService.ACTION_MESSAGE_RECEIVED)) {
					ChatMessage chatMessage = intent.getParcelableExtra("message");
					adapter.add(chatMessage.getBody());
					
					Log.d("ChatActivity", "new message: " + chatMessage.getBody());
				}
			}
		};
		
		setTitle(contact.getNickname());
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(MessageService.ACTION_MESSAGE_RECEIVED);
		LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver, intentFilter);
	}
	
	@Override
	public void onPause() {
		super.onPause();
	}
	
	public void onClick(View v) {
		if (v.getId() == R.id.btn_send) {
			new SendChatMessageTask(this, new String(contact.getJid()), getMessage()).execute();
		}
	}

	private String getMessage() {
		return messageText.getText().toString();
	}
	
	@Override
	public void onResponse(Boolean result) {
		if (result) {
			Toast.makeText(this, "success", Toast.LENGTH_SHORT).show();
			adapter.add(getMessage());
		}
	}

	@Override
	public void onErrorResponse(SmackInvocationException exception) {
		Toast.makeText(this, "error", Toast.LENGTH_SHORT).show();		
	}
}