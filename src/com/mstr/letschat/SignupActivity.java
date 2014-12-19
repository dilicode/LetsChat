package com.mstr.letschat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.mstr.letschat.tasks.Response.Listener;
import com.mstr.letschat.tasks.SignupTask;

public class SignupActivity extends Activity implements OnClickListener, Listener<Boolean> {
	private EditText nameText;
	private EditText phoneNumberText;
	private EditText passwordText;
	
	private Button submitButton;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_signup);
		
		nameText = (EditText)findViewById(R.id.et_name);
		phoneNumberText = (EditText)findViewById(R.id.et_phone_number);
		passwordText = (EditText)findViewById(R.id.et_password);
		
		submitButton = (Button)findViewById(R.id.btn_submit);
		submitButton.setOnClickListener(this);
	}
	
	@Override
	public void onClick(View v) {
		if (v == submitButton) {
			new SignupTask(this, this, phoneNumberText.getText().toString(), passwordText.getText().toString(), nameText.getText().toString()).execute();
		}
	}
	
	@Override
	public void onResponse(Boolean result) {
		if (result) {
			Toast.makeText(SignupActivity.this, R.string.login_success, Toast.LENGTH_SHORT).show();
			
			startActivity(new Intent(SignupActivity.this, ChatHistoryActivity.class));
		}
	}

	@Override
	public void onErrorResponse(SmackInvocationException exception) {
		Toast.makeText(SignupActivity.this, R.string.create_account_error, Toast.LENGTH_SHORT).show();
	}
}