package com.mstr.letschat.adapters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.mstr.letschat.R;
import com.mstr.letschat.model.ContactRequest;

public class ContactRequestListAdapter extends BaseAdapter {
	
	public static interface OnAcceptButtonClickListener {
		public void onAcceptButtonClick(View view, int position);
	}
	
	private List<ContactRequest> requests;
	private Context context;
	private OnAcceptButtonClickListener listener;
	
	public ContactRequestListAdapter(Context context, List<ContactRequest> requests) {
		this.context = context;
		this.requests = requests;
	}
	
	public ContactRequestListAdapter(Context context) {
		this.context = context;
		this.requests = new ArrayList<ContactRequest>();
	}
	
	@Override
	public int getCount() {
		return requests.size();
	}

	@Override
	public Object getItem(int position) {
		return requests.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
	
	public void addAll(Collection<ContactRequest> collection) {
		requests.addAll(collection);
		notifyDataSetChanged();
	}
	
	public OnAcceptButtonClickListener getListener() {
		return listener;
	}

	public void setListener(OnAcceptButtonClickListener listener) {
		this.listener = listener;
	}
	
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder = null;
		
		if (convertView != null) {
			viewHolder = (ViewHolder)convertView.getTag();
		} else {
			convertView = LayoutInflater.from(context).inflate(R.layout.contact_request, parent, false);
			
			viewHolder = new ViewHolder();
			viewHolder.userText = (TextView)convertView.findViewById(R.id.tv_user);
			viewHolder.messageText = (TextView)convertView.findViewById(R.id.tv_message);
			viewHolder.acceptButton = (Button)convertView.findViewById(R.id.btn_accept);
			
			convertView.setTag(viewHolder);
		}
		
		ContactRequest request = (ContactRequest)getItem(position);
		viewHolder.userText.setText(request.getNickname());
		viewHolder.messageText.setText(context.getString(R.string.request_contact_text));
		viewHolder.acceptButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (listener != null) {
					listener.onAcceptButtonClick(v, position);
				}
			}
		});
		
		return convertView;
	}
	
	static class ViewHolder {
		TextView userText;
		TextView messageText;
		Button acceptButton;
	}
}