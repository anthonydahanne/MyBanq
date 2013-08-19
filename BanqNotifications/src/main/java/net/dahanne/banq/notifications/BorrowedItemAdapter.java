package net.dahanne.banq.notifications;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import net.dahanne.banq.BanqClient;
import net.dahanne.banq.exceptions.FailedToRenewException;
import net.dahanne.banq.exceptions.InvalidSessionException;
import net.dahanne.banq.model.BorrowedItem;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;

public class BorrowedItemAdapter extends ArrayAdapter<BorrowedItem> {

    private ViewHolder holder;

    public BorrowedItemAdapter(Context context, List<BorrowedItem> borrowedItems) {
        super(context, R.layout.borrowed_item, R.id.bookName, borrowedItems);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.borrowed_item, null);
            holder = new ViewHolder();
            holder.name = (TextView) convertView.findViewById(R.id.bookName);
            holder.shelfMark = (TextView) convertView.findViewById(R.id.shelfMark);
            holder.remainingDays = (TextView) convertView.findViewById(R.id.remainingDays);
            holder.renewButton = (Button) convertView.findViewById(R.id.renewButton);
            holder.separator = convertView.findViewById(R.id.separator);
            convertView.setTag(holder);
        }
        holder = (ViewHolder) convertView.getTag();
        final BorrowedItem item = getItem(position);
        holder.name.setText(item.getTitle());
        holder.shelfMark.setText(item.getShelfMark());
        Spannable daysRemainig = new SpannableString(String.format(getContext().getString(R.string.daysRemaining), item.getRemaingDays()));
        daysRemainig.setSpan(new ForegroundColorSpan(getColor(item.getRemaingDays())), 0, daysRemainig.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        holder.remainingDays.setText(daysRemainig);
        holder.renewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new RenewAsyncTask(getContext()).execute(item.getUserID(), item.getDocNo());
            }
        });
        holder.renewButton.setVisibility(item.getRemaingDays() > 7 ? View.GONE : View.VISIBLE);
        holder.separator.setVisibility(item.getRemaingDays() > 7 ? View.GONE : View.VISIBLE);
        return convertView;
    }

    private int getColor(int dayRemaining) {
        if (dayRemaining > 7) {
            return Color.GREEN;
        } else if (dayRemaining > 3) {
            return Color.YELLOW;
        } else {
            return Color.RED;
        }
    }

    private class ViewHolder {
        public TextView name;
        public TextView shelfMark;
        public TextView remainingDays;
        public Button renewButton;
        public View separator;
    }

    class RenewAsyncTask extends AsyncTask<String, Void, Void> {
        private final Context context;
        private Exception exceptionCaught;

        public RenewAsyncTask(Context context) {
            this.context = context;
        }

        @Override
        protected Void doInBackground(String... params) {
            BanqClient bc = new BanqClient();
            try {
                Set<String> cookies = Authenticator.getCookies(context);

                String userID = params[0];
                String docNo = params[1];

                try {
                    bc.renew(cookies, userID, docNo);
                } catch (InvalidSessionException ise) {
                    cookies = Authenticator.authenticate(context);
                    bc.renew(cookies, userID, docNo);
                }
            } catch (Exception e) {
                exceptionCaught = e;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void nothing) {
            if (exceptionCaught == null) {
                Toast.makeText(context, context.getString(R.string.renewal_successful), Toast.LENGTH_SHORT).show();
                // TODO : revenir a l'activite principale et rafraichir les emprunts
            } else if (exceptionCaught == null) {
                Toast.makeText(context, context.getString(R.string.unexpectedError), Toast.LENGTH_SHORT).show();
            } else if (exceptionCaught instanceof InvalidSessionException) {
                Toast.makeText(context, context.getString(R.string.invalid_session), Toast.LENGTH_SHORT).show();
            } else if (exceptionCaught instanceof FailedToRenewException) {
                Toast.makeText(context, exceptionCaught.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }
}
