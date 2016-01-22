package com.mstr.letschat;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.android.camera.CropImageIntentBuilder;
import com.mstr.letschat.tasks.Response.Listener;
import com.mstr.letschat.tasks.SignupTask;
import com.mstr.letschat.utils.AppLog;
import com.mstr.letschat.utils.FileUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class SignupActivity extends AppCompatActivity implements OnClickListener, Listener<Boolean> {
	private static final int REQUEST_CODE_SELECT_PICTURE = 1;
	private static final int REQUEST_CODE_CROP_IMAGE = 2;
	
	private static final String RAW_PHOTO_FILE_NAME = "camera.png";
	private static final String AVATAR_FILE_NAME = "avatar.png";
	
	private EditText nameText;
	private EditText phoneNumberText;
	private EditText passwordText;
	
	private Button submitButton;
	private ImageButton uploadAvatarButton;
	
	private File rawImageFile;
	private File avatarImageFile;

	private SignupTask signupTask;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_signup);
		
		nameText = (EditText)findViewById(R.id.et_name);
		phoneNumberText = (EditText)findViewById(R.id.et_phone_number);
		passwordText = (EditText)findViewById(R.id.et_password);
		uploadAvatarButton = (ImageButton)findViewById(R.id.btn_upload_avatar);
		submitButton = (Button)findViewById(R.id.btn_submit);
		
		submitButton.setOnClickListener(this);
		uploadAvatarButton.setOnClickListener(this);
		
		File dir = FileUtils.getDiskCacheDir(this, "temp");
		if (!dir.exists()) {
			dir.mkdirs();
		}
		rawImageFile = new File(dir, RAW_PHOTO_FILE_NAME);
		avatarImageFile = new File(dir, AVATAR_FILE_NAME);

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
		if (v == submitButton) {
			String phoneNumber = phoneNumberText.getText().toString();
			String password = passwordText.getText().toString();
			String name = nameText.getText().toString();
			
			if (phoneNumber.trim().length() == 0 || password.trim().length() == 0 ||
					name.trim().length() == 0) {
				Toast.makeText(this, R.string.incomplete_signup_info, Toast.LENGTH_SHORT).show();
				return;
			}

			signupTask = new SignupTask(this, this, phoneNumber, password, name, getAvatarBytes());
			signupTask.execute();
		} else if(v == uploadAvatarButton) {
			chooseAction();
		}
	}
	
	private void chooseAction() {
		Intent captureImageIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		captureImageIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(rawImageFile));
		
		Intent pickIntent = new Intent(Intent.ACTION_GET_CONTENT);
		pickIntent.setType("image/*");
		
		Intent chooserIntent = Intent.createChooser(pickIntent, getString(R.string.profile_photo));
		chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {captureImageIntent});
		
		startActivityForResult(chooserIntent, REQUEST_CODE_SELECT_PICTURE);
	}
	
	@Override
	public void onResponse(Boolean result) {
		if (result) {
			Toast.makeText(this, R.string.login_success, Toast.LENGTH_SHORT).show();
			
			startActivity(new Intent(this, MainActivity.class));

			setResult(RESULT_OK);
			finish();
		}
	}

	@Override
	public void onErrorResponse(SmackInvocationException exception) {
		Toast.makeText(this, R.string.create_account_error, Toast.LENGTH_SHORT).show();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != RESULT_OK) {
			return;
		}
		
		switch (requestCode) {
		case REQUEST_CODE_SELECT_PICTURE:
			boolean isCamera;
			if(data == null) {
				isCamera = true;
			} else {
				String action = data.getAction();
				if(action == null) {
					isCamera = false;
				} else {
					isCamera = action.equals(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
				}
			}
			
			if(isCamera) {
				startCropImage(Uri.fromFile(rawImageFile));
			} else {
				startCropImage(data == null ? null : data.getData());
			}
			
			break;
		
		case REQUEST_CODE_CROP_IMAGE:
			Bitmap bitmap = BitmapFactory.decodeFile(avatarImageFile.getAbsolutePath());
			RoundedBitmapDrawable drawable = RoundedBitmapDrawableFactory.create(getResources(), bitmap);
			drawable.setCircular(true);
			uploadAvatarButton.setImageDrawable(drawable);
			
			break;
		}
	}
	
	private void startCropImage(Uri source) {
		if (source != null) {
			int size = getResources().getDimensionPixelSize(R.dimen.default_avatar_size);
			CropImageIntentBuilder cropImage = new CropImageIntentBuilder(size, size, Uri.fromFile(avatarImageFile));
			cropImage.setSourceImage(source);
			
			startActivityForResult(cropImage.getIntent(this), REQUEST_CODE_CROP_IMAGE);
		}
	}
	
	private byte[] getAvatarBytes() {
		if (!avatarImageFile.exists()) return null;
		
		InputStream inputStream = null;
		try {
			inputStream = new FileInputStream(avatarImageFile);
		} catch (FileNotFoundException e) {
			AppLog.e("avatar file not found", e);
		}
		
		byte[] buffer = new byte[1024];
		int bytesRead;
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		try {
			while ((bytesRead = inputStream.read(buffer)) != -1) {
				output.write(buffer, 0, bytesRead);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return output.toByteArray();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		if (signupTask != null) {
			signupTask.dismissDialogAndCancel();
		}
	}
}