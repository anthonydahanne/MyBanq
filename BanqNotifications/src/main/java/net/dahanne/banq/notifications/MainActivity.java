package net.dahanne.banq.notifications;

import android.accounts.AccountManager;
import android.app.ListActivity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import net.dahanne.banq.BanqClient;
import net.dahanne.banq.exceptions.InvalidSessionException;
import net.dahanne.banq.model.BorrowedItem;
import net.dahanne.banq.model.Details;

import java.util.Date;
import java.util.Set;

public class MainActivity extends ListActivity {

    private TextView userName;
    private TextView currentDebt;
    private TextView expirationDate;
    private AccountManager accountManager;

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
            new RetrieveInfosAsyncTask().execute();
            accountManager = AccountManager.get(this);
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
            case R.id.action_test_notification:
                NotificationHelper.launchNotification(this, new BorrowedItem("Book title", "BTU", new Date(), new Date()));
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
                Set<String> cookies = Authenticator.getCookies(MainActivity.this);
                try {
                    details = bc.getDetails(cookies);
                } catch (InvalidSessionException ise) {
                    cookies = Authenticator.authenticate(MainActivity.this);
                    details = bc.getDetails(cookies);
                }
            } catch (Exception e) {
                exceptionCaught = e;
            }
            return details;
        }

        @Override
        protected void onPostExecute(Details details) {
            if (exceptionCaught == null && details != null) {
                userName.setText(String.format(getString(R.string.name), details.getName()));
                currentDebt.setText(String.format(getString(R.string.currentDebt), details.getCurrentDebt()));
                expirationDate.setText(String.format(getString(R.string.expirationDebt), details.getExpirationDate()));
                setListAdapter(new BorrowedItemAdapter(MainActivity.this, details.getBorrowedItems()));
            } else if (exceptionCaught == null) {
                Toast.makeText(MainActivity.this, getString(R.string.unexpectedError), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, exceptionCaught.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

}
