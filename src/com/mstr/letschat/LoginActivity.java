package com.mstr.letschat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.mstr.letschat.tasks.LoginTask;
import com.mstr.letschat.tasks.Response.Listener;

public class LoginActivity extends Activity implements Listener<Boolean>, OnClickListener {
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
	public void onClick(View v) {
		if (v == loginButton) {
			new LoginTask(this, this, phoneNumberText.getText().toString(), passwordText.getText().toString()).execute();
		}
	}

	@Override
	public void onResponse(Boolean response) {
		if (response) {
			startActivity(new Intent(this, ChatHistoryActivity.class));
		}
	}

	@Override
	public void onErrorResponse(SmackInvocationException exception) {
		Toast.makeText(this, R.string.login_error, Toast.LENGTH_SHORT).show();
	}
}