package com.mstr.letschat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

public class StartupActivity extends Activity implements OnClickListener {
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_startup);
		
		findViewById(R.id.btn_login).setOnClickListener(this);
		findViewById(R.id.btn_signup).setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_login:
			break;
			
		case R.id.btn_signup:
			startActivity(new Intent(this, SignupActivity.class));
		}
	}
}
