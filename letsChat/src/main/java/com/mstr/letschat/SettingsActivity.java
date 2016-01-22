package com.mstr.letschat;

import android.os.Bundle;
import android.view.MenuItem;

import java.util.List;

public class SettingsActivity extends AppCompatPreferenceActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}
	
	@Override
	public void onBuildHeaders(List<Header> target) {
		loadHeadersFromResource(R.xml.preference_headers, target);
	}
	
	@Override
	protected boolean isValidFragment(String fragmentName) {
		return true;
    }


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				onBackPressed();
				return true;
		}

		return super.onOptionsItemSelected(item);
	}
}