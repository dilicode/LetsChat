package com.mstr.letschat.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Contact implements Parcelable {
	private String user;
	private String name;
	
	private boolean added;
	
	public Contact(String user, String name) {
		this.user = user;
		this.name = name;
	}
	
	public Contact() {}
	
	private Contact(Parcel in) {
		user = in.readString();
		name = in.readString();
		added = in.readByte() != 0;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public boolean isAdded() {
		return added;
	}

	public void setAdded(boolean added) {
		this.added = added;
	}
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(user);
		dest.writeString(name);
		dest.writeByte((byte)(added ? 1 : 0));
	}
	
	public static final Parcelable.Creator<Contact> CREATOR = new Parcelable.Creator<Contact>() {
		@Override
		public Contact createFromParcel(Parcel source) {
			return new Contact(source);
		}

		@Override
		public Contact[] newArray(int size) {
			return new Contact[size];
		}
		
	};
}