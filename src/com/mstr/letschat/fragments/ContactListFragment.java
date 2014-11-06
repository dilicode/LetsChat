package com.mstr.letschat.fragments;

import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract.Contacts;
import android.widget.SimpleCursorAdapter;

public class ContactListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {
	private static final String[] FROM_COLUMNS = {
			Contacts.DISPLAY_NAME };
	
	private static final String[] PROJECTION = {Contacts._ID, Contacts.DISPLAY_NAME};
	
	private static final int[] TO_IDS = {android.R.id.text1};
	
	private SimpleCursorAdapter cursorAdapter;
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		cursorAdapter = new SimpleCursorAdapter(getActivity(), android.R.layout.simple_list_item_1, null, FROM_COLUMNS, TO_IDS, 0);
		setListAdapter(cursorAdapter);
		
		setListShown(false);
		
		getLoaderManager().initLoader(0, null, this);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		String select = "((" + Contacts.DISPLAY_NAME + " NOTNULL) AND ("
                + Contacts.HAS_PHONE_NUMBER + "=1) AND ("
                + Contacts.DISPLAY_NAME + " != '' ))";
		
		return new CursorLoader(getActivity(), Contacts.CONTENT_URI,
				PROJECTION, select, null,
				Contacts.DISPLAY_NAME + " COLLATE LOCALIZED ASC");
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		cursorAdapter.swapCursor(data);
		
		if (isResumed()) {
			setListShown(true);
		} else {
			setListShownNoAnimation(true);
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		cursorAdapter.swapCursor(null);
	}
}