package com.mstr.letschat;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.mstr.letschat.tasks.SendChatMessageTask;
import com.mstr.letschat.tasks.SendChatMessageTask.SendChatMessageListener;

public class ChatActivity extends Activity implements SendChatMessageListener {
	public static final String EXTRA_DATA_NAME_TO_USER = "com.mstr.letschat.ToUser";
	
	private EditText messageText;
	private ListView messageListView;
	
	private String toUser;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_chat);
	
		messageText = (EditText)findViewById(R.id.et_message);
		findViewById(R.id.btn_send).setOnClickListener(onSendButtonClickListener);
		messageListView = (ListView)findViewById(R.id.message_list);
	
		toUser = getIntent().getStringExtra(EXTRA_DATA_NAME_TO_USER);
	}
	
	private OnClickListener onSendButtonClickListener = new OnClickListener() {
		public void onClick(View v) {
			new SendChatMessageTask(ChatActivity.this, toUser, messageText.getText().toString()).execute();
		}
	};


	@Override
	public void onChatMessageSent(boolean result) {
		Toast.makeText(this, "chat sent: " + result, Toast.LENGTH_SHORT).show();
		
	}}