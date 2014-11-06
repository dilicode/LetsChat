package com.mstr.letschat.adapters;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.mstr.letschat.R;
import com.mstr.letschat.model.ContactSearchResult;

public class ContactSearchResultAdapter extends BaseAdapter {
	private Context context;
	private List<ContactSearchResult> list;
	
	public ContactSearchResultAdapter(Context context, List<ContactSearchResult> list) {
		this.context = context;
		this.list = list;
	}
	
	@Override
	public int getCount() {
		return list.size();
	}

	@Override
	public Object getItem(int position) {
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder = null;
		
		if (convertView != null) {
			viewHolder = (ViewHolder)convertView.getTag();
		} else {
			convertView = LayoutInflater.from(context).inflate(R.layout.contact_search_result_item, parent, false);
			
			viewHolder = new ViewHolder();
			viewHolder.avatar = (ImageView)convertView.findViewById(R.id.avatar);
			viewHolder.user = (TextView)convertView.findViewById(R.id.tv_user);
			viewHolder.name = (TextView)convertView.findViewById(R.id.tv_name);
			
			convertView.setTag(viewHolder);
		}
		
		ContactSearchResult item = (ContactSearchResult)getItem(position);
		viewHolder.user.setText(item.getUser());
		viewHolder.name.setText(item.getName());
		
		return convertView;
	}
	
	static class ViewHolder {
		ImageView avatar;
		TextView user;
		TextView name;
	}
}