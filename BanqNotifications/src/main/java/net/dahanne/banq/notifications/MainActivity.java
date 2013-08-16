package net.dahanne.banq.notifications;

import android.app.ListActivity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        userName = (TextView) findViewById(R.id.userName);
        currentDebt = (TextView) findViewById(R.id.currentDebt);
        expirationDate = (TextView) findViewById(R.id.expirationDate);
        new RetrieveInfosAsyncTask().execute();
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
                launchNotification(new BorrowedItem("Book title","BTU",new Date(), new Date()));
        }
        return super.onMenuItemSelected(featureId, item);
    }

    private void launchNotification(BorrowedItem borrowedItem) {
        // Prepare intent which is triggered if the
// notification is selected

        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);

// Build notification
// Actions are just fake
        Notification noti = new Notification.Builder(this)
                .setContentTitle(getString(R.string.item_to_return_to_banq))
                .setContentText(borrowedItem.getTitle())
                .setSubText(borrowedItem.getToBeReturnedBefore().toString())
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentIntent(pIntent)
                .addAction(android.R.drawable.ic_menu_more, getString(R.string.see_all_items), pIntent)
                .addAction(android.R.drawable.ic_menu_rotate, getString(R.string.renew), pIntent).build();


        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

// Hide the notification after its selected
        noti.flags |= Notification.FLAG_AUTO_CANCEL;

        notificationManager.notify(0, noti);
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
                Set<String> cookies = PreferenceHelper.getCookies(MainActivity.this);
                try {
                    details = bc.getDetails(cookies);
                } catch (InvalidSessionException ise) {
                    cookies = bc.authenticate(PreferenceHelper.getUsername(MainActivity.this), PreferenceHelper.getPassword(MainActivity.this));
                    PreferenceHelper.saveCookies(MainActivity.this, cookies);
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
                PreferenceHelper.saveLogin(MainActivity.this, details.getName());
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
