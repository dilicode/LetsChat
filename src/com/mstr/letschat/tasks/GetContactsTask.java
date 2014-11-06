package com.mstr.letschat.tasks;

import java.lang.ref.WeakReference;
import java.util.List;

import org.jivesoftware.smack.RosterEntry;

import com.mstr.letschat.utils.XMPPUtils;

import android.os.AsyncTask;

public class GetContactsTask extends AsyncTask<Void, Void, List<RosterEntry>> {

	public static interface GetRosterEntriesInterface {
		public void onRosterEntriesReceived(List<RosterEntry> entries);
	}
	
	private WeakReference<GetRosterEntriesInterface> listener;
	
	public GetContactsTask(GetRosterEntriesInterface listener) {
		this.listener = new WeakReference<GetRosterEntriesInterface>(listener);
	}
	
	@Override
	protected List<RosterEntry> doInBackground(Void... params) {
		return XMPPUtils.getRosterEntries();
	}
	
	public void onPostExecute(List<RosterEntry> entries) {
		GetRosterEntriesInterface l = listener.get();
		if (l != null) {
			l.onRosterEntriesReceived(entries);
		}
	}
}