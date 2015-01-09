package com.mstr.letschat.adapters;

import java.util.List;

import android.content.Context;
import android.content.res.TypedArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.mstr.letschat.R;

public class StatusListAdapter extends ArrayAdapter<String> {
	private int primaryTextColor;
	private int selectedTextColor;
	
	private int selectedPosition = -1;
	
	public StatusListAdapter(Context context, List<String> statusList) {
		super(context, android.R.layout.simple_list_item_1, statusList);
		
		TypedArray a = context.obtainStyledAttributes(new int[] {android.R.attr.textColorPrimary});
		primaryTextColor = a.getColor(0, 0);
		a.recycle();
		
		selectedTextColor = context.getResources().getColor(R.color.selected_status);
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		TextView textView = (TextView)super.getView(position, convertView, parent);
		
		textView.setTextColor(position == selectedPosition ? selectedTextColor : primaryTextColor);
		
		return textView;
	}
	
	public void setSelection(int position) {
		if (selectedPosition != position) {
			selectedPosition = position;
		
			notifyDataSetChanged();
		}
	}
	
	public void setSelection(String status) {
		int position = getPosition(status);
		
		setSelection(position);
	}
}