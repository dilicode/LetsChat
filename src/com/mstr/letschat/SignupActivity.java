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
import com.mstr.letschat.tasks.CreateAccountTask.AccountCreationResult;
import com.mstr.letschat.tasks.CreateAccountTask.AddAccountListener;
import com.mstr.letschat.tasks.LoginTask;
import com.mstr.letschat.tasks.LoginTask.LoginListener;

public class SignupActivity extends Activity implements OnClickListener, AddAccountListener, LoginListener {
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
			new CreateAccountTask(this, phoneNumberText.getText().toString(), nameText.getText().toString(),
					passwordText.getText().toString()).execute();
		}
	}

	@Override
	public void onAccountAdded(AccountCreationResult result) {
		switch (result) {
		case SUCCESS:
			new LoginTask(this, phoneNumberText.getText().toString(), passwordText.getText().toString()).execute();
			break;
			
		case FAILURE:
			Toast.makeText(this, R.string.create_account_error, Toast.LENGTH_SHORT).show();
			break;
			
		case CONFLICT:
			Toast.makeText(this, R.string.account_already_exists, Toast.LENGTH_SHORT).show();
			break;
		}
	}

	@Override
	public void onLogin(boolean result) {
		if (result) {
			startActivity(new Intent(this, ChatHistoryActivity.class));
		} else {
			Toast.makeText(this, R.string.login_error, Toast.LENGTH_SHORT).show();
		}
		
		finish();
	}
}