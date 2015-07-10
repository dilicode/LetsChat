package com.mstr.letschat.fragments;

import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.mstr.letschat.R;
import com.mstr.letschat.SmackInvocationException;
import com.mstr.letschat.model.LoginUserProfile;
import com.mstr.letschat.tasks.LoadProfileTask;
import com.mstr.letschat.tasks.Response.Listener;

public class ProfileFragment extends PreferenceFragment implements Listener<LoginUserProfile> {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.profile_preferences);

		new LoadProfileTask(this, getActivity()).execute();
	}

	@Override
	public void onResponse(LoginUserProfile profile) {
		if (profile != null) {
			findPreference(getString(R.string.avatar_preference)).setIcon(new BitmapDrawable(getResources(), profile.getAvatar()));
			findPreference(getString(R.string.nickname_preference)).setSummary(profile.getNickname());
		}
	}
	
	@Override
	public void onErrorResponse(SmackInvocationException exception) {}
}