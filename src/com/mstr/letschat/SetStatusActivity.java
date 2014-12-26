package com.mstr.letschat;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class SetStatusActivity extends Activity {
	private TextView statusText;
	private ListView listView;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_set_status);
		
		statusText = (TextView)findViewById(R.id.tv_status);
		listView = (ListView)findViewById(R.id.status_list);
		
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, getStatusOptions());
		listView.setAdapter(adapter);
		
		getActionBar().setHomeButtonEnabled(true);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	private List<String> getStatusOptions() {
		List<String> list = new ArrayList<String>();
		list.add(getString(R.string.status_available));
		list.add(getString(R.string.status_busy));
		list.add(getString(R.string.status_at_school));
		list.add(getString(R.string.status_at_work));
		list.add(getString(R.string.status_in_a_meeting));
		
		return list;
	}
}