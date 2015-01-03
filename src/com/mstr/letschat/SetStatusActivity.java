package com.mstr.letschat;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.mstr.letschat.adapters.StatusListAdapter;
import com.mstr.letschat.tasks.GetStatusTask;
import com.mstr.letschat.tasks.Response.Listener;

public class SetStatusActivity extends Activity {
	private TextView statusText;
	private ProgressBar progressBar;
	private ListView listView;
	private StatusListAdapter adapter;
	
	private Listener<String> getStatusListener = new Listener<String>() {
		@Override
		public void onResponse(String result) {
			hideProgressBar();
			statusText.setText(result);
		}

		@Override
		public void onErrorResponse(SmackInvocationException exception) {
			hideProgressBar();
		}
	};
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_set_status);
		
		statusText = (TextView)findViewById(R.id.tv_status);
		progressBar = (ProgressBar)findViewById(R.id.get_status_progress);
		listView = (ListView)findViewById(R.id.status_list);
		
		adapter = new StatusListAdapter(this, getStatusOptions());
		listView.setAdapter(adapter);
		
		adapter.setSelection(1);
		
		new GetStatusTask(getStatusListener, this).execute();
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
	
	private void hideProgressBar() {
		progressBar.setVisibility(View.INVISIBLE);
	}
}