package com.mstr.letschat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.mstr.letschat.tasks.LoginTask;
import com.mstr.letschat.tasks.LoginTask.LoginListener;

public class LoginActivity extends Activity implements LoginListener {
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		new LoginTask(this, "admin", "admin").execute();
	}

	@Override
	public void onLogin(boolean result) {
		if (result) {
			Intent intent = new Intent(this, ChatActivity.class);
			intent.putExtra(ChatActivity.EXTRA_DATA_NAME_TO_USER, "admin");
			startActivity(intent);
			finish();
		}
	}
}
