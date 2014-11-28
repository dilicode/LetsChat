package com.mstr.letschat.adapters;


public class ContactListItemAdapter {
	/*public ContactListItemAdapter(Context context, int layout, Cursor c, int flags) {
		super(context, layout, c, flags);
	}

	@Override
    public void bindView(View view, Context context, Cursor cursor) {
        final ContactListItemCache cache = (ContactListItemCache) view.getTag();
        // Set the name
        cursor.copyStringToBuffer(SUMMARY_NAME_COLUMN_INDEX, cache.nameBuffer);
        int size = cache.nameBuffer.sizeCopied;
        cache.nameView.setText(cache.nameBuffer.data, 0, size);
        final long contactId = cursor.getLong(SUMMARY_ID_COLUMN_INDEX);
        final String lookupKey = cursor.getString(SUMMARY_LOOKUP_KEY);
        cache.photoView.assignContactUri(Contacts.getLookupUri(contactId, lookupKey));
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = super.newView(context, cursor, parent);
        ContactListItemCache cache = new ContactListItemCache();
        cache.nameView = (TextView) view.findViewById(R.id.name);
        cache.photoView = (QuickContactBadge) view.findViewById(R.id.badge);
        view.setTag(cache);

        return view;
    }
}

final static class ContactListItemCache {
    public TextView nameView;
    public QuickContactBadge photoView;
    public CharArrayBuffer nameBuffer = new CharArrayBuffer(128);
}*/
}
