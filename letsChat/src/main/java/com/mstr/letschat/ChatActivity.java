package com.mstr.letschat;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.LoaderManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewAnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.mstr.letschat.adapters.MessageCursorAdapter;
import com.mstr.letschat.databases.ChatContract;
import com.mstr.letschat.databases.ChatContract.ChatMessageTable;
import com.mstr.letschat.databases.ConversationTableHelper;
import com.mstr.letschat.providers.DatabaseContentProvider;
import com.mstr.letschat.service.MessageService;
import com.mstr.letschat.service.MessageService.LocalBinder;
import com.mstr.letschat.tasks.Response.Listener;
import com.mstr.letschat.tasks.SendPlainTextTask;
import com.mstr.letschat.tasks.SendImageTask;
import com.mstr.letschat.tasks.SendLocationTask;
import com.mstr.letschat.utils.Utils;
import com.mstr.letschat.views.LocationView;
import com.mstr.letschat.xmpp.UserLocation;

import java.util.ArrayList;

public class ChatActivity extends AppCompatActivity
		implements OnClickListener, Listener<Boolean>,
		LoaderManager.LoaderCallbacks<Cursor>, TextWatcher,
		AbsListView.MultiChoiceModeListener {

	private static final String LOG_TAG = "ChatActivity";
	public static final String EXTRA_DATA_NAME_TO = "com.mstr.letschat.To";
	public static final String EXTRA_DATA_NAME_NICKNAME = "com.mstr.letschat.Nickname";

	private static final int REQUEST_PLACE_PICKER = 1;
	private static final int REQUEST_IMAGE_PICKER = 2;

	private String to;
	private String nickname;
	
	private EditText messageText;
	private ImageButton sendButton;
	private ListView messageListView;
	private CardView attachOptionsContainer;
	private Button attachLocationButton;
	private Button attachGalleryButton;
	
	private MessageCursorAdapter adapter;
	
	private MessageService messageService;
	private boolean bound = false;

	private Listener<Boolean> sendLocationListener;
	private Listener<Boolean> sendImageListener;

	private AbsListView.RecyclerListener recyclerListener = new AbsListView.RecyclerListener() {
		@Override
		public void onMovedToScrapHeap(View view) {
			if (view instanceof LocationView) {
				((LocationView)view).onMovedToScrapHeap();
			}
		}
	};
	
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
		messageListView.setRecyclerListener(recyclerListener);
		messageListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
		messageListView.setMultiChoiceModeListener(this);

		initAttachOptions();

		setTitle(nickname);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		getLoaderManager().initLoader(0, null, this);

		initTaskListeners();
	}

	private void initTaskListeners() {
		sendLocationListener = new Listener<Boolean>() {
			@Override
			public void onResponse(Boolean result) {}

			@Override
			public void onErrorResponse(Exception exception) {}
		};

		sendImageListener = new Listener<Boolean>() {
			@Override
			public void onResponse(Boolean result) {}

			@Override
			public void onErrorResponse(Exception exception) {
				Toast.makeText(ChatActivity.this, R.string.send_image_error, Toast.LENGTH_SHORT).show();
			}
		};
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
			new SendPlainTextTask(this, this, to, nickname, getMessage()).execute();
			return;
		}

		if (v == attachLocationButton) {
			sendLocation();
			return;
		}

		if (v == attachGalleryButton) {
			pickImage();
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
			setAttachOptionsVisibility(attachOptionsContainer.getVisibility() == View.VISIBLE ? View.INVISIBLE : View.VISIBLE);

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
	public void onErrorResponse(Exception exception) {
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
				ChatMessageTable.COLUMN_NAME_STATUS,
				ChatMessageTable.COLUMN_NAME_ADDRESS,
				ChatMessageTable.COLUMN_NAME_LATITUDE,
				ChatMessageTable.COLUMN_NAME_LONGITUDE,
				ChatMessageTable.COLUMN_NAME_MEDIA_URL
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
			int cx = attachOptionsContainer.getWidth() / 2;
			int cy = attachOptionsContainer.getHeight() / 2;
			float endRadius = (float) Math.hypot(cx, cy);

			Animator animator = ViewAnimationUtils.createCircularReveal(attachOptionsContainer, cx, cy, 0, endRadius);
			animator.setInterpolator(new LinearInterpolator());
			animator.setDuration(getResources().getInteger(R.integer.attach_views_anim_duration));
			animator.addListener(new AnimatorListenerAdapter() {});
			attachOptionsContainer.setVisibility(View.VISIBLE);
			animator.start();
		} else {
			attachOptionsContainer.setVisibility(View.VISIBLE);
		}
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	private void hideAttachOptions() {
		if (Utils.hasLollipop()) {
			int cx = attachOptionsContainer.getWidth() / 2;
			int cy = attachOptionsContainer.getHeight() / 2;
			float startRadius = (float) Math.hypot(cx, cy);

			Animator animator = ViewAnimationUtils.createCircularReveal(attachOptionsContainer, cx, cy, startRadius, 0);
			animator.addListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					attachOptionsContainer.setVisibility(View.INVISIBLE);
				}
			});
			animator.setInterpolator(new LinearInterpolator());
			animator.setDuration(getResources().getInteger(R.integer.attach_views_anim_duration));
			animator.start();
		} else {
			attachOptionsContainer.setVisibility(View.INVISIBLE);
		}
	}

	private boolean setAttachOptionsVisibility(int visibility) {
		if (attachOptionsContainer.getVisibility() == visibility) {
			return false;
		}

		if (visibility == View.VISIBLE) {
			showAttachOptions();

			return true;
		} else if (visibility == View.INVISIBLE) {
			hideAttachOptions();
			return true;
		}

		return false;
	}


	@Override
	public void onBackPressed() {
		if (!setAttachOptionsVisibility(View.INVISIBLE)) {
			super.onBackPressed();
		}
	}

	private void initAttachOptions() {
		attachOptionsContainer = (CardView)findViewById(R.id.attach_options_container);
		attachLocationButton = (Button)findViewById(R.id.attach_location);
		attachLocationButton.setOnClickListener(this);
		attachGalleryButton = (Button)findViewById(R.id.attach_from_gallery);
		attachGalleryButton.setOnClickListener(this);
	}

	public static PendingIntent getNotificationPendingIntent(Context context, String to, String nickname) {
		TaskStackBuilder taskStackbuilder = TaskStackBuilder.create(context);
		taskStackbuilder.addParentStack(ChatActivity.class);
		Intent intent = new Intent(context, ChatActivity.class);
		intent.putExtra(ChatActivity.EXTRA_DATA_NAME_TO, to);
		intent.putExtra(ChatActivity.EXTRA_DATA_NAME_NICKNAME, nickname);
		taskStackbuilder.addNextIntent(intent);
		
		return taskStackbuilder.getPendingIntent(1, PendingIntent.FLAG_UPDATE_CURRENT);
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
			Log.e(LOG_TAG, e.toString(), e);
		} catch (GooglePlayServicesNotAvailableException e) {
			Log.e(LOG_TAG, e.toString(), e);
		}
	}

	private void pickImage() {
		Intent pickIntent = new Intent(Intent.ACTION_GET_CONTENT);
		pickIntent.setType("image/*");

		startActivityForResult(pickIntent, REQUEST_IMAGE_PICKER);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			switch (requestCode) {
				case REQUEST_PLACE_PICKER:
					// The user has selected a place. Extract the name and address.
					Place place = PlacePicker.getPlace(data, this);
					new SendLocationTask(sendLocationListener, this, to, nickname, new UserLocation(place)).execute();
					break;

				case REQUEST_IMAGE_PICKER:
					String fileName = String.valueOf(System.currentTimeMillis());
					new SendImageTask(sendImageListener, this, to, nickname, getString(R.string.image_message_body), data.getData(), fileName).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
					break;
			}
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
		mode.setTitle(String.valueOf(messageListView.getCheckedItemCount()));
	}

	@Override
	public boolean onCreateActionMode(ActionMode mode, Menu menu) {
		MenuInflater inflater = mode.getMenuInflater();
		inflater.inflate(R.menu.chat_select_context, menu);
		return true;
	}

	@Override
	public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
		return false;
	}

	@Override
	public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_delete:
				deleteMessages();
				mode.finish(); // Action picked, so close the CAB
				return true;
			default:
				return false;
		}
	}

	@Override
	public void onDestroyActionMode(ActionMode mode) {}

	private void deleteMessages() {
		SparseBooleanArray positions = messageListView.getCheckedItemPositions();
		boolean isLatestMessageDeleted = positions.get(adapter.getCount() - 1);

		long[] ids = messageListView.getCheckedItemIds();
		ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();
		for (long id : ids) {
			operations.add(ContentProviderOperation.newDelete(ChatMessageTable.CONTENT_URI)
					.withSelection(ChatMessageTable._ID + " =? ", new String[]{String.valueOf(id)}).build());
		}

		try {
			getContentResolver().applyBatch(DatabaseContentProvider.AUTHORITY, operations);
		} catch (Exception e) {
			Log.e(LOG_TAG, "delete messages error ", e);
		}

		if (isLatestMessageDeleted) {
			Cursor cursor = getContentResolver().query(ChatMessageTable.CONTENT_URI, new String[]{"MAX(_id) AS max_id"},
					ChatMessageTable.COLUMN_NAME_JID + "=?", new String[]{to}, null);
			if (cursor.moveToFirst()) {
				int maxId = cursor.getInt(0);
				cursor = getContentResolver().query(ChatMessageTable.CONTENT_URI, new String[]{ChatMessageTable.COLUMN_NAME_MESSAGE, ChatMessageTable.COLUMN_NAME_TIME},
						ChatMessageTable._ID + "=?", new String[]{String.valueOf(maxId)}, null);
				if (cursor.moveToFirst()) {
					String latestMessage = cursor.getString(cursor.getColumnIndex(ChatMessageTable.COLUMN_NAME_MESSAGE));
					long timeMillis = cursor.getLong(cursor.getColumnIndex(ChatMessageTable.COLUMN_NAME_TIME));
					getContentResolver().update(ChatContract.ConversationTable.CONTENT_URI, ConversationTableHelper.newUpdateContentValues(latestMessage, timeMillis),
							ChatContract.ConversationTable.COLUMN_NAME_NAME + "=?", new String[]{to});
				} else {
					getContentResolver().delete(ChatContract.ConversationTable.CONTENT_URI, ChatContract.ConversationTable.COLUMN_NAME_NAME + "=?", new String[]{to});
				}
			}
		}
	}
}