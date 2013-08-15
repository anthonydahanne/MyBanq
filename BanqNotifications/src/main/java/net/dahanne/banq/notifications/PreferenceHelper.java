package net.dahanne.banq.notifications;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PreferenceHelper {

    private static final String KEY_PREF_LOGIN = "login_key";
    private static final String KEY_PREF_COOKIES = "cookies_key";

    public static void saveLogin(Context context, String login) {
        getPreferences(context).edit().putString(KEY_PREF_LOGIN, login).commit();
    }

    public static void saveCookies(Context context, Set<String> cookies) {
        getPreferences(context).edit().putStringSet(KEY_PREF_COOKIES, cookies).commit();
    }

    public static boolean isLoggedIn(Context context) {
        return !getCookies(context).isEmpty();
    }

    public static String getLogin(Context context) {
        return getPreferences(context).getString(KEY_PREF_LOGIN, "");
    }

    private static SharedPreferences getPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static void clearCache(Context context) {
        getPreferences(context).edit().clear().commit();
    }

    public static Set<String> getCookies(Context context) {
        return getPreferences(context).getStringSet(KEY_PREF_COOKIES, new HashSet<String>());
    }
}
