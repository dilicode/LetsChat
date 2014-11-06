package com.mstr.letschat;

import android.app.Activity;
import android.os.Bundle;
import android.os.Debug;
import android.widget.Toast;

import com.mstr.letschat.tasks.SetupAccountTask;
import com.mstr.letschat.tasks.SetupAccountTask.SetupAccountListener;

public class AccountSetupActivity extends Activity implements SetupAccountListener {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		
		new SetupAccountTask(this, this, "admin").execute();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
	}

	@Override
	public void onAccountSetup() {
		Toast.makeText(this, "account setup finished", Toast.LENGTH_SHORT).show();
	}
}