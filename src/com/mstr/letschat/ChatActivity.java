package com.mstr.letschat;

import android.app.Activity;

import android.app.LoaderManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.Bundle;
import android.os.IBinder;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.mstr.letschat.adapters.MessageCursorAdapter;
import com.mstr.letschat.databases.ChatContract.ChatMessageTable;
import com.mstr.letschat.service.MessageService;
import com.mstr.letschat.service.MessageService.LocalBinder;
import com.mstr.letschat.tasks.Response.Listener;
import com.mstr.letschat.tasks.SendChatMessageTask;

public class ChatActivity extends Activity
		implements OnClickListener, Listener<Boolean>, 
		LoaderManager.LoaderCallbacks<Cursor>, TextWatcher {
	
	private static final String LOG_TAG = "ChatActivity";
	
	public static final String EXTRA_DATA_NAME_TO = "com.mstr.letschat.To";
	public static final String EXTRA_DATA_NAME_NICKNAME = "com.mstr.letschat.Nickname";
	
	private String to;
	private String nickname;
	
	private EditText messageText;
	private Button sendButton;
	private ListView messageListView;
	
	private MessageCursorAdapter adapter;
	
	private MessageService messageService;
	private boolean bound = false;
	
	private ServiceConnection serviceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			messageService = ((LocalBinder)service).getService();
			messageService.startConversationWith(to);
			bound = true;
		}
		
		@Override
		public void onServiceDisconnected(ComponentName name) {
			bound = false;
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		to = getIntent().getStringExtra(EXTRA_DATA_NAME_TO);
		nickname = getIntent().getStringExtra(EXTRA_DATA_NAME_NICKNAME);
		
		setContentView(R.layout.activity_chat);
	
		messageText = (EditText)findViewById(R.id.et_message);
		messageText.addTextChangedListener(this);
		
		sendButton = (Button)findViewById(R.id.btn_send);
		sendButton.setOnClickListener(this);
		
		messageListView = (ListView)findViewById(R.id.message_list);
		adapter = new MessageCursorAdapter(this, null, 0);
		messageListView.setAdapter(adapter);
		
		setTitle(nickname);
		
		getLoaderManager().initLoader(0, null, this);
	}		
	
	@Override
	protected void onStart() {
		super.onStart();
		
		// bind to MessageService
		Intent intent = new Intent(this, MessageService.class);
		bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		
		if (bound) {
			if (messageService != null) {
				messageService.stopConversation();
			}
			unbindService(serviceConnection);
			bound = false;
		}
	}
	
	@Override
	public void onClick(View v) {
		if (v == sendButton) {
			new SendChatMessageTask(this, this, to, nickname, getMessage()).execute();
		}
	}
	
	private String getMessage() {
		return messageText.getText().toString();
	}
	
	@Override
	public void onResponse(Boolean result) {
		clearText();
	}

	@Override
	public void onErrorResponse(SmackInvocationException exception) {
		clearText();
	}

	private void clearText() {
		messageText.setText("");
	}
	
	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		String[] projection = new String[] {
				ChatMessageTable._ID,
				ChatMessageTable.COLUMN_NAME_MESSAGE,
				ChatMessageTable.COLUMN_NAME_TIME,
				ChatMessageTable.COLUMN_NAME_TYPE,
				ChatMessageTable.COLUMN_NAME_STATUS
		};
		return new CursorLoader(this, ChatMessageTable.CONTENT_URI, projection,
				ChatMessageTable.COLUMN_NAME_JID + "=?", new String[]{to}, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		adapter.swapCursor(data);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		adapter.swapCursor(null);
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {}

	@Override
	public void afterTextChanged(Editable s) {
		if (s.toString().trim().length() > 0) {
			sendButton.setEnabled(true);
		} else {
			sendButton.setEnabled(false);
		}
	}	
}