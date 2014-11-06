package com.mstr.letschat;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;

public class ContactListActivity extends ListActivity implements OnQueryTextListener {
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
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
			startActivity(new Intent(this, SearchContactActivity.class));
			
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
}
