package com.mstr.letschat.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.mstr.letschat.R;
import com.mstr.letschat.xmpp.UserLocation;

/**
 * Created by dilli on 12/1/2015.
 */
public abstract class LocationView extends MessageView implements OnMapReadyCallback  {
    protected TextView name;
    protected TextView address;
    protected MapView mapView;

    private GoogleMap map;

    public LocationView(Context context) {
        this(context, null);
    }

    public LocationView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void init(Context context) {
        super.init(context);

        mapView = (MapView)findViewById(R.id.map);
        name = (TextView)findViewById(R.id.tv_location_name);
        address = (TextView)findViewById(R.id.tv_address);

        setOrientation(LinearLayout.VERTICAL);
        setDescendantFocusability(FOCUS_BLOCK_DESCENDANTS);
    }

    public GoogleMap getMap() {
        return map;
    }

    public MapView getMapView() {
        return mapView;
    }

    public void setName(String text) {
        name.setText(text);
    }

    public void setAddress(String text) {
        address.setText(text);
    }

    public void initializeMapView() {
        if (mapView != null) {
            // Initialise the MapView
            mapView.onCreate(null);
            // Set the map ready callback to receive the GoogleMap object
            mapView.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        MapsInitializer.initialize(getContext().getApplicationContext());
        map = googleMap;

        UserLocation data = (UserLocation)mapView.getTag();
        if (data != null) {
            setMapLocation(data);
        }
    }

    public void setMapLocation(UserLocation data) {
        if (map != null) {
            LatLng latLng = data.getLatLng();
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13f));
            map.addMarker(new MarkerOptions().position(latLng));

            // Set the map type back to normal.
            map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        }
    }

    public void onMovedToScrapHeap() {
        if (map != null) {
            // Clear the map and free up resources by changing the map type to none
            map.clear();
            map.setMapType(GoogleMap.MAP_TYPE_NONE);
        }
    }
}