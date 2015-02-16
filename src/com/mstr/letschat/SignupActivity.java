package com.mstr.letschat;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.android.camera.CropImageIntentBuilder;
import com.mstr.letschat.tasks.Response.Listener;
import com.mstr.letschat.tasks.SignupTask;

public class SignupActivity extends Activity implements OnClickListener, Listener<Boolean> {
	private static final int REQUEST_CODE_SELECT_PICTURE = 1;
	private static final int REQUEST_CODE_CROP_IMAGE = 2;
	
	private static final String TEMP_PHOTO_FILE_NAME = "temp_photo.jpg";
	private static final String PROFILE_PHOTO_FILE_NAME = "profile_photo.jpg";
	
	private EditText nameText;
	private EditText phoneNumberText;
	private EditText passwordText;
	
	private Button submitButton;
	private ImageButton uploadAvatarButton;
	
	private File rawImageFile;
	private File croppedImageFile;
	
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
		
		if (isExternalStorageWritable()) {
			rawImageFile = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), TEMP_PHOTO_FILE_NAME);
		}
		
		croppedImageFile = new File(getFilesDir(), PROFILE_PHOTO_FILE_NAME);
	}
	
	@Override
	public void onClick(View v) {
		if (v == submitButton) {
			new SignupTask(this, this, phoneNumberText.getText().toString(), passwordText.getText().toString(), nameText.getText().toString()).execute();
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
			Toast.makeText(SignupActivity.this, R.string.login_success, Toast.LENGTH_SHORT).show();
			
			startActivity(new Intent(SignupActivity.this, ConversationActivity.class));
		}
	}

	@Override
	public void onErrorResponse(SmackInvocationException exception) {
		Toast.makeText(SignupActivity.this, R.string.create_account_error, Toast.LENGTH_SHORT).show();
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
			uploadAvatarButton.setImageBitmap(BitmapFactory.decodeFile(croppedImageFile.getAbsolutePath()));
			
			break;
		}
	}
	
	private void startCropImage(Uri source) {
		if (source != null) {
			Uri croppedImage = Uri.fromFile(croppedImageFile);
			
			int size = this.getResources().getDimensionPixelSize(R.dimen.sign_up_avatar_size);
			CropImageIntentBuilder cropImage = new CropImageIntentBuilder(size, size, croppedImage);
			cropImage.setSourceImage(source);
			
			startActivityForResult(cropImage.getIntent(this), REQUEST_CODE_CROP_IMAGE);
		}
	}
	
	private boolean isExternalStorageWritable() {
		String state = Environment.getExternalStorageState();
		return Environment.MEDIA_MOUNTED.equals(state);
	}
}