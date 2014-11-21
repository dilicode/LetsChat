package com.mstr.letschat;

import java.util.List;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.mstr.letschat.adapters.UserSearchResultAdapter;
import com.mstr.letschat.adapters.UserSearchResultAdapter.OnAddButtonClickListener;
import com.mstr.letschat.model.UserSearchResult;
import com.mstr.letschat.tasks.SendContactRequestTask;

public class UserSearchResultActivity extends ListActivity implements OnAddButtonClickListener {
	public static final String LOG_TAG = "ContactSearchResultActivity";
	
	public static final String EXTRA_DATA_NAME_USER_SEARCH_RESULT = "com.mstr.letschat.UserSearchResult";
	
	private List<UserSearchResult> users;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		users = getIntent().getExtras().getParcelableArrayList(EXTRA_DATA_NAME_USER_SEARCH_RESULT);
		
		UserSearchResultAdapter adapter = new UserSearchResultAdapter(this, users);
		adapter.setAddButtonListener(this);
		setListAdapter(adapter);
		
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
	
	public void onContactRequestSent(Boolean result) {
		if (result) {
			Toast.makeText(this, R.string.contact_request_sent_message, Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(this, R.string.sending_contact_request_error_message, Toast.LENGTH_SHORT).show();
		}
	}
	
	public UserSearchResult getUser(int position) {
		return users.get(position);
	}
	
	@Override
	public void onAddButtonClick(int position, View v) {
		new SendContactRequestTask(this, position).execute();
	}
}