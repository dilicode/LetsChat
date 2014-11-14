package com.mstr.letschat.tasks;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import org.jivesoftware.smack.RosterEntry;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Debug;
import android.provider.ContactsContract;

import com.mstr.letschat.xmpp.XMPPHelper;

public class SetupAccountTask extends AsyncTask<Void, Void, Void> {
	private static final String LOG_TAG = "SetupAccountTask";
	
	public static interface SetupAccountListener {
		public void onAccountSetup();
	}
	
	private WeakReference<Context> context;
	private WeakReference<SetupAccountListener> listener;
	private String user;
	
	public SetupAccountTask(SetupAccountListener listener, Context context, String user) {
		this.listener = new WeakReference<SetupAccountListener>(listener);
		this.context = new WeakReference<Context>(context);
		this.user = user;
	}
	
	@Override
	public Void doInBackground(Void... params) {
		
		List<RosterEntry> entriesInContacts = new ArrayList<RosterEntry>();
		
		Context ctx = context.get();
		if (ctx != null) {
			Cursor cursor = ctx.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
					new String[]{ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER}, 
					null, null,
					ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " COLLATE LOCALIZED ASC");
			if (cursor != null) {
				int numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
				int nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
				
				while (cursor.moveToNext()) {
					String number = cursor.getString(numberIndex);
					String name = cursor.getString(nameIndex);
					
					XMPPHelper.getInstance().search(number);
				}
			}
		}
		
		return null;
	}
	
	@Override
	public void onPostExecute(Void result) {
		SetupAccountListener l = listener.get();
		if (l != null) {
			l.onAccountSetup();
		}
	}
}
