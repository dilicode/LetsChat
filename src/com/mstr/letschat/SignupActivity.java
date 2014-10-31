package com.mstr.letschat;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.mstr.letschat.tasks.AddAccountTask;
import com.mstr.letschat.tasks.AddAccountTask.AccountCreationResult;
import com.mstr.letschat.tasks.AddAccountTask.AddAccountListener;

public class SignupActivity extends Activity implements OnClickListener, AddAccountListener {
	private EditText nicknameText;
	private EditText cellPhoneNumberText;
	private EditText passwordText;
	
	private Button submitButton;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_signup);
		
		nicknameText = (EditText)findViewById(R.id.et_nickname);
		cellPhoneNumberText = (EditText)findViewById(R.id.et_cell_phone_number);
		passwordText = (EditText)findViewById(R.id.et_password);
		
		submitButton = (Button)findViewById(R.id.btn_submit);
		submitButton.setOnClickListener(this);
	}
	
	@Override
	public void onClick(View v) {
		if (v == submitButton) {
			new AddAccountTask(this, nicknameText.getText().toString(), passwordText.getText().toString()).execute();
		}
	}

	@Override
	public void onAccountAdded(AccountCreationResult result) {
	}
}
