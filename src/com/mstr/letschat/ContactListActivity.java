package com.mstr.letschat;

import java.util.List;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;

import com.mstr.letschat.model.Contact;
import com.mstr.letschat.tasks.QueryContactsTask;
import com.mstr.letschat.tasks.QueryContactsTask.QueryContactsListener;

public class ContactListActivity extends ListActivity implements OnQueryTextListener, QueryContactsListener {
	private ArrayAdapter<Contact> adapter;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		getListView().addHeaderView(getHeaderView());
		
		adapter = new ArrayAdapter<Contact>(this, android.R.layout.simple_list_item_1, android.R.id.text1);
		setListAdapter(adapter);
		
		new QueryContactsTask(this, this).execute();
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
	public void onContactsReturnd(List<Contact> result) {
		adapter.addAll(result);
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		if (position == 0) {
			// header view is clicked
			startActivity(new Intent(ContactListActivity.this, ReceivedContactRequestListActivity.class));
		} else {
			Intent intent = new Intent(this, ChatActivity.class);
			intent.putExtra(ChatActivity.EXTRA_DATA_NAME_TO_JID, ((Contact)adapter.getItem(position)).getJid());
			startActivity(intent);
		}
	}
	
	private View getHeaderView() {
		return LayoutInflater.from(this).inflate(R.layout.contact_list_header, null);
	}
}