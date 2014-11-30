package com.mstr.letschat.model;

import com.mstr.letschat.databases.ChatContract.ContactRequestTable;

import android.content.ContentValues;
import android.os.Parcel;
import android.os.Parcelable;

public class ContactRequest implements Parcelable {
	public static final int STATUS_PENDING = 1;
	public static final int STATUS_ACCPTED = 2;
	
	private String nickname;
	private String origin;
	
	private int status = STATUS_PENDING;
	
	public ContactRequest() {}
	
	public ContactRequest(String origin, String nickname) {
		this.origin = origin;
		this.nickname = nickname;
	}
	
	public ContactRequest(String origin, String nickname, int status) {
		this.origin = origin;
		this.nickname = nickname;
		this.status = status;
	}
	
	public ContactRequest(Parcel in) {
		origin = in.readString();
		nickname = in.readString();
		status = in.readInt();
	}
	
	public static ContentValues newContentValues(String origin, String nickname) {
		ContentValues values = new ContentValues();
		values.put(ContactRequestTable.COLUMN_NAME_ORIGIN, origin);
		values.put(ContactRequestTable.COLUMN_NAME_NICKNAME, nickname);
		values.put(ContactRequestTable.COLUMN_NAME_STATUS, STATUS_PENDING);
		
		return values;
	}
	
	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}
	
	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}
	
	public void markAsAccepted() {
		status = STATUS_ACCPTED;
	}
	
	public String getOrigin() {
		return origin;
	}

	public void setOrigin(String origin) {
		this.origin = origin;
	}
	
	public boolean isAccepted() {
		return status == STATUS_ACCPTED;
	}

	public String getNotificationMessage() {
		StringBuilder sb = new StringBuilder();
		sb.append(nickname).append(" ").append("");
		
		return sb.toString();
	}
	
	public static final Creator<ContactRequest> CREATOR = new Creator<ContactRequest>() {
		@Override
		public ContactRequest createFromParcel(Parcel in) {
			return new ContactRequest(in);
		}
		
		public ContactRequest[] newArray(int size) {
			return new ContactRequest[size];
		}
	};

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(origin);
		dest.writeString(nickname);
		dest.writeInt(status);
	}
}