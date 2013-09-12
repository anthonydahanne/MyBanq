package net.dahanne.banq.notifications;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.provider.Settings;

/**
 * Used to be a fragment, but fragments for preference activities are not compatible with older APIs
 * Look in the commit log to find back
 * BanqNotifications/src/main/java/net/dahanne/banq/notifications/PrefsFragment.java
 *
 */
public class PreferencesActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {

    private Preference.OnPreferenceClickListener onclickListener = new Preference.OnPreferenceClickListener() {
        @Override
        public boolean onPreferenceClick(Preference preference) {
            if (PreferenceHelper.KEY_PREF_SYNC.equals(preference.getKey())) {
                startActivity(new Intent(Settings.ACTION_SYNC_SETTINGS).putExtra("authority", getString(R.string.authority)));
            } else if (PreferenceHelper.KEY_PREF_ADD.equals(preference.getKey())) {
                startActivity(new Intent(Settings.ACTION_ADD_ACCOUNT).putExtra("authority", getString(R.string.authority)));
            }
            return true;
        }
    };

    @SuppressWarnings("deprecation")
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.prefs);
        findPreference(PreferenceHelper.KEY_PREF_SYNC).setOnPreferenceClickListener(onclickListener);
        findPreference(PreferenceHelper.KEY_PREF_ADD).setOnPreferenceClickListener(onclickListener);
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        updateSummary(PreferenceHelper.KEY_PREF_DAYS_TO_TRIGGER);
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

        if (PreferenceHelper.KEY_PREF_DAYS_TO_TRIGGER.equals(key)) {
            int daysToTriggerValue = PreferenceHelper.getDaysToTrigger(this);
            getPreferenceManager().findPreference(key).setSummary(getString(R.string.remaining_days_summary, Integer.toString(daysToTriggerValue)));
        }
    }
}
