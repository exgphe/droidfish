package com.asile.easychess.activities.fragments;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

import com.asile.easychess.R;

public class ColorsPreferenceFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.prefs_colors, rootKey);
    }
}