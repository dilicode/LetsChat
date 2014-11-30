package com.mstr.letschat;

import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.view.MenuItem;

import com.mstr.letschat.adapters.ContactRequestListItemAdapter;
import com.mstr.letschat.adapters.ContactRequestListItemAdapter.OnAcceptButtonClickListener;
import com.mstr.letschat.databases.ChatContract.ContactRequestTable;

public class ContactRequestListActivity extends ListActivity 
	implements LoaderManager.LoaderCallbacks<Cursor>, OnAcceptButtonClickListener {
	
	private static final String[] PROJECTION = new String[] {
		ContactRequestTable._ID,
		ContactRequestTable.COLUMN_NAME_ORIGIN,
		ContactRequestTable.COLUMN_NAME_NICKNAME,
		ContactRequestTable.COLUMN_NAME_STATUS};
	
	private ContactRequestListItemAdapter adapter;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		getActionBar().setHomeButtonEnabled(true);
		
		// Create an empty adapter we will use to display the loaded data.
		adapter = new ContactRequestListItemAdapter(this, null, 0);
		setListAdapter(adapter);
		
		getLoaderManager().initLoader(0, null, this);
	}
	
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onAcceptButtonClick(String origin, String nickname) {
		/*new AcceptContactRequestTask(new Listener<Contact>() {
			@Override
			public void onResponse(Contact result) {
				requests.get(position).markAsAccepted();
				adapter.notifyDataSetChanged();
				
				// start contact profile activity
				Intent intent = new Intent(ContactRequestListActivity.this, ContactProfileActivity.class);
				intent.putExtra(MessageService.EXTRA_DATA_NAME_CONTACT, result);
				startActivity(intent);
			}
			
			@Override
			public void onErrorResponse(SmackInvocationException exception) {}
			
		}, this, null).execute();*/
	}
	
	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
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
}