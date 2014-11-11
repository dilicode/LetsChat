package com.mstr.letschat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.mstr.letschat.service.MessageService;
import com.mstr.letschat.tasks.LoginTask;
import com.mstr.letschat.tasks.LoginTask.LoginListener;
import com.mstr.letschat.utils.UserUtils;

public class LoginActivity extends Activity implements LoginListener, OnClickListener {
	private EditText phoneNumberText;
	private EditText passwordText;
	private Button loginButton;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_login);
			
		phoneNumberText = (EditText)findViewById(R.id.et_phone_number);
		passwordText = (EditText)findViewById(R.id.et_password);
		loginButton = (Button)findViewById(R.id.btn_login);
		
		loginButton.setOnClickListener(this);
	}

	@Override
	public void onLogin(boolean result) {
		if (result) {
			startActivity(new Intent(this, ChatHistoryActivity.class));
			
			startMessageService();
			
			finish();
		} else {
			Toast.makeText(this, R.string.login_error, Toast.LENGTH_SHORT).show();
		}
	}

	private void startMessageService() {
		UserUtils.setUser(this, phoneNumberText.getText().toString());
		UserUtils.setPassword(this, passwordText.getText().toString());
		
		Intent serviceIntent = new Intent(this, MessageService.class);
		serviceIntent.setAction(MessageService.ACTION_POST_LOGIN);
		startService(serviceIntent);
	}
	
	@Override
	public void onClick(View v) {
		if (v == loginButton) {
			new LoginTask(this, phoneNumberText.getText().toString(), passwordText.getText().toString()).execute();
		}
	}
}