package com.mstr.letschat.fragments;

import android.app.Fragment;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mstr.letschat.R;
import com.mstr.letschat.SmackInvocationException;
import com.mstr.letschat.model.UserProfile;
import com.mstr.letschat.tasks.LoadProfileTask;
import com.mstr.letschat.tasks.Response.Listener;

public class ProfileFragment extends Fragment implements Listener<UserProfile> {
	private ImageView image;
	private TextView nickname;
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_profile, container, false);
		image = (ImageView)view.findViewById(R.id.avatar);
		nickname = (TextView)view.findViewById(R.id.nickname);
		
		new LoadProfileTask(this, getActivity()).execute();
		
		return view;
	}
	
	@Override
	public void onResponse(UserProfile profile) {
		byte[] avatar = profile.getAvatar();
		if (avatar != null) {
			image.setImageBitmap(BitmapFactory.decodeByteArray(avatar, 0, avatar.length));
		} else {
			image.setImageResource(R.drawable.ic_default_avatar);
		}
		nickname.setText(profile.getNickname());
	}
	
	@Override
	public void onErrorResponse(SmackInvocationException exception) {}
}
