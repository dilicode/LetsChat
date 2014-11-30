package com.mstr.letschat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.mstr.letschat.model.Contact;
import com.mstr.letschat.service.MessageService;

public class ContactProfileActivity extends Activity implements OnClickListener {
	private Contact contact;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//contact = getIntent().getParcelableExtra(MessageService.EXTRA_DATA_NAME_CONTACT);
		
		setContentView(R.layout.activity_user_profile);
		findViewById(R.id.btn_send_message).setOnClickListener(this);
		((TextView)findViewById(R.id.tv_nickname)).setText(contact.getNickname());
		
		getActionBar().setHomeButtonEnabled(true);
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_send_message:
			Intent intent = new Intent(this, ChatActivity.class);
			//intent.putExtra(MessageService.EXTRA_DATA_NAME_CONTACT, contact);
			startActivity(intent);
			
			break;
		}
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			break;
			
		case R.id.action_delete:
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.contact_profile_menu, menu);
		
		return true;
	}
}