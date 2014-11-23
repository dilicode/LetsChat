package com.mstr.letschat;

import java.util.LinkedList;
import java.util.List;

import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;
import android.view.View;

import com.mstr.letschat.adapters.ContactRequestListAdapter;
import com.mstr.letschat.adapters.ContactRequestListAdapter.OnAcceptButtonClickListener;
import com.mstr.letschat.model.ContactRequest;
import com.mstr.letschat.service.MessageService;
import com.mstr.letschat.tasks.AcceptContactRequestTask;
import com.mstr.letschat.tasks.QueryReceivedContactRequestsTask;
import com.mstr.letschat.tasks.QueryReceivedContactRequestsTask.QueryReceivedContactRequestsListener;

public class ReceivedContactRequestListActivity extends ListActivity 
		implements QueryReceivedContactRequestsListener,
		OnAcceptButtonClickListener {
	
	private LinkedList<ContactRequest> requests = new LinkedList<ContactRequest>();
	
	private ContactRequestListAdapter adapter;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		getActionBar().setHomeButtonEnabled(true);
	
		adapter = new ContactRequestListAdapter(this, requests);
		setListAdapter(adapter);
		adapter.setListener(this);
		
		new QueryReceivedContactRequestsTask(this, this).execute();
	}
	
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		IntentFilter filter = new IntentFilter(MessageService.ACTION_PRESENCE_RECEIVED);
		filter.setPriority(10);
		registerReceiver(receiver, filter);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		
		unregisterReceiver(receiver);
	}
	
	@Override
	public void onRequestsReturned(List<ContactRequest> result) {
		if (result != null) {
			requests.addAll(result);
			adapter.notifyDataSetChanged();
		}
	}
	
	public ContactRequest getContactRequest(int position) {
		return requests.get(position);
	}
	
	@Override
	public void onAcceptButtonClick(View view, int position) {
		new AcceptContactRequestTask(this, position).execute();
		
	}
	
	private BroadcastReceiver receiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action != null && action.equals(MessageService.ACTION_PRESENCE_RECEIVED)) {
				requests.addFirst((ContactRequest)intent.getParcelableExtra(MessageService.EXTRA_DATA_NAME_JID));
				adapter.notifyDataSetChanged();
				
				abortBroadcast();
			}
		}
	};
}