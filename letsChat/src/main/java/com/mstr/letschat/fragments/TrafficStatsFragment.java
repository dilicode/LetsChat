package com.mstr.letschat.fragments;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.TrafficStats;
import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.mstr.letschat.R;
import com.mstr.letschat.utils.PreferenceUtils;

/**
 * Created by dilli on 7/8/2015.
 */
public class TrafficStatsFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.network_traffic_preferences);

        populateStatsFields();
    }

    private void populateStatsFields() {
        ApplicationInfo applicationInfo = null;
        try {
            applicationInfo = getActivity().getPackageManager().getApplicationInfo(getActivity().getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {}

        if (applicationInfo != null) {
            int uid = applicationInfo.uid;

            long receivedBytes = TrafficStats.getUidRxBytes(uid);
            long transmittedBytes = TrafficStats.getUidTxBytes(uid);
            String unit = " " + getString(R.string.traffic_unit);
            findPreference(PreferenceUtils.TRAFFIC_TRANSMITTED).setSummary(Long.toString(transmittedBytes / 1024) + unit);
            findPreference(PreferenceUtils.TRAFFIC_RECEIVED).setSummary(Long.toString(receivedBytes / 1024) + unit);
        }
    }
}