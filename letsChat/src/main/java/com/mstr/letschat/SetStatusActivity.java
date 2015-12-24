package com.mstr.letschat;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.mstr.letschat.adapters.StatusListAdapter;
import com.mstr.letschat.tasks.LoadStatusTask;
import com.mstr.letschat.tasks.Response.Listener;
import com.mstr.letschat.tasks.SaveStatusTask;

public class SetStatusActivity extends AppCompatActivity implements OnItemClickListener {
	private TextView statusText;
	private ProgressBar progressBar;
	private ListView listView;
	private StatusListAdapter adapter;
	
	private Listener<String> getStatusListener = new Listener<String>() {
		@Override
		public void onResponse(String result) {
			hideProgressBar();
			statusText.setText(result);
			adapter.setSelection(result);
		}

		@Override
		public void onErrorResponse(SmackInvocationException exception) {
			hideProgressBar();
		}
	};
	
	private Listener<Boolean> setStatusListener = new Listener<Boolean>() {

		@Override
		public void onResponse(Boolean result) {}

		@Override
		public void onErrorResponse(SmackInvocationException exception) {
			Toast.makeText(SetStatusActivity.this, R.string.set_status_error, Toast.LENGTH_SHORT).show();
		}
	};
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_set_status);
		
		statusText = (TextView)findViewById(R.id.tv_status);
		progressBar = (ProgressBar)findViewById(R.id.get_status_progress);
		listView = (ListView)findViewById(R.id.status_list);
		
		adapter = new StatusListAdapter(this, getResources().getStringArray(R.array.status_items));
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(this);
		
		
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		new LoadStatusTask(getStatusListener, this).execute();
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
	
	private void hideProgressBar() {
		progressBar.setVisibility(View.INVISIBLE);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		new SaveStatusTask(setStatusListener, this, adapter, statusText, position).execute();
	}
}