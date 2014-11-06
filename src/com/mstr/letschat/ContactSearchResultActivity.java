package com.mstr.letschat;

import java.util.ArrayList;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.MenuItem;

import com.mstr.letschat.adapters.ContactSearchResultAdapter;
import com.mstr.letschat.model.ContactSearchResult;

public class ContactSearchResultActivity extends ListActivity {
	public static final String EXTRA_DATA_NAME_CONTACT_LIST = "com.mstr.letschat.ContactList";
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		ArrayList<ContactSearchResult> list = getIntent().getExtras().getParcelableArrayList(EXTRA_DATA_NAME_CONTACT_LIST);
		
		setListAdapter(new ContactSearchResultAdapter(this, list));
		
		getActionBar().setDisplayHomeAsUpEnabled(true);
	}
	
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		}
		
		return super.onOptionsItemSelected(item);
	}
}