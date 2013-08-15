package net.dahanne.banq.notifications;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import net.dahanne.banq.BanqClient;
import net.dahanne.banq.model.BorrowedItem;
import net.dahanne.banq.model.Details;

import java.util.Set;

public class MainActivity extends Activity {

    private EditText resultField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final EditText usernameField = (EditText) findViewById(R.id.usernameField);
        final EditText passwordField = (EditText) findViewById(R.id.passwordField);
        final Button submitButton = (Button) findViewById(R.id.submitButton);
        resultField = (EditText) findViewById(R.id.resultField);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DoItAllAsyncTask().execute(usernameField.getText().toString(), passwordField.getText().toString());
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }


    class DoItAllAsyncTask extends AsyncTask<String, Void, Details> {

        private String username;
        private String password;
        private Exception exceptionCaught;

        @Override
        protected Details doInBackground(String[] params) {
            this.username = params[0];
            this.password = params[1];
            BanqClient bc = new BanqClient();
            Details details = null;
            try {
                Set<String> cookies = bc.authenticate(username, password);
                String detailsPage = bc.getDetailsPage(cookies);
                details = bc.parseDetails(detailsPage);
            } catch (Exception e) {
                exceptionCaught = e;
            }
            return details;
        }

        @Override
        protected void onPostExecute(Details details) {
            if (exceptionCaught != null) {
                Toast.makeText(MainActivity.this, exceptionCaught.getMessage(), Toast.LENGTH_SHORT).show();
            } else if (details != null) {
                StringBuilder detailsAsText = new StringBuilder();

                detailsAsText.append("Name : ").append(details.getName()).append("\n");
                detailsAsText.append("Current Debt : ").append(details.getCurrentDebt()).append("\n");
                detailsAsText.append("Expiration Date : ").append(details.getExpirationDate()).append("\n");

                detailsAsText.append("--List of items--").append("\n");


                for (BorrowedItem borrowedItem : details.getBorrowedItems()) {
                    detailsAsText.append("Name : ").append(borrowedItem.getTitle()).append("\n");
                    detailsAsText.append("Shelf Mark : ").append(borrowedItem.getShelfMark()).append("\n");
                    detailsAsText.append("Borrowed date : ").append(borrowedItem.getBorrowedDate()).append("\n");
                    detailsAsText.append("To be returned before date : ").append(borrowedItem.getToBeReturnedBefore()).append("\n");
                    detailsAsText.append("-------------").append("\n");
                }

                resultField.setText(detailsAsText.toString());
            }
        }
    }

}
