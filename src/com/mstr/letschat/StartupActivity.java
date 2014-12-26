package com.mstr.letschat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;

import com.mstr.letschat.service.MessageService;
import com.mstr.letschat.utils.UserUtils;

public class StartupActivity extends Activity implements OnClickListener {
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_startup);
		
		if (UserUtils.getUser(this) != null) {
			new Handler().postDelayed(new Runnable() {
				public void run() {
					startService(new Intent(MessageService.ACTION_CONNECT, null, StartupActivity.this, MessageService.class));
					
					startActivity(new Intent(StartupActivity.this, ConversationActivity.class));
					overridePendingTransition(0, 0);
				}
			}, 500);
		} else {
			findViewById(R.id.ll_buttons_container).setVisibility(View.VISIBLE);
		}
		
		findViewById(R.id.btn_login).setOnClickListener(this);
		findViewById(R.id.btn_signup).setOnClickListener(this);
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_login:
			startActivity(new Intent(this, LoginActivity.class));
			break;
			
		case R.id.btn_signup:
			startActivity(new Intent(this, SignupActivity.class));
			break;
		}
	}
}