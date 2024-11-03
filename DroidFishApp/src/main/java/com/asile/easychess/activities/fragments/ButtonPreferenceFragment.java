package com.asile.easychess.activities.fragments;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

import org.petero.easychess.R;

public class ButtonPreferenceFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.prefs_button, rootKey);
    }
}
