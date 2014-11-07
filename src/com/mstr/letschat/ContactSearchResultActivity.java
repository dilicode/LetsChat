package com.mstr.letschat;

import java.util.ArrayList;

import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.mstr.letschat.adapters.ContactSearchResultAdapter;
import com.mstr.letschat.adapters.ContactSearchResultAdapter.OnAddButtonClickListener;
import com.mstr.letschat.model.ContactSearchResult;
import com.mstr.letschat.service.MessageService;
import com.mstr.letschat.service.MessageService.LocalBinder;
import com.mstr.letschat.tasks.AddContactTask;

public class ContactSearchResultActivity extends ListActivity implements OnAddButtonClickListener {
	public static final String LOG_TAG = "ContactSearchResultActivity";
	
	public static final String EXTRA_DATA_NAME_CONTACT_LIST = "com.mstr.letschat.ContactList";
	
	private ContactSearchResultAdapter adapter;
	
	private MessageService messageService;
	private boolean bound;
	
	private ServiceConnection connection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			messageService = ((LocalBinder)service).getService();
			bound = true;
			
			Log.d(LOG_TAG, "service connected, componentName: " + name);
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			bound = false;
			
			Log.d(LOG_TAG, "service disconnected, componentName: " + name);
		}
	};
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		ArrayList<ContactSearchResult> list = getIntent().getExtras().getParcelableArrayList(EXTRA_DATA_NAME_CONTACT_LIST);
		adapter = new ContactSearchResultAdapter(this, list);
		setListAdapter(adapter);
		
		getActionBar().setDisplayHomeAsUpEnabled(true);
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		
		bindService(new Intent(this, MessageService.class), connection, Context.BIND_AUTO_CREATE);
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		
		if (bound) {
			unbindService(connection);
			bound = false;
		}
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
	public void onAddButtonClick(int position, View v) {
		new AddContactTask(this, position).execute();
	}

	public void onContactAdded(int position) {
		((ContactSearchResult)adapter.getItem(position)).setAdded(true);
		
		adapter.notifyDataSetChanged();
	}
}