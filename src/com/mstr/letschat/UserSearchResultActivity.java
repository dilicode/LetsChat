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
import com.mstr.letschat.tasks.Response.Listener;

public class UserSearchResultActivity extends ListActivity implements OnAddButtonClickListener {
	public static final String LOG_TAG = "ContactSearchResultActivity";
	
	public static final String EXTRA_DATA_NAME_USER_SEARCH_RESULT = "com.mstr.letschat.UserSearchResult";
	
	private List<UserSearchResult> users;
	private UserSearchResultAdapter adapter;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		users = getIntent().getExtras().getParcelableArrayList(EXTRA_DATA_NAME_USER_SEARCH_RESULT);
		
		adapter = new UserSearchResultAdapter(this, users);
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
	
	@Override
	public void onAddButtonClick(final int position, View v) {
		new SendContactRequestTask(new Listener<Boolean>() {
			@Override
			public void onResponse(Boolean result) {
				if (result) {
					users.get(position).setStatus(UserSearchResult.STATUS_WAITING_FOR_ACCEPTANCE);
					adapter.notifyDataSetChanged();
				}
			}

			@Override
			public void onErrorResponse(SmackInvocationException exception) {
				Toast.makeText(UserSearchResultActivity.this, R.string.sending_contact_request_error_message, Toast.LENGTH_SHORT).show();	
			}
			
		}, this, users.get(position)).execute();
	}
}