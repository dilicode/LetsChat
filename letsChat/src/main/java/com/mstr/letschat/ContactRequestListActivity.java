package com.mstr.letschat;

import android.app.LoaderManager;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.ListView;

import com.mstr.letschat.adapters.ContactRequestCursorAdapter;
import com.mstr.letschat.adapters.ContactRequestCursorAdapter.OnAcceptButtonClickListener;
import com.mstr.letschat.databases.ChatContract.ContactRequestTable;
import com.mstr.letschat.model.UserProfile;
import com.mstr.letschat.receivers.IncomingContactRequestReceiver;
import com.mstr.letschat.service.MessageService;
import com.mstr.letschat.tasks.AcceptContactRequestTask;
import com.mstr.letschat.tasks.Response.Listener;

public class ContactRequestListActivity extends AppCompatActivity
	implements LoaderManager.LoaderCallbacks<Cursor>, OnAcceptButtonClickListener {

	private ListView listView;
	
	private BroadcastReceiver receiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action != null && action.equals(MessageService.ACTION_CONTACT_REQUEST_RECEIVED)) {
				abortBroadcast();
			}
		}
	};
	
	private ContactRequestCursorAdapter adapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_contact_requests);
		listView = (ListView)findViewById(R.id.list);

		// Create an empty adapter we will use to display the loaded data.
		adapter = new ContactRequestCursorAdapter(this, null, 0);
		adapter.setOnAcceptButtonClicklistener(this);
		listView.setAdapter(adapter);
		
		listView.setItemsCanFocus(false);
		
		getLoaderManager().initLoader(0, null, this);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		IntentFilter filter = new IntentFilter(MessageService.ACTION_CONTACT_REQUEST_RECEIVED);
		filter.setPriority(10);
		registerReceiver(receiver, filter);
	
		// cancel notification if existing
		cancelNotificationIfExisting();
	}
	
	protected void onPause() {
		super.onPause();
		
		unregisterReceiver(receiver);
	}
	
	@Override
	public void onAcceptButtonClick(Uri uri) {
		new AcceptContactRequestTask(new Listener<UserProfile>() {
			@Override
			public void onResponse(UserProfile result){
				// start contact profile activity
				Intent intent = new Intent(ContactRequestListActivity.this, UserProfileActivity.class);
				intent.putExtra(UserProfileActivity.EXTRA_DATA_NAME_USER_PROFILE, result);
				startActivity(intent);
			}
			
			@Override
			public void onErrorResponse(SmackInvocationException exception) {}
			
		}, this, uri).execute();
	}
	
	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		String[] PROJECTION = new String[] {
			ContactRequestTable._ID,
			ContactRequestTable.COLUMN_NAME_JID,
			ContactRequestTable.COLUMN_NAME_NICKNAME,
			ContactRequestTable.COLUMN_NAME_STATUS};
		return new CursorLoader(this, ContactRequestTable.CONTENT_URI, PROJECTION, null, null, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		adapter.swapCursor(data);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		adapter.swapCursor(null);
	}
	
	private void cancelNotificationIfExisting() {
		((NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE)).cancel(
				IncomingContactRequestReceiver.INCOMING_CONTACT_REQUEST_NOTIFICATION_ID);
	}
}