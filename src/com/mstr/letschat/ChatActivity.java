package com.mstr.letschat;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.mstr.letschat.adapters.MessageCursorAdapter;
import com.mstr.letschat.databases.ChatContract.ChatMessageTable;
import com.mstr.letschat.tasks.Response.Listener;
import com.mstr.letschat.tasks.SendChatMessageTask;

public class ChatActivity extends Activity
		implements OnClickListener, Listener<Boolean>, LoaderManager.LoaderCallbacks<Cursor>,
		TextWatcher {
	public static final String EXTRA_DATA_NAME_TO = "com.mstr.letschat.To";
	public static final String EXTRA_DATA_NAME_NICKNAME = "com.mstr.letschat.Nickname";
	
	private String to;
	
	private EditText messageText;
	private Button sendButton;
	private ListView messageListView;
	
	private MessageCursorAdapter adapter;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		to = getIntent().getStringExtra(EXTRA_DATA_NAME_TO);
		
		setContentView(R.layout.activity_chat);
	
		messageText = (EditText)findViewById(R.id.et_message);
		messageText.addTextChangedListener(this);
		
		sendButton = (Button)findViewById(R.id.btn_send);
		sendButton.setOnClickListener(this);
		
		messageListView = (ListView)findViewById(R.id.message_list);
		adapter = new MessageCursorAdapter(this, null, 0);
		messageListView.setAdapter(adapter);
		
		setTitle(getIntent().getStringExtra(EXTRA_DATA_NAME_NICKNAME));
		
		getLoaderManager().initLoader(0, null, this);
	}
	
	public void onClick(View v) {
		if (v.getId() == R.id.btn_send) {
			new SendChatMessageTask(this, this, to, getMessage()).execute();
		}
	}

	private String getMessage() {
		return messageText.getText().toString();
	}
	
	@Override
	public void onResponse(Boolean result) {
		if (result) {
			Toast.makeText(this, "success", Toast.LENGTH_SHORT).show();
			clearText();
		}
	}

	@Override
	public void onErrorResponse(SmackInvocationException exception) {
		Toast.makeText(this, "error", Toast.LENGTH_SHORT).show();
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