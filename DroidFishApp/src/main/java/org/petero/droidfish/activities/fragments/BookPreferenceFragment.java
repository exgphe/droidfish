package org.petero.droidfish.activities.fragments;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

import org.petero.droidfish.R;

public class BookPreferenceFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.prefs_book, rootKey);
    }
}
