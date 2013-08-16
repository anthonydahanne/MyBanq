/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package net.dahanne.banq.notifications;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import net.dahanne.banq.BanqClient;

import java.util.HashSet;
import java.util.Set;

/**
 * This class is an implementation of AbstractAccountAuthenticator for
 * authenticating accounts in the com.example.android.samplesync domain. The
 * interesting thing that this class demonstrates is the use of authTokens as
 * part of the authentication process. In the account setup UI, the user enters
 * their username and password. But for our subsequent calls off to the service
 * for syncing, we want to use an authtoken instead - so we're not continually
 * sending the password over the wire. getAuthToken() will be called when
 * SyncAdapter calls AccountManager.blockingGetAuthToken(). When we get called,
 * we need to return the appropriate authToken for the specified account. If we
 * already have an authToken stored in the account, we return that authToken. If
 * we don't, but we do have a username and password, then we'll attempt to talk
 * to the sample service to fetch an authToken. If that fails (or we didn't have
 * a username/password), then we need to prompt the user - so we create an
 * AuthenticatorActivity intent and return that. That will display the dialog
 * that prompts the user for their login information.
 */
class Authenticator extends AbstractAccountAuthenticator {

    /**
     * The tag used to log to adb console. *
     */
    private static final String TAG = "Authenticator";

    // Authentication Service context
    private final Context mContext;

    public Authenticator(Context context) {
        super(context);
        mContext = context;
    }

    @Override
    public Bundle addAccount(AccountAuthenticatorResponse response, String accountType,
                             String authTokenType, String[] requiredFeatures, Bundle options) {
        Log.v(TAG, "addAccount()");
        final Intent intent = new Intent(mContext, LoginActivity.class);
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
        final Bundle bundle = new Bundle();
        bundle.putParcelable(AccountManager.KEY_INTENT, intent);
        return bundle;
    }

    @Override
    public Bundle confirmCredentials(
            AccountAuthenticatorResponse response, Account account, Bundle options) {
        Log.v(TAG, "confirmCredentials()");
        return null;
    }

    @Override
    public Bundle editProperties(AccountAuthenticatorResponse response, String accountType) {
        Log.v(TAG, "editProperties()");
        throw new UnsupportedOperationException();
    }

    @Override
    public Bundle getAuthToken(AccountAuthenticatorResponse response, Account account,
                               String authTokenType, Bundle loginOptions) throws NetworkErrorException {
        Log.v(TAG, "getAuthToken()");

        // If the caller requested an authToken type we don't support, then
        // return an error
        if (!authTokenType.equals(mContext.getString(R.string.accountType))) {
            final Bundle result = new Bundle();
            result.putString(AccountManager.KEY_ERROR_MESSAGE, "invalid authTokenType");
            return result;
        }

        // Extract the username and password from the Account Manager, and ask
        // the server for an appropriate AuthToken.
        final AccountManager am = AccountManager.get(mContext);
        final String password = am.getPassword(account);
        if (password != null) {
            Set<String> cookies = authenticate(mContext, account.name, password);
            if (cookies != null && !cookies.isEmpty()) {
                final Bundle result = new Bundle();
                result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
                result.putString(AccountManager.KEY_AUTHTOKEN, TextUtils.join("$$$", cookies));
                return result;
            }
        }

        // If we get here, then we couldn't access the user's password - so we
        // need to re-prompt them for their credentials. We do that by creating
        // an intent to display our AuthenticatorActivity panel.
        final Intent intent = new Intent(mContext, LoginActivity.class);
        intent.putExtra(LoginActivity.EXTRA_LOGIN, account.name);
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
        final Bundle bundle = new Bundle();
        bundle.putParcelable(AccountManager.KEY_INTENT, intent);
        return bundle;
    }

    @Override
    public String getAuthTokenLabel(String authTokenType) {
        // null means we don't support multiple authToken types
        Log.v(TAG, "getAuthTokenLabel()");
        return null;
    }

    @Override
    public Bundle hasFeatures(
            AccountAuthenticatorResponse response, Account account, String[] features) {
        // This call is used to query whether the Authenticator supports
        // specific features. We don't expect to get called, so we always
        // return false (no) for any queries.
        Log.v(TAG, "hasFeatures()");
        final Bundle result = new Bundle();
        result.putBoolean(AccountManager.KEY_BOOLEAN_RESULT, false);
        return result;
    }

    @Override
    public Bundle updateCredentials(AccountAuthenticatorResponse response, Account account,
                                    String authTokenType, Bundle loginOptions) {
        Log.v(TAG, "updateCredentials()");
        return null;
    }

    public static boolean isLoggedIn(Activity activity) {
        try {
            return !getLocalCookies(activity).isEmpty();
        } catch (Exception e) {
            Log.w(Authenticator.class.getSimpleName(), e.getMessage(), e);
            return false;
        }
    }

    public static Set<String> getCookies(Context context) {
        return getCookies(context, getAccount(context));
    }

    private static String getPassword(Context context) {
        return AccountManager.get(context).getPassword(getAccount(context));
    }

    public static Set<String> getLocalCookies(Activity activity) {
        return getLocalCookies(activity, getAccount(activity));
    }

    private static Account getAccount(Context context) {
        return new Account(PreferenceHelper.getLogin(context), context.getString(R.string.accountType));
    }

    private static Set<String> getLocalCookies(Activity activity, Account account) {
        try {
            AccountManager accountManager = AccountManager.get(activity);
            return extractCookies(accountManager.getAuthToken(account, activity.getString(R.string.accountType), null, activity, null, null).getResult().getString(AccountManager.KEY_AUTHTOKEN));
        } catch (Exception e) {
            Log.e(Authenticator.class.getSimpleName(), e.getMessage(), e);
        }
        return new HashSet<String>();
    }

    private static Set<String> extractCookies(String cookieString) {
        HashSet<String> cookies = new HashSet<String>();
        for (String cookie : cookieString.split("&&&")) {
            cookies.add(cookie);
        }
        return cookies;
    }


    public static Set<String> authenticate(Context context, String login, String password) {
        Set<String> cookies = new HashSet<String>();
        try {
            cookies = new BanqClient().authenticate(login, password);
            Account account = new Account(login, context.getString(R.string.accountType));
            AccountManager accountManager = AccountManager.get(context);
            //If the account already exists, we update the account
            if (!accountManager.addAccountExplicitly(account, password, null)) {
                accountManager.setPassword(account, password);
            }
            PreferenceHelper.saveLogin(context, login);
        } catch (Exception e) {
            Log.e(Authenticator.class.getSimpleName(), e.getMessage(), e);
        }
        return cookies;
    }

    public static Set<String> getCookies(Context mContext, Account account) {
        try {
            return extractCookies(AccountManager.get(mContext).blockingGetAuthToken(account, mContext.getString(R.string.accountType), true));
        } catch (Exception e) {
            Log.e(Authenticator.class.getSimpleName(), e.getMessage(), e);
        }
        return new HashSet<String>();
    }
    public static Set<String> authenticate(Context mContext) {
        return authenticate(mContext, PreferenceHelper.getLogin(mContext), getPassword(mContext));
    }
}
