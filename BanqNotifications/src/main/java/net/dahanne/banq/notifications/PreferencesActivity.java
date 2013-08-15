package net.dahanne.banq.notifications;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;

public class PreferencesActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {

    private Preference.OnPreferenceClickListener onclickListsner = new Preference.OnPreferenceClickListener() {
        @Override
        public boolean onPreferenceClick(Preference preference) {
            if (PreferenceHelper.KEY_PREF_LOGIN.equals(preference.getKey())) {
                PreferenceHelper.clearCache(PreferencesActivity.this);
                startActivity(LoginActivity.newIntent(PreferencesActivity.this));
                finish();
            }
            return true;
        }
    };

    @SuppressWarnings("deprecation")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.prefs);
        updateSummary(PreferenceHelper.KEY_PREF_LOGIN);
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        findPreference(PreferenceHelper.KEY_PREF_LOGIN).setOnPreferenceClickListener(onclickListsner);
    }

    public static Intent newIntent(Context context) {
        return new Intent(context, PreferencesActivity.class);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        updateSummary(key);
    }

    @SuppressWarnings("deprecation")
    private void updateSummary(String key) {
        String value = "";
        if (PreferenceHelper.KEY_PREF_LOGIN.equals(key)) {
            value = PreferenceHelper.getLogin(this);
            getPreferenceManager().findPreference(key).setSummary(value);
        }
    }
}
