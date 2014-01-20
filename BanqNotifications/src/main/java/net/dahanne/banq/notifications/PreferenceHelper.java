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
    private static final String KEY_PREF_SYNC_FREQ = "";
    public static final long _24_HOURS = 1000l * 60l * 60l * 24l;

    public static boolean isSyncEnabled(Context context) {
        return getPreferences(context).getBoolean(KEY_PREF_ENABLE_SYNC, true);
    }

    public static int getDaysToTrigger(Context context) {
        return getPreferences(context).getInt(KEY_PREF_DAYS_TO_TRIGGER, DEFAULT_DAYS_TO_TRIGGER);
    }

    private static SharedPreferences getPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static long getSyncFrequency(Context context) {
        return getPreferences(context).getLong(KEY_PREF_SYNC_FREQ, _24_HOURS);
    }
}
