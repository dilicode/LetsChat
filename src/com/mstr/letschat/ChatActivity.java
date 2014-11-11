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
import com.mstr.letschat.service.MessageService;
import com.mstr.letschat.tasks.SendChatMessageTask;
import com.mstr.letschat.tasks.SendChatMessageTask.SendChatMessageListener;

public class ChatActivity extends Activity implements OnClickListener, SendChatMessageListener {
	public static final String EXTRA_DATA_NAME_TO_JID = "com.mstr.letschat.ToJid";
	
	private EditText messageText;
	private ListView messageListView;
	
	private String toJid;
	
	private ArrayAdapter<String> adapter;
	
	private BroadcastReceiver messageReceiver;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		toJid = getIntent().getStringExtra(EXTRA_DATA_NAME_TO_JID);
		
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
	
	@Override
	public void onChatMessageSent(boolean result, String body) {
		if (result) {
			Toast.makeText(this, "success: " + body, Toast.LENGTH_SHORT).show();
			adapter.add(body);
		} else {
			Toast.makeText(this, "failure: " + body, Toast.LENGTH_SHORT).show();
		}
	}

	public void onClick(View v) {
		if (v.getId() == R.id.btn_send) {
			new SendChatMessageTask(ChatActivity.this, toJid, messageText.getText().toString()).execute();
		}
	}}