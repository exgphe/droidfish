package com.asile.easychess.activities.fragments;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

import com.asile.easychess.R;

public class BookPreferenceFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.prefs_book, rootKey);
    }
}
