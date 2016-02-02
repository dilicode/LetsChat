package com.mstr.letschat;

import android.content.AsyncQueryHandler;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.mstr.letschat.databases.ChatContract.ContactTable;
import com.mstr.letschat.model.UserProfile;
import com.mstr.letschat.tasks.Response.Listener;
import com.mstr.letschat.tasks.SendContactRequestTask;

import java.lang.ref.WeakReference;

public class UserProfileActivity extends AppCompatActivity implements OnClickListener {
	public static final String EXTRA_DATA_NAME_USER_PROFILE = "com.mstr.letschat.UserProfile";
	
	private UserProfile profile;
	
	private Button button;
	
	private ContentObserver contactObserver;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_user_profile);
		
		profile = getIntent().getParcelableExtra(EXTRA_DATA_NAME_USER_PROFILE);
		
		ImageView imageView = (ImageView)findViewById(R.id.avatar);
		byte[] avatar = profile.getAvatar();
		if (avatar != null) {
			Bitmap bitmap = BitmapFactory.decodeByteArray(avatar, 0, avatar.length);
			RoundedBitmapDrawable drawable = RoundedBitmapDrawableFactory.create(getResources(), bitmap);
			drawable.setCircular(true);
			imageView.setImageDrawable(drawable);
		} else {
			imageView.setImageResource(R.drawable.ic_default_avatar);
		}
		
		((TextView)findViewById(R.id.tv_nickname)).setText(profile.getNickname());
		
		button = (Button)findViewById(R.id.btn);
		button.setOnClickListener(this);
		setButtonText();
		
		String status = profile.getStatus();
		if (status != null) {
			((TextView)findViewById(R.id.tv_status)).setText(status);
		}
		
		if (profile.canAddToContact()) {
			contactObserver = new ContactContentObserver(new Handler());
			getContentResolver().registerContentObserver(ContactTable.CONTENT_URI, true, contactObserver);
		}
		
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn:
			if (profile.canAddToContact()) {
				onAddClick();
			} else {
				onSendMessageClick();
			}
			
			break;
		}
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			break;
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		if (contactObserver != null) {
			getContentResolver().unregisterContentObserver(contactObserver);
		}
	}
	
	private void setButtonText() {
		if (profile.canAddToContact()) {
			button.setText(R.string.add);
		} else {
			button.setText(R.string.send_message);
		}
	}
	
	private void onAddClick() {
		new SendContactRequestTask(new Listener<Boolean>() {
			@Override
			public void onResponse(Boolean result) {
				Toast.makeText(UserProfileActivity.this, R.string.contact_request_sent, Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onErrorResponse(Exception exception) {
				Toast.makeText(UserProfileActivity.this, R.string.sending_contact_request_error, Toast.LENGTH_SHORT).show();	
			}
			
		}, this, profile).execute();
	}
	
	private void onSendMessageClick() {
		Intent intent = new Intent(this, ChatActivity.class);
		intent.putExtra(ChatActivity.EXTRA_DATA_NAME_TO, profile.getJid());
		intent.putExtra(ChatActivity.EXTRA_DATA_NAME_NICKNAME, profile.getNickname());
		
		startActivity(intent);
	}
	
	private class ContactContentObserver extends ContentObserver {
		public ContactContentObserver(Handler handler) {
			super(handler);
		}
		
		@Override
		public void onChange(boolean selfChange) {
			onChange(selfChange, null);
		}
		
		@Override
		public void onChange(boolean selfChange, Uri uri) {
			ContactQueryHandler queryHandler = new ContactQueryHandler(UserProfileActivity.this);
			
			// Query for contact
			queryHandler.startQuery(0, null, ContactTable.CONTENT_URI, new String[]{ContactTable._ID},
					ContactTable.COLUMN_NAME_JID + " = ?", new String[]{profile.getJid()}, null);
		}
	}
	
	private static final class ContactQueryHandler extends AsyncQueryHandler {
		private WeakReference<UserProfileActivity> activityWrapper;
		
		public ContactQueryHandler(UserProfileActivity activity) {
			super(activity.getContentResolver());
			activityWrapper = new WeakReference<UserProfileActivity>(activity);
		}
		
		@Override
		protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
			UserProfileActivity activity = activityWrapper.get();
			if (activity != null && cursor.moveToFirst()) {
				activity.profile.markAsContact();
				activity.setButtonText();
				
				cursor.close();
			}
		}
	}
}