package net.dahanne.banq.notifications;

import android.accounts.Account;
import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import net.dahanne.banq.BanqClient;
import net.dahanne.banq.exceptions.FailedToRenewException;
import net.dahanne.banq.exceptions.InvalidSessionException;
import net.dahanne.banq.model.BorrowedItem;

import java.io.IOException;
import java.text.DateFormat;

public class PlusActivity extends Activity {

    private TextView titleView;
    private TextView authorView;
    private TextView locationView;
    private TextView feesView;
    private TextView borrowedView;
    private TextView toBeReturnedView;
    private Button renewButton;
    private Account account;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final BorrowedItem borrowedItem = (BorrowedItem) getIntent().getSerializableExtra(BorrowedItemAdapter.ITEM);
        account = getIntent().getParcelableExtra(BorrowedItemAdapter.ACCOUNT);
        if (borrowedItem == null) {
            return;
        }

        DateFormat dateFormat = DateFormat.getDateInstance(java.text.DateFormat.FULL);
        setContentView(R.layout.activity_plus);
        titleView = (TextView) findViewById(R.id.titleView);
        titleView.setText(borrowedItem.getTitle());
        authorView = (TextView) findViewById(R.id.authorView);
        authorView.setText(borrowedItem.getAuthorInfo());
        locationView = (TextView) findViewById(R.id.locationView);
        locationView.setText(borrowedItem.getDocumentLocation());
        feesView = (TextView) findViewById(R.id.feesView);
        feesView.setText(borrowedItem.getLateFees());
        borrowedView = (TextView) findViewById(R.id.borrowedView);
        CharSequence borrowedDateAsString = dateFormat.format(borrowedItem.getBorrowedDate());
        borrowedView.setText(borrowedDateAsString);
        toBeReturnedView = (TextView) findViewById(R.id.toBeReturnedView);
        CharSequence toBeReturnedBeforeString = dateFormat.format(borrowedItem.getToBeReturnedBefore());
        toBeReturnedView.setText(toBeReturnedBeforeString);
        renewButton = (Button) findViewById(R.id.renewButton);
        renewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new RenewAsyncTask(PlusActivity.this).execute(borrowedItem.getObjId(), Integer.toString(borrowedItem.getItemPosition()));
            }
        });
        if (!borrowedItem.isRenewable()) {
            renewButton.setText(this.getString(R.string.not_renewable));
            renewButton.setEnabled(false);
        }
        TableLayout tableLayout = (TableLayout) findViewById(R.id.itemTableLayout);
        if (TextUtils.isEmpty(borrowedItem.getAuthorInfo())) {
            TableRow row = (TableRow) findViewById(R.id.authorInfoRow);
            tableLayout.removeView(row);
        }
        if (TextUtils.isEmpty(borrowedItem.getLateFees())) {
            TableRow row = (TableRow) findViewById(R.id.lateFeesRow);
            tableLayout.removeView(row);
        }
    }

    class RenewAsyncTask extends AsyncTask<String, Void, Void> {
        private final Context context;
        private Exception exceptionCaught;

        public RenewAsyncTask(Context context) {
            this.context = context;
        }

        @Override
        protected Void doInBackground(String... params) {
            String objId = params[0];
            String itemPosition = params[1];
            BanqClient bc;
            try {
                bc = Authenticator.getBanqClient(context, account);
                bc.renew(objId, Integer.valueOf(itemPosition));
            } catch (FailedToRenewException e) {
                exceptionCaught = e;
            } catch (IOException e) {
                exceptionCaught = e;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void nothing) {
            if (exceptionCaught == null) {
                Toast.makeText(context, context.getString(R.string.renewal_successful), Toast.LENGTH_SHORT).show();
                PlusActivity.this.finish();
            } else if (exceptionCaught instanceof InvalidSessionException) {
                Toast.makeText(context, context.getString(R.string.invalid_session), Toast.LENGTH_SHORT).show();
//                ((MainActivity) getContext()).backToLogin();
            } else if (exceptionCaught instanceof FailedToRenewException) {
                Toast.makeText(context, Html.fromHtml(exceptionCaught.getMessage()), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, context.getString(R.string.unexpectedError), Toast.LENGTH_SHORT).show();
            }
        }
    }


}
