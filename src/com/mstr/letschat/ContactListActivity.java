package com.mstr.letschat;

import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnActionExpandListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.TextView;

import com.mstr.letschat.adapters.ContactCursorAdapter;
import com.mstr.letschat.bitmapcache.ImageFetcher;
import com.mstr.letschat.databases.ChatContract.ContactTable;

public class ContactListActivity extends ListActivity 
	implements LoaderManager.LoaderCallbacks<Cursor>,
	OnQueryTextListener, OnClickListener, OnActionExpandListener {
	
	private TextView newContactsText;
	private View contactsDivider;
	
	private ContactCursorAdapter adapter;
	private String query;
	
	private ImageFetcher imageFetcher;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_contacts);
		
		newContactsText = (TextView)findViewById(R.id.tv_new_contacts);
		newContactsText.setOnClickListener(this);
		contactsDivider = findViewById(R.id.contacts_divider);
		
		imageFetcher = ImageFetcher.getAvatarImageFetcher(this);
		
		adapter = new ContactCursorAdapter(this, null, 0);
		setListAdapter(adapter);
		
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		getLoaderManager().initLoader(0, null, this);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.contact_list_menu, menu);
		
		MenuItem searchItem = menu.findItem(R.id.action_search);
		SearchView searchView = (SearchView)searchItem.getActionView();
		searchView.setOnQueryTextListener(this);
		searchItem.setOnActionExpandListener(this);
		
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
	protected void onPause() {
		super.onPause();
		
		imageFetcher.flushCache();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		imageFetcher.closeCache();
	}
	
	@Override
	public boolean onQueryTextSubmit(String query) {
		restartLoader(query);
		
		return true;
	}

	@Override
	public boolean onQueryTextChange(String newText) {
		restartLoader(newText);
		
		return true;
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Cursor cursor = (Cursor)adapter.getItem(position);
		
		Intent intent = new Intent(this, ChatActivity.class);
		intent.putExtra(ChatActivity.EXTRA_DATA_NAME_TO, cursor.getString(cursor.getColumnIndex(ContactTable.COLUMN_NAME_JID)));
		intent.putExtra(ChatActivity.EXTRA_DATA_NAME_NICKNAME, cursor.getString(cursor.getColumnIndex(ContactTable.COLUMN_NAME_NICKNAME)));
		startActivity(intent);
	}
	
	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		String[] projection = new String[] {
				ContactTable._ID,
				ContactTable.COLUMN_NAME_JID,
				ContactTable.COLUMN_NAME_NICKNAME,
				ContactTable.COLUMN_NAME_STATUS
			};
		
		String selection = null;
		String[] selectionArgs = null;
		if (hasQueryText()) {
			selection = ContactTable.COLUMN_NAME_NICKNAME + " like ?";
			selectionArgs = new String[]{query + "%"};
		}
		
		return new CursorLoader(this, ContactTable.CONTENT_URI, projection, selection, selectionArgs, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		adapter.swapCursor(data);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		adapter.swapCursor(null);
	}
	
	private void restartLoader(String query) {
		this.query = query;
		getLoaderManager().restartLoader(0, null, this);
	}
	
	private boolean hasQueryText() {
		return query != null && !query.equals("");
	}

	@Override
	public void onClick(View v) {
		if (v == newContactsText) {
			startActivity(new Intent(this, ContactRequestListActivity.class));
		}
	}
	
	@Override
	public boolean onMenuItemActionExpand(MenuItem item) {
		newContactsText.setVisibility(View.GONE);
		contactsDivider.setVisibility(View.GONE);
		
		return true;
	}

	@Override
	public boolean onMenuItemActionCollapse(MenuItem item) {
		newContactsText.setVisibility(View.VISIBLE);
		contactsDivider.setVisibility(View.VISIBLE);
		
		if (hasQueryText()) {
			restartLoader(null);
		}
		
		return true;
	}
	
	public ImageFetcher getImageFetcher() {
		return imageFetcher;
	}
}