package com.mstr.letschat;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * Created by dilli on 7/10/2015.
 */
public class ServerSettingsActivity extends PreferenceActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.server_preference);
    }
}