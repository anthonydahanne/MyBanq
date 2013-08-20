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
                startActivity(LoginActivity.newIntent(PreferencesActivity.this, true));
                finish();
            }
            return true;
        }
    };

    @SuppressWarnings("deprecation")
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.prefs);
        updateSummary(PreferenceHelper.KEY_PREF_LOGIN);
        updateSummary(PreferenceHelper.KEY_PREF_DAYS_TO_TRIGGER);
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

        if (PreferenceHelper.KEY_PREF_LOGIN.equals(key)) {
            String loginValue = PreferenceHelper.getLogin(this);
            getPreferenceManager().findPreference(key).setSummary(loginValue);
        }
        if (PreferenceHelper.KEY_PREF_DAYS_TO_TRIGGER.equals(key)) {
            int daysToTriggerValue = PreferenceHelper.getDaysToTrigger(this);
            getPreferenceManager().findPreference(key).setSummary(Integer.toString(daysToTriggerValue));
        }
    }
}
