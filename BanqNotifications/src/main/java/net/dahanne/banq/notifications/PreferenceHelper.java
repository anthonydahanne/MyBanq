package net.dahanne.banq.notifications;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.HashSet;
import java.util.Set;

public class PreferenceHelper {

    public static final String KEY_PREF_LOGIN = "login_key";
    private static final String KEY_PREF_COOKIES = "cookies_key";
    private static final String KEY_PREF_USERNAME = "username_key";
    private static final String KEY_PREF_PASSWORD = "password_key";

    /*public static void saveUsername(Context context, String username) {
        getPreferences(context).edit().putString(KEY_PREF_USERNAME, username).commit();
    }*/

    /*@Deprecated
    public static void savePassword(Context context, String password) {
        getPreferences(context).edit().putString(KEY_PREF_PASSWORD, password).commit();
    }*/

    public static void saveLogin(Context context, String login) {
        getPreferences(context).edit().putString(KEY_PREF_LOGIN, login).commit();
    }

    /*@Deprecated
    public static void saveCookies(Context context, Set<String> cookies) {
        getPreferences(context).edit().putStringSet(KEY_PREF_COOKIES, cookies).commit();
    }

    @Deprecated
    public static boolean isLoggedIn(Context context) {
        return !getCookies(context).isEmpty();
    }*/

    public static String getLogin(Context context) {
        return getPreferences(context).getString(KEY_PREF_LOGIN, "");
    }

    /*public static String getUsername(Context context) {
        return getPreferences(context).getString(KEY_PREF_USERNAME, "");
    }*/

    /*@Deprecated
    public static String getPassword(Context context) {
        return getPreferences(context).getString(KEY_PREF_PASSWORD, "");
    }*/

    private static SharedPreferences getPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static void clearCache(Context context) {
        getPreferences(context).edit().clear().commit();
    }

    /*@Deprecated
    public static Set<String> getCookies(Context context) {
        return getPreferences(context).getStringSet(KEY_PREF_COOKIES, new HashSet<String>());
    }*/
}
