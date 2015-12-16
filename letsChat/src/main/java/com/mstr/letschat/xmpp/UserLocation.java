package com.mstr.letschat.xmpp;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.maps.model.LatLng;
import com.mstr.letschat.databases.ChatContract;
import com.mstr.letschat.databases.ChatMessageTableHelper;

import org.jivesoftware.smack.packet.PacketExtension;

/**
 * Created by dilli on 11/11/2015.
 */
public class UserLocation implements PacketExtension, Parcelable {
    public static final String NAMESPACE = "jabber:client";
    public static final String ELEMENT_NAME = "location";

    public static final String TAG_NAME_TYPE = "type";
    public static final String TAG_NAME_LATITUDE = "lat";
    public static final String TAG_NAME_LONGITUDE = "lon";
    public static final String TAG_NAME_NAME = "name";
    public static final String TAG_NAME_ADDRESS = "addr";

    private double latitude;
    private double longitude;
    private String name;
    private String address;

    public UserLocation() {}

    public UserLocation(Place place) {
        name = place.getName().toString();
        address = place.getAddress().toString();

        latitude = place.getLatLng().latitude;
        longitude = place.getLatLng().longitude;
    }

    public UserLocation(Parcel parcel) {
        name = parcel.readString();
        address = parcel.readString();
        latitude = parcel.readDouble();
        longitude = parcel.readDouble();
    }

    public UserLocation(Cursor cursor) {
        name = cursor.getString(cursor.getColumnIndex(ChatContract.ChatMessageTable.COLUMN_NAME_MESSAGE));
        address = cursor.getString(cursor.getColumnIndex(ChatContract.ChatMessageTable.COLUMN_NAME_ADDRESS));
        latitude = cursor.getDouble(cursor.getColumnIndex(ChatContract.ChatMessageTable.COLUMN_NAME_LATITUDE));
        longitude = cursor.getDouble(cursor.getColumnIndex(ChatContract.ChatMessageTable.COLUMN_NAME_LONGITUDE));
    }

    public String getName() {
        return name;
    }

    @Override
    public String getElementName() {
        return ELEMENT_NAME;
    }

    @Override
    public String getNamespace() {
        return NAMESPACE;
    }

    @Override
    public CharSequence toXML() {
        return String.format(
                "<location xmlns='jabber:client'><type>%d</type><lat>%f</lat><lon>%f</lon><name>%s</name><addr>%s</addr></location>",
                ChatMessageTableHelper.MESSAGE_TYPE_LOCATION, latitude, longitude, name, address);
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getAddress() {
        return address;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public LatLng getLatLng() {
        return new LatLng(latitude, longitude);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(address);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
    }

    public static final Parcelable.Creator<UserLocation> CREATOR = new Creator<UserLocation>() {

        @Override
        public UserLocation createFromParcel(Parcel source) {
            return new UserLocation(source);
        }

        @Override
        public UserLocation[] newArray(int size) {
            return new UserLocation[size];
        }
    };
}