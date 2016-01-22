package com.mstr.letschat.fragments;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.mstr.letschat.R;
import com.mstr.letschat.utils.NetworkUtils;
import com.mstr.letschat.utils.NetworkUtils.RxTxBytes;
import com.mstr.letschat.utils.PreferenceUtils;

/**
 * Created by dilli on 7/8/2015.
 */
public class NetworkUsageFragment extends PreferenceFragment {
    public static final long KB_IN_BYTES = 1024;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.network_traffic_preferences);

        populateStatsFields();
    }

    private void populateStatsFields() {
        RxTxBytes rxTxBytes = NetworkUtils.getTotalRxTxBytes(getActivity());
        if (rxTxBytes != null) {
            String unit = " " + getString(R.string.traffic_unit);
            findPreference(PreferenceUtils.TRAFFIC_TRANSMITTED).setSummary(Long.toString(rxTxBytes.txBytes / KB_IN_BYTES) + unit);
            findPreference(PreferenceUtils.TRAFFIC_RECEIVED).setSummary(Long.toString(rxTxBytes.rxBytes / KB_IN_BYTES) + unit);
        }
    }
}