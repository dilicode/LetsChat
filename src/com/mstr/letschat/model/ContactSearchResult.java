package com.mstr.letschat.model;

import android.os.Parcel;
import android.os.Parcelable;

public class ContactSearchResult implements Parcelable {
	private String user;
	private String name;
	
	public ContactSearchResult(String user, String name) {
		this.user = user;
		this.name = name;
	}
	
	public ContactSearchResult() {}
	
	private ContactSearchResult(Parcel in) {
		user = in.readString();
		name = in.readString();
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
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(user);
		dest.writeString(name);
	}
	
	public static final Parcelable.Creator<ContactSearchResult> CREATOR = new Parcelable.Creator<ContactSearchResult>() {
		@Override
		public ContactSearchResult createFromParcel(Parcel source) {
			return new ContactSearchResult(source);
		}

		@Override
		public ContactSearchResult[] newArray(int size) {
			return new ContactSearchResult[size];
		}
		
	};
}