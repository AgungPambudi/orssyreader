package com.drocode.android.orrsyreader.prefs;

import com.drocode.android.orrsyreader.R;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceActivity;
import android.webkit.URLUtil;

public class Preferences extends PreferenceActivity implements
		OnSharedPreferenceChangeListener {

	public static final String PREF_WIFI_ONLY = "PREF_WIFI_ONLY";
	public static final String PREF_RSS_URL = "PREF_RSS_URL";

	SharedPreferences prefs;

	private String rssFeedUrl = "http://drocode.com/feed/";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.setting_preferences);
	}

	@Override
	protected void onResume() {
		super.onResume();
		getPreferenceScreen().getSharedPreferences()
				.registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		getPreferenceScreen().getSharedPreferences()
				.unregisterOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		if (key.equals(PREF_RSS_URL)) {
			String value = sharedPreferences.getString(key, null);
			if (!URLUtil.isValidUrl(value)) {
				new AlertDialog.Builder(this)
						.setMessage("Please enter a valid URL")
						.setCancelable(false)
						.setNeutralButton("OK",
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
									}
								}).show();
				EditTextPreference p = (EditTextPreference) findPreference(PREF_RSS_URL);
				p.setText(rssFeedUrl);
			}
		}
	}

}
