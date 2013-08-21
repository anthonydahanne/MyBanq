package net.dahanne.banq.notifications;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

public class PrefsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.prefs);
        findPreference(PreferenceHelper.KEY_PREF_LOGIN).setOnPreferenceClickListener(onclickListsner);
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
                PreferenceHelper.clearCache(getActivity());
                startActivity(LoginActivity.newIntent(getActivity(), true));
                getActivity().finish();
            }
            return true;
        }
    };

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        updateSummary(key);
    }
}