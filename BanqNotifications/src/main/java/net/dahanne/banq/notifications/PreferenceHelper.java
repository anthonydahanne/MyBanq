package net.dahanne.banq.notifications;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class PreferenceHelper {

    public static final String KEY_PREF_LOGIN = "login_key";
    public static final String KEY_PREF_DAYS_TO_TRIGGER = "days_to_trigger_key";

    private static final String KEY_PREF_USERNAME = "username_key";
    private static final int DEFAULT_DAYS_TO_TRIGGER = 3;
    private static final String KEY_PREF_SYNC_FREQ = "";
    public static final long _8_HOURS = 1000l * 60l * 60l * 8l;

    public static void saveUsername(Context context, String username) {
        getPreferences(context).edit().putString(KEY_PREF_USERNAME, username).commit();
    }

    public static void saveLogin(Context context, String login) {
        getPreferences(context).edit().putString(KEY_PREF_LOGIN, login).commit();
    }

    public static void saveDaysToTrigger(Context context, int daysToTrigger) {
        getPreferences(context).edit().putInt(KEY_PREF_DAYS_TO_TRIGGER, daysToTrigger).commit();
    }

    public static String getLogin(Context context) {
        return getPreferences(context).getString(KEY_PREF_LOGIN, "");
    }

    public static int getDaysToTrigger(Context context) {
        return getPreferences(context).getInt(KEY_PREF_DAYS_TO_TRIGGER, DEFAULT_DAYS_TO_TRIGGER);
    }

    public static String getUsername(Context context) {
        return getPreferences(context).getString(KEY_PREF_USERNAME, "");
    }

    private static SharedPreferences getPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static void clearCache(Context context) {
        getPreferences(context).edit().clear().commit();
    }

    public static long getSyncFrequency(Context context) {
        return getPreferences(context).getLong(KEY_PREF_SYNC_FREQ, _8_HOURS);
    }
}
