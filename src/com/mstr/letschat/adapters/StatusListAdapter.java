package com.mstr.letschat.adapters;

import java.util.List;

import android.content.Context;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class StatusListAdapter extends ArrayAdapter<String> {
	private int selectedPosition = -1;
	
	public StatusListAdapter(Context context, List<String> statusList) {
		super(context, android.R.layout.simple_list_item_1, statusList);
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		TextView textView = (TextView)super.getView(position, convertView, parent);
		if (position == selectedPosition) {
			textView.setText(Html.fromHtml("<b>" + textView.getText() + "</b>"));
		}
		
		return textView;
	}
	
	public void setSelection(int position) {
		this.selectedPosition = position;
	}
}
