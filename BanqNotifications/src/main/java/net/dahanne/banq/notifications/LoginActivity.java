package net.dahanne.banq.notifications;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import net.dahanne.banq.BanqClient;

import java.util.Set;

import static android.accounts.AccountManager.*;

/**
 * Activity which displays a mLogin screen to the user, offering registration as
 * well.
 */
public class LoginActivity extends AccountAuthenticatorActivity {

    /**
     * The default email to populate the email field with.
     */
    public static final String EXTRA_LOGIN = "com.example.android.authenticatordemo.extra.EMAIL";


    /**
     * Keep track of the mLogin task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // Values for email and password at the time of the mLogin attempt.
    private String mLogin;
    private String mPassword;

    // UI references.
    private EditText mLoginView;
    private EditText mPasswordView;
    private View mLoginFormView;
    private View mLoginStatusView;
    private TextView mLoginStatusMessageView;

    public static Intent newIntent(Context context) {
        return new Intent(context, LoginActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (PreferenceHelper.isLoggedIn(this)) {
            startActivity(MainActivity.newIntent(LoginActivity.this));
            finish();
        }

        setContentView(R.layout.activity_login);

        // Set up the login form.
        mLogin = getIntent().getStringExtra(EXTRA_LOGIN);
        mLoginView = (EditText) findViewById(R.id.email);
        mLoginView.setText(mLogin);

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin(findViewById(R.id.sign_in_button));
                    return true;
                }
                return false;
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mLoginStatusView = findViewById(R.id.login_status);
        mLoginStatusMessageView = (TextView) findViewById(R.id.login_status_message);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.login, menu);
        return true;
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void attemptLogin(View v) {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mLoginView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        mLogin = mLoginView.getText().toString();
        mPassword = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password.
        if (TextUtils.isEmpty(mPassword)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        } else if (mPassword.length() < 4) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(mLogin)) {
            mLoginView.setError(getString(R.string.error_field_required));
            focusView = mLoginView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            mLoginStatusMessageView.setText(R.string.login_progress_signing_in);
            showProgress(true);
            mAuthTask = new UserLoginTask();
            mAuthTask.execute(mLogin, mPassword);
        }
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginStatusView.setVisibility(View.VISIBLE);
            mLoginStatusView.animate()
                    .setDuration(shortAnimTime)
                    .alpha(show ? 1 : 0)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
                        }
                    });

            mLoginFormView.setVisibility(View.VISIBLE);
            mLoginFormView.animate()
                    .setDuration(shortAnimTime)
                    .alpha(show ? 0 : 1)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                        }
                    });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }


    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<String, Void, Void> {
        private Exception exceptionCaught;

        @Override
        protected Void doInBackground(String... params) {

            try {
                String login = params[0];
                String password = params[1];
                Set<String> cookies = new BanqClient().authenticate(login, password);
                PreferenceHelper.saveCookies(LoginActivity.this, cookies);
                PreferenceHelper.saveUsername(LoginActivity.this, login);
                PreferenceHelper.savePassword(LoginActivity.this, password);
                Account account = new Account(login, getString(R.string.accountType));
                AccountManager accountManager = AccountManager.get(LoginActivity.this);
                //If the account already exists, we update the account
                if (!accountManager.addAccountExplicitly(account, password, null)) {
                    accountManager.setPassword(account, password);
                }
                final Intent intent = new Intent();
                intent.putExtra(KEY_ACCOUNT_NAME, login);
                intent.putExtra(KEY_ACCOUNT_TYPE, getString(R.string.accountType));
                setAccountAuthenticatorResult(intent.getExtras());
                setResult(RESULT_OK, intent);
            } catch (Exception e) {
                Log.e(getClass().getSimpleName(), e.getMessage(), e);
                exceptionCaught = e;
            }
            return null;
        }

        @Override
        protected void onPostExecute(final Void success) {
            mAuthTask = null;
            showProgress(false);

            if (exceptionCaught == null) {
                startActivity(MainActivity.newIntent(LoginActivity.this));
                finish();
            } else {
                mPasswordView.setError(exceptionCaught.getMessage());
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
}
