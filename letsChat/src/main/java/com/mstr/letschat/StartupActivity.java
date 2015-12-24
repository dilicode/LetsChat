package com.mstr.letschat;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.mstr.letschat.tasks.LoginTask;
import com.mstr.letschat.tasks.Response.Listener;
import com.mstr.letschat.utils.PreferenceUtils;

public class StartupActivity extends AppCompatActivity implements OnClickListener, Listener<Boolean> {
	private static final int REQUEST_CODE_LOGIN = 1;
	private static final int REQUEST_CODE_SIGNUP = 2;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_startup);
		
		if (PreferenceUtils.getUser(this) != null) {
			new LoginTask(this, this, PreferenceUtils.getUser(this), PreferenceUtils.getPassword(this), false).execute();
		} else {
			findViewById(R.id.ll_buttons_container).setVisibility(View.VISIBLE);
		}
		
		findViewById(R.id.btn_login).setOnClickListener(this);
		findViewById(R.id.btn_signup).setOnClickListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.connectivity_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.action_server) {
			startActivity(new Intent(this, ServerSettingsActivity.class));
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_login:
			startActivityForResult(new Intent(this, LoginActivity.class), REQUEST_CODE_LOGIN);
			break;
			
		case R.id.btn_signup:
			startActivityForResult(new Intent(this, SignupActivity.class), REQUEST_CODE_SIGNUP);
			break;
		}
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			finish();
		}
	}

	@Override
	public void onResponse(Boolean result) {
		if (result) {
			startConversationActivity();
		}
	}

	@Override
	public void onErrorResponse(SmackInvocationException exception) {
		Toast.makeText(this, R.string.login_error, Toast.LENGTH_SHORT).show();
		startConversationActivity();
	}
	
	private void startConversationActivity() {
		startActivity(new Intent(this, ConversationActivity.class));
		finish();
	}
}