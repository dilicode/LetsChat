package com.mstr.letschat;

import java.util.List;

import android.preference.PreferenceActivity;

public class SettingsActivity extends PreferenceActivity {
	
	@Override
	public void onBuildHeaders(List<Header> target) {
		loadHeadersFromResource(R.layout.preference_headers, target);
	}
	
	@Override
	protected boolean isValidFragment(String fragmentName) {
		return true;
    }
}