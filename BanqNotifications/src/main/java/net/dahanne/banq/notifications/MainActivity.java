package net.dahanne.banq.notifications;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.widget.TextView;
import android.widget.Toast;

import net.dahanne.banq.BanqClient;
import net.dahanne.banq.model.Details;

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

    public static Intent newIntent(Context context) {
        return new Intent(context, MainActivity.class);
    }


    class RetrieveInfosAsyncTask extends AsyncTask<String, Void, Details> {
        private Exception exceptionCaught;

        @Override
        protected Details doInBackground(String[] params) {
            BanqClient bc = new BanqClient();
            try {
                Set<String> cookies = PreferenceHelper.getCookies(MainActivity.this);
                String detailsPage = bc.getDetailsPage(cookies);
                return bc.parseDetails(detailsPage);
            } catch (Exception e) {
                exceptionCaught = e;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Details details) {
            if (exceptionCaught == null && details != null) {
                userName.setText(String.format(getString(R.string.name), details.getName()));
                currentDebt.setText(String.format(getString(R.string.currentDebt), details.getCurrentDebt()));
                expirationDate.setText(String.format(getString(R.string.expirationDebt), details.getExpirationDate()));
                setListAdapter(new BorrowedItemAdapter(MainActivity.this, details.getBorrowedItems()));
            } else {
                Toast.makeText(MainActivity.this, exceptionCaught.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

}
