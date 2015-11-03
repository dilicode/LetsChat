package com.mstr.letschat;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.LoaderManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.TaskStackBuilder;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewAnimationUtils;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.mstr.letschat.adapters.MessageCursorAdapter;
import com.mstr.letschat.databases.ChatContract.ChatMessageTable;
import com.mstr.letschat.service.MessageService;
import com.mstr.letschat.service.MessageService.LocalBinder;
import com.mstr.letschat.tasks.Response.Listener;
import com.mstr.letschat.tasks.SendChatMessageTask;
import com.mstr.letschat.utils.AppLog;
import com.mstr.letschat.utils.Utils;

public class ChatActivity extends Activity
		implements OnClickListener, Listener<Boolean>, 
		LoaderManager.LoaderCallbacks<Cursor>, TextWatcher {
	
	public static final String EXTRA_DATA_NAME_TO = "com.mstr.letschat.To";
	public static final String EXTRA_DATA_NAME_NICKNAME = "com.mstr.letschat.Nickname";

	private static final int REQUEST_PLACE_PICKER = 1;
	
	private String to;
	private String nickname;
	
	private EditText messageText;
	private ImageButton sendButton;
	private ListView messageListView;
	private LinearLayout attachOptionsView;
	private ImageButton attachLocationButton;
	
	private MessageCursorAdapter adapter;
	
	private MessageService messageService;
	private boolean bound = false;
	
	private ServiceConnection serviceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			messageService = ((LocalBinder)service).getService();
			messageService.startConversation(to);
			bound = true;
		}
		
		@Override
		public void onServiceDisconnected(ComponentName name) {
			bound = false;
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		to = getIntent().getStringExtra(EXTRA_DATA_NAME_TO);
		nickname = getIntent().getStringExtra(EXTRA_DATA_NAME_NICKNAME);
		
		setContentView(R.layout.activity_chat);
	
		messageText = (EditText)findViewById(R.id.et_message);
		messageText.addTextChangedListener(this);
		
		sendButton = (ImageButton)findViewById(R.id.btn_send);
		sendButton.setOnClickListener(this);
		sendButton.setEnabled(false);
		
		messageListView = (ListView)findViewById(R.id.message_list);
		adapter = new MessageCursorAdapter(this, null, 0);
		messageListView.setAdapter(adapter);

		initAttachOptions();

		setTitle(nickname);
		
		getActionBar().setDisplayHomeAsUpEnabled(true);

		getLoaderManager().initLoader(0, null, this);
	}		

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.chat_menu, menu);

		return true;
	}

	@Override
	protected void onStart() {
		super.onStart();
		
		// bind to MessageService
		Intent intent = new Intent(this, MessageService.class);
		bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		
		if (bound) {
			if (messageService != null) {
				messageService.stopConversation();
			}
			unbindService(serviceConnection);
			bound = false;
		}
	}
	
	@Override
	public void onClick(View v) {
		if (v == sendButton) {
			new SendChatMessageTask(this, this, to, nickname, getMessage()).execute();
			return;
		}

		if (v == attachLocationButton) {
			sendLocation();
			return;
		}
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;

		case R.id.action_attach:
			setAttachOptionsVisibility(attachOptionsView.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);

			return true;
		}
		
		return super.onOptionsItemSelected(item);
	}

	
	private String getMessage() {
		return messageText.getText().toString();
	}
	
	@Override
	public void onResponse(Boolean result) {
		clearText();
	}

	@Override
	public void onErrorResponse(SmackInvocationException exception) {
		clearText();
	}

	private void clearText() {
		messageText.setText("");
	}
	
	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		String[] projection = new String[] {
				ChatMessageTable._ID,
				ChatMessageTable.COLUMN_NAME_MESSAGE,
				ChatMessageTable.COLUMN_NAME_TIME,
				ChatMessageTable.COLUMN_NAME_TYPE,
				ChatMessageTable.COLUMN_NAME_STATUS
		};

		return new CursorLoader(this, ChatMessageTable.CONTENT_URI, projection,
				ChatMessageTable.COLUMN_NAME_JID + "=?", new String[]{to}, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		adapter.swapCursor(data);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		adapter.swapCursor(null);
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {}

	@Override
	public void afterTextChanged(Editable s) {
		if (s.toString().trim().length() > 0) {
			sendButton.setEnabled(true);
		} else {
			sendButton.setEnabled(false);
		}
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	private void showAttachOptions() {
		if (Utils.hasLollipop()) {
			int radius = Math.max(attachOptionsView.getWidth(), attachOptionsView.getHeight());

			Animator animator = ViewAnimationUtils.createCircularReveal(attachOptionsView, attachOptionsView.getRight(),
					attachOptionsView.getTop(), 0, radius);
			attachOptionsView.setVisibility(View.VISIBLE);
			animator.setInterpolator(new AccelerateDecelerateInterpolator());
			animator.setDuration(200);
			animator.start();
		} else {
			attachOptionsView.setVisibility(View.VISIBLE);
		}
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	private void hideAttachOptions() {
		if (Utils.hasLollipop()) {
			int radius = Math.max(attachOptionsView.getWidth(), attachOptionsView.getHeight());

			Animator animator = ViewAnimationUtils.createCircularReveal(attachOptionsView, attachOptionsView.getRight(),
					attachOptionsView.getTop(), radius, 0);
			animator.addListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					super.onAnimationEnd(animation);
					attachOptionsView.setVisibility(View.GONE);
				}
			});
			animator.setInterpolator(new AccelerateDecelerateInterpolator());
			animator.setDuration(200);
			animator.start();
		} else {
			attachOptionsView.setVisibility(View.GONE);
		}
	}

	private boolean setAttachOptionsVisibility(int visibility) {
		if (attachOptionsView.getVisibility() == visibility) {
			return false;
		}

		if (visibility == View.VISIBLE) {
			showAttachOptions();

			return true;
		} else if (visibility == View.GONE) {
			hideAttachOptions();
			return true;
		}

		return false;
	}


	@Override
	public void onBackPressed() {
		if (!setAttachOptionsVisibility(View.GONE)) {
			super.onBackPressed();
		}
	}

	private void initAttachOptions() {
		attachOptionsView = (LinearLayout)findViewById(R.id.attach_options_container);
		attachLocationButton = (ImageButton)findViewById(R.id.attach_location);
		attachLocationButton.setOnClickListener(this);
	}

	public static PendingIntent getChatActivityPendingIntent(Context context, String to, String nickname) {
		TaskStackBuilder taskStackbuilder = TaskStackBuilder.create(context);
		taskStackbuilder.addParentStack(ChatActivity.class);
		Intent intent = new Intent(context, ChatActivity.class);
		intent.putExtra(ChatActivity.EXTRA_DATA_NAME_TO, to);
		intent.putExtra(ChatActivity.EXTRA_DATA_NAME_NICKNAME, nickname);
		taskStackbuilder.addNextIntent(intent);
		
		return taskStackbuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
	}

	private void sendLocation() {
		// Construct an intent for the place picker
		try {
			PlacePicker.IntentBuilder intentBuilder = new PlacePicker.IntentBuilder();
			Intent intent = intentBuilder.build(this);
			// Start the intent by requesting a result,
			// identified by a request code.
			startActivityForResult(intent, REQUEST_PLACE_PICKER);

		} catch (GooglePlayServicesRepairableException e) {
			AppLog.e(e.toString(), e);
		} catch (GooglePlayServicesNotAvailableException e) {
			AppLog.e(e.toString(), e);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_PLACE_PICKER && resultCode == Activity.RESULT_OK) {
			// The user has selected a place. Extract the name and address.
			final Place place = PlacePicker.getPlace(data, this);

			final CharSequence name = place.getName();
			final CharSequence address = place.getAddress();
			String attributions = PlacePicker.getAttributions(data);
			if (attributions == null) {
				attributions = "";
			}

			Toast.makeText(this, "name: " + name + " address: " + address, Toast.LENGTH_SHORT).show();
		} else {
			super.onActivityResult(requestCode, resultCode, data);
		}
	}
}