package net.dahanne.banq.notifications;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.provider.Settings;

public class PrefsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.prefs);
        findPreference(PreferenceHelper.KEY_PREF_LOGIN).setOnPreferenceClickListener(onclickListsner);
        findPreference(PreferenceHelper.KEY_PREF_SYNC).setOnPreferenceClickListener(onclickListsner);
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        updateSummary(PreferenceHelper.KEY_PREF_LOGIN);
        updateSummary(PreferenceHelper.KEY_PREF_DAYS_TO_TRIGGER);
    }

    private void updateSummary(String key) {
        if (PreferenceHelper.KEY_PREF_LOGIN.equals(key)) {
            String loginValue = PreferenceHelper.getLogin(getActivity());
            getPreferenceManager().findPreference(key).setSummary(loginValue);
        }
        if (PreferenceHelper.KEY_PREF_DAYS_TO_TRIGGER.equals(key)) {
            int daysToTriggerValue = PreferenceHelper.getDaysToTrigger(getActivity());
            getPreferenceManager().findPreference(key).setSummary(getString(R.string.remaining_days_summary, Integer.toString(daysToTriggerValue)));
        }
    }

    private Preference.OnPreferenceClickListener onclickListsner = new Preference.OnPreferenceClickListener() {
        @Override
        public boolean onPreferenceClick(Preference preference) {
            if (PreferenceHelper.KEY_PREF_LOGIN.equals(preference.getKey())) {
                //PreferenceHelper.clearCache(getActivity());
                startActivity(LoginActivity.newIntent(getActivity(), true).putExtra(LoginActivity.EXTRA_LOGIN,PreferenceHelper.getLogin(getActivity())));
//                getActivity().finish();
            } else if (PreferenceHelper.KEY_PREF_SYNC.equals(preference.getKey())) {
                startActivity(new Intent(Settings.ACTION_SYNC_SETTINGS).putExtra("authority",getString(R.string.authority)));
            }
            return true;
        }
    };

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        updateSummary(key);
    }
}