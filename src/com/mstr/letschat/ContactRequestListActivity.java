package com.mstr.letschat;

import java.util.LinkedList;

import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.example.android.apis.app.LoaderThrottle.MainTable;
import com.mstr.letschat.adapters.ContactRequestAdapter;
import com.mstr.letschat.databases.ChatContract.ContactRequestTable;
import com.mstr.letschat.model.Contact;
import com.mstr.letschat.model.ContactRequest;
import com.mstr.letschat.service.MessageService;
import com.mstr.letschat.tasks.AcceptContactRequestTask;
import com.mstr.letschat.tasks.Response.Listener;

public class ContactRequestListActivity extends ListActivity implements LoaderManager.LoaderCallbacks<Cursor> {
	
	private LinkedList<ContactRequest> requests = new LinkedList<ContactRequest>();
	
	private static final String[] PROJECTION = new String[] {
		ContactRequestTable.COLUMN_NAME_NICKNAME,
		ContactRequestTable.COLUMN_NAME_STATUS};
	
	private ContactRequestAdapter adapter;
	
	private SimpleCursorAdapter madapter;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		getActionBar().setHomeButtonEnabled(true);
		
		// Create an empty adapter we will use to display the loaded data.
		mAdapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_1, null, PROJECTION, new int[] { android.R.id.text1 }, 0);
        setListAdapter(mAdapter);

		
		
		getLoaderManager().initLoader(0, null, this);
		
		Cursor c = getContentResolver().query(ContactRequestTable.CONTENT_URI, PROJECTION, null, null, null);

        // Map Cursor columns to views defined in simple_list_item_2.xml
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,
                android.R.layout.simple_list_item_2, c,
                        new String[] {
                            Phone.TYPE,
                            Phone.NUMBER
                        },
                        new int[] { android.R.id.text1, android.R.id.text2 });
        //Used to display a readable string for the phone type
        adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                //Let the adapter handle the binding if the column is not TYPE
                if (columnIndex != COLUMN_TYPE) {
                    return false;
                }
                int type = cursor.getInt(COLUMN_TYPE);
                String label = null;
                //Custom type? Then get the custom label
                if (type == Phone.TYPE_CUSTOM) {
                    label = cursor.getString(COLUMN_LABEL);
                }
                //Get the readable string
                String text = (String) Phone.getTypeLabel(getResources(), type, label);
                //Set text
                ((TextView) view).setText(text);
                return true;
            }
        });
        setListAdapter(adapter);
		
	
		/*adapter = new ContactRequestAdapter(this, requests);
		setListAdapter(adapter);
		adapter.setListener(this);
		
		new QueryContactRequestsTask(new Listener<List<ContactRequest>>() {
			@Override
			public void onResponse(List<ContactRequest> result) {
				requests.addAll(result);
				adapter.notifyDataSetChanged();
			}

			@Override
			public void onErrorResponse(SmackInvocationException exception) {}
			
		}, this).execute();*/
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
		
		IntentFilter filter = new IntentFilter(MessageService.ACTION_NEW_CONTACT_REQUEST);
		filter.setPriority(10);
		registerReceiver(receiver, filter);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		
		unregisterReceiver(receiver);
	}
	
	@Override
	public void onAcceptButtonClick(View view, final int position) {
		new AcceptContactRequestTask(new Listener<Contact>() {
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
			
		}, this, requests.get(position)).execute();
	}
	
	private BroadcastReceiver receiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action != null && action.equals(MessageService.ACTION_NEW_CONTACT_REQUEST)) {
				requests.addFirst((ContactRequest)intent.getParcelableExtra(MessageService.EXTRA_DATA_NAME_CONTACT_REQUEST));
				adapter.notifyDataSetChanged();
				
				abortBroadcast();
			}
		}
	};

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return new CursorLoader(this, ContactRequestTable.CONTENT_URI, PROJECTION, null, null, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		// TODO Auto-generated method stub
		
	}
}