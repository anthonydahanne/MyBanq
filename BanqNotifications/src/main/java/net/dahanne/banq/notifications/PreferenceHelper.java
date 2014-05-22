package net.dahanne.banq.notifications;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class PreferenceHelper {

    public static final String KEY_PREF_DAYS_TO_TRIGGER = "days_to_trigger_key";
    public static final String KEY_PREF_SYNC = "sync_key";
    public static final String KEY_PREF_ADD = "add_key";
    public static final String KEY_PREF_ENABLE_SYNC = "enable_sync_key";

    private static final int DEFAULT_DAYS_TO_TRIGGER = 3;

    public static boolean isSyncEnabled(Context context) {
        return getPreferences(context).getBoolean(KEY_PREF_ENABLE_SYNC, true);
    }

    public static int getDaysToTrigger(Context context) {
        return getPreferences(context).getInt(KEY_PREF_DAYS_TO_TRIGGER, DEFAULT_DAYS_TO_TRIGGER);
    }

    private static SharedPreferences getPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }
}
