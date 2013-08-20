package net.dahanne.banq.notifications;

import android.accounts.AccountManager;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import net.dahanne.banq.BanqClient;
import net.dahanne.banq.exceptions.InvalidSessionException;
import net.dahanne.banq.model.BorrowedItem;
import net.dahanne.banq.model.Details;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Set;

public class MainActivity extends Activity {

    private TextView userName;
    private TextView currentDebt;
    private TextView expirationDate;
    private long _1_month = 1000l * 60l * 60l * 24l * 30l;
    private long _1_week = 1000l * 60l * 60l * 24l * 7l;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (TextUtils.isEmpty(PreferenceHelper.getLogin(this))) {
            startActivity(LoginActivity.newIntent(this, true));
            finish();
        } else {
            setContentView(R.layout.activity_main);
            userName = (TextView) findViewById(R.id.userName);
            currentDebt = (TextView) findViewById(R.id.currentDebt);
            expirationDate = (TextView) findViewById(R.id.expirationDate);
            showProgress(true);
            new RetrieveInfosAsyncTask().execute();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                startActivity(PreferencesActivity.newIntent(this));
//            case R.id.action_test_notification:
//                NotificationHelper.launchNotification(this, new BorrowedItem("Book title", "BTU", new Date(), new Date(), "", ""));
        }
        return super.onMenuItemSelected(featureId, item);
    }


    public static Intent newIntent(Context context) {
        return new Intent(context, MainActivity.class);
    }


    class RetrieveInfosAsyncTask extends AsyncTask<String, Void, Details> {
        private Exception exceptionCaught;

        @Override
        protected Details doInBackground(String[] params) {
            BanqClient bc = new BanqClient();
            Details details = null;
            try {
                Log.i(getClass().getSimpleName(), "Récupération des cookies");
                Set<String> cookies = Authenticator.getCookies(MainActivity.this);
                Log.i(getClass().getSimpleName(), "Cookies récupérés");
                try {
                    Log.i(getClass().getSimpleName(), "Récupération du détail");
                    details = bc.getDetails(cookies);
                    Log.i(getClass().getSimpleName(), "Cookies récupérés");
                } catch (InvalidSessionException ise) {
                    Log.i(getClass().getSimpleName(), "Session invalide : reconnexion");
                    cookies = Authenticator.authenticate(MainActivity.this);
                    Log.i(getClass().getSimpleName(), "Réconnecté avec cookies -> Récupération du détail");
                    details = bc.getDetails(cookies);
                    Log.i(getClass().getSimpleName(), "Détail récupéré");
                }
            } catch (Exception e) {
                Log.e(getClass().getSimpleName(), e.getMessage(), e);
                exceptionCaught = e;
            }
            return details;
        }

        @Override
        protected void onPostExecute(Details details) {
            showProgress(false);
            if (exceptionCaught == null && details != null) {
                userName.setText(String.format(getString(R.string.name), details.getName()));
                Spannable debt = new SpannableString(String.format(getString(R.string.currentDebt), details.getCurrentDebt()));
                debt.setSpan(new ForegroundColorSpan(Color.RED), debt.length() - 5, debt.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                currentDebt.setText(debt);
                Spannable expiration = new SpannableString(String.format(getString(R.string.expirationDebt), DateFormat.getDateInstance().format(details.getExpirationDate())));
                expiration.setSpan(new ForegroundColorSpan(getColor(details.getExpirationDate())), expiration.length() - 10, expiration.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                expirationDate.setText(expiration);
                ((GridView) findViewById(android.R.id.list)).setAdapter(new BorrowedItemAdapter(MainActivity.this, details.getBorrowedItems()));
            } else if (exceptionCaught == null) {
                Toast.makeText(MainActivity.this, getString(R.string.unexpectedError), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, exceptionCaught.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }

        private int getColor(Date expirationDate) {
            if (expirationDate.getTime() - Calendar.getInstance().getTimeInMillis() > _1_month) {
                return Color.GREEN;
            } else if (expirationDate.getTime() - Calendar.getInstance().getTimeInMillis() > _1_week) {
                return Color.YELLOW;
            } else {
                return Color.RED;
            }
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
        final View detailStatusView = findViewById(R.id.detail_status);
        final View userInfosView = findViewById(R.id.user_infos);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            detailStatusView.setVisibility(View.VISIBLE);
            detailStatusView.animate()
                    .setDuration(shortAnimTime)
                    .alpha(show ? 1 : 0)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            detailStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
                        }
                    });

            userInfosView.setVisibility(View.VISIBLE);
            userInfosView.animate()
                    .setDuration(shortAnimTime)
                    .alpha(show ? 0 : 1)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            userInfosView.setVisibility(show ? View.GONE : View.VISIBLE);
                        }
                    });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            detailStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
            userInfosView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

}
