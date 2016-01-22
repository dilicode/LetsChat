package com.mstr.letschat;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.mstr.letschat.tasks.LoginTask;
import com.mstr.letschat.tasks.Response.Listener;

public class LoginActivity extends AppCompatActivity implements Listener<Boolean>, OnClickListener {
	private EditText phoneNumberText;
	private EditText passwordText;
	private Button loginButton;

	private LoginTask loginTask;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_login);
		
		phoneNumberText = (EditText)findViewById(R.id.et_phone_number);
		passwordText = (EditText)findViewById(R.id.et_password);
		loginButton = (Button)findViewById(R.id.btn_login);
		
		loginButton.setOnClickListener(this);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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

	@Override
	public void onClick(View v) {
		if (v == loginButton) {
			loginTask = new LoginTask(this, this, phoneNumberText.getText().toString(), passwordText.getText().toString());
			loginTask.execute();
		}
	}

	@Override
	public void onResponse(Boolean response) {
		if (response) {
			startActivity(new Intent(this, MainActivity.class));

			setResult(RESULT_OK);
			finish();
		}
	}

	@Override
	public void onErrorResponse(SmackInvocationException exception) {
		if (exception.isCausedBySASLError()) {
			Toast.makeText(this, R.string.invalid_credentials, Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(this, R.string.login_error, Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		if (loginTask != null) {
			loginTask.dismissDialogAndCancel();
		}
	}
}