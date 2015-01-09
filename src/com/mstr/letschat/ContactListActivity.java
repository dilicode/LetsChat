package com.mstr.letschat;

import android.annotation.SuppressLint;
import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;

import com.mstr.letschat.adapters.ContactCursorAdapter;
import com.mstr.letschat.databases.ChatContract.ContactTable;

public class ContactListActivity extends ListActivity 
	implements OnQueryTextListener, LoaderManager.LoaderCallbacks<Cursor> {
	
	private ContactCursorAdapter adapter;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		getListView().addHeaderView(getHeaderView());
		
		adapter = new ContactCursorAdapter(this, null, 0);
		setListAdapter(adapter);
		
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		getLoaderManager().initLoader(0, null, this);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.contact_list_menu, menu);
		SearchView searchView = (SearchView)menu.findItem(R.id.action_search).getActionView();
		searchView.setOnQueryTextListener(this);
		
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
			
		case R.id.action_add:
			startActivity(new Intent(this, SearchUserActivity.class));
			
			return true;
			
		case R.id.action_search:
			return true;
		}
		
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onQueryTextSubmit(String query) {
		return false;
	}

	@Override
	public boolean onQueryTextChange(String newText) {
		return false;
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		if (position == 0) {
			// header view is clicked
			startActivity(new Intent(this, ContactRequestListActivity.class));
		} else {
			Cursor cursor = (Cursor)adapter.getItem(position - 1);
			
			Intent intent = new Intent(this, ChatActivity.class);
			intent.putExtra(ChatActivity.EXTRA_DATA_NAME_TO, cursor.getString(cursor.getColumnIndex(ContactTable.COLUMN_NAME_JID)));
			intent.putExtra(ChatActivity.EXTRA_DATA_NAME_NICKNAME, cursor.getString(cursor.getColumnIndex(ContactTable.COLUMN_NAME_NICKNAME)));
			startActivity(intent);
		}
	}
	
	@SuppressLint("InflateParams")
	private View getHeaderView() {
		return LayoutInflater.from(this).inflate(R.layout.contact_list_header, null);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		String[] projection = new String[] {
				ContactTable._ID,
				ContactTable.COLUMN_NAME_JID,
				ContactTable.COLUMN_NAME_NICKNAME,
				ContactTable.COLUMN_NAME_STATUS
				};
		return new CursorLoader(this, ContactTable.CONTENT_URI, projection, null, null, null);
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