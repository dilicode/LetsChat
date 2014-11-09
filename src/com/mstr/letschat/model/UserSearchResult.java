package com.mstr.letschat.model;

import android.os.Parcel;
import android.os.Parcelable;

public class UserSearchResult implements Parcelable {
	private String user;
	private String name;
	
	private boolean added;
	
	public UserSearchResult(String user, String name) {
		this.user = user;
		this.name = name;
	}
	
	public UserSearchResult() {}
	
	private UserSearchResult(Parcel in) {
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
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		
		if (o == null) {
			return false;
		}
		
		if (!(o instanceof UserSearchResult)) {
			return false;
		}
		
		return name.equals(((UserSearchResult)o).name);
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(user);
		dest.writeString(name);
		dest.writeByte((byte)(added ? 1 : 0));
	}
	
	public static final Parcelable.Creator<UserSearchResult> CREATOR = new Parcelable.Creator<UserSearchResult>() {
		@Override
		public UserSearchResult createFromParcel(Parcel source) {
			return new UserSearchResult(source);
		}

		@Override
		public UserSearchResult[] newArray(int size) {
			return new UserSearchResult[size];
		}
		
	};
}