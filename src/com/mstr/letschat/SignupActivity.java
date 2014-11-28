package com.mstr.letschat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.mstr.letschat.tasks.CreateAccountTask;
import com.mstr.letschat.tasks.LoginTask;
import com.mstr.letschat.tasks.Response.Listener;

public class SignupActivity extends Activity implements OnClickListener {
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
			new CreateAccountTask(createAccountListener, phoneNumberText.getText().toString(), nameText.getText().toString(),
					passwordText.getText().toString()).execute();
		}
	}

	private Listener<Boolean> createAccountListener = new Listener<Boolean>() {
		@Override
		public void onResponse(Boolean result) {
			if (result) {
				new LoginTask(loginListener, SignupActivity.this, phoneNumberText.getText().toString(), passwordText.getText().toString()).execute();
			}
		}

		@Override
		public void onErrorResponse(SmackInvocationException exception) {
			Toast.makeText(SignupActivity.this, R.string.create_account_error, Toast.LENGTH_SHORT).show();
		}
	};
	
	private Listener<Boolean> loginListener = new Listener<Boolean>() {

		@Override
		public void onResponse(Boolean result) {
			if (result) {
				startActivity(new Intent(SignupActivity.this, ChatHistoryActivity.class));
			}
		}

		@Override
		public void onErrorResponse(SmackInvocationException exception) {
			Toast.makeText(SignupActivity.this, R.string.login_error, Toast.LENGTH_SHORT).show();
		}
		
	};
}