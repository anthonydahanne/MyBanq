package net.dahanne.banq.notifications;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class PreferencesActivity extends PreferenceActivity {



    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new PrefsFragment()).commit();
        PreferenceManager.setDefaultValues(this, R.xml.prefs, false);

    }

    public static Intent newIntent(Context context) {
        return new Intent(context, PreferencesActivity.class);
    }
}
