package com.mstr.letschat;

import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.AsyncQueryHandler;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.mstr.letschat.adapters.ConversationCursorAdapter;
import com.mstr.letschat.databases.ChatContract.ConversationTable;

public class ConversationActivity extends ListActivity implements LoaderManager.LoaderCallbacks<Cursor> {
	private ConversationCursorAdapter adapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_conversation);
		
		adapter = new ConversationCursorAdapter(this, null, 0);
		setListAdapter(adapter);
		
		getLoaderManager().initLoader(0, null, this);
	}
	
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.conversation_menu, menu);
		
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_add:
			startActivity(new Intent(this, SearchUserActivity.class));
			return true;
			
		case R.id.action_new_chat:
			startActivity(new Intent(this, ContactListActivity.class));
			return true;
			
		case R.id.action_set_status:
			startActivity(new Intent(this, SetStatusActivity.class));
			return true;
		}
		
		return super.onOptionsItemSelected(item);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		String[] projection = new String[] {
			ConversationTable._ID,
			ConversationTable.COLUMN_NAME_NAME,
			ConversationTable.COLUMN_NAME_NICKNAME,
			ConversationTable.COLUMN_NAME_TIME,
			ConversationTable.COLUMN_NAME_LATEST_MESSAGE,
			ConversationTable.COLUMN_NAME_UNREAD
		};
		return new CursorLoader(this, ConversationTable.CONTENT_URI, projection, null, null, null);
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
	protected void onListItemClick(ListView l, View v, int position, long id) {
		new ConversationQueryHandler(this).startQuery(0, null, ConversationTable.CONTENT_URI,
				new String[]{ConversationTable.COLUMN_NAME_NAME, ConversationTable.COLUMN_NAME_NICKNAME},
				ConversationTable._ID + " = ?", new String[]{String.valueOf(id)}, null);
	}
	
	private static final class ConversationQueryHandler extends AsyncQueryHandler {
		private ConversationActivity activity;
		
		public ConversationQueryHandler(ConversationActivity activity) {
			super(activity.getContentResolver());
			this.activity = activity;
		}
		
		@Override
		protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
			if (cursor.moveToFirst()) {
				String to = cursor.getString(cursor.getColumnIndex(ConversationTable.COLUMN_NAME_NAME));
				String nickname = cursor.getString(cursor.getColumnIndex(ConversationTable.COLUMN_NAME_NICKNAME));
				
				cursor.close();
				
				Intent intent = new Intent(activity, ChatActivity.class);
				intent.putExtra(ChatActivity.EXTRA_DATA_NAME_TO, to);
				intent.putExtra(ChatActivity.EXTRA_DATA_NAME_NICKNAME, nickname);
				activity.startActivity(intent);
			}
		}
	}
}