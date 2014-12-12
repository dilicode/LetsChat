package com.mstr.letschat;

import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;

public class ChatHistoryActivity extends Activity {
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.chat_history_menu, menu);
		
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_add:
			startActivity(new Intent(this, SearchUserActivity.class));
			
			return true;
		case R.id.action_new_chat:
			startActivity(new Intent(this, ContactListActivity.class));
			
			return true;
		}
		
		return super.onOptionsItemSelected(item);
	}
}