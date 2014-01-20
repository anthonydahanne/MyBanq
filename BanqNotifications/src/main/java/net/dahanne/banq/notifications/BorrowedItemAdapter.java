package net.dahanne.banq.notifications;

import android.accounts.Account;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.Html;
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

import net.dahanne.banq.model.BorrowedItem;

import java.util.List;

public class BorrowedItemAdapter extends ArrayAdapter<BorrowedItem> {

    public static final String ITEM = "net.dahanne.banq.notifications.Item";
    public static final String ACCOUNT = "net.dahanne.banq.notifications.Account";
    private ViewHolder holder;
    private Account account;

    public BorrowedItemAdapter(Context context, List<BorrowedItem> borrowedItems, Account account) {
        super(context, R.layout.borrowed_item, R.id.bookName, borrowedItems);
        this.account = account;
    }

    @Override
    public View getView(int position, View convertView, final ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.borrowed_item, parent, false);
            holder = new ViewHolder();
            holder.name = (TextView) convertView.findViewById(R.id.bookName);
            holder.remainingDays = (TextView) convertView.findViewById(R.id.remainingDays);
            holder.plusButton = (Button) convertView.findViewById(R.id.plusButton);
            holder.separator = convertView.findViewById(R.id.separator);
            holder.renewable = (TextView) convertView.findViewById(R.id.renewable);
            convertView.setTag(holder);
        }
        holder = (ViewHolder) convertView.getTag();
        final BorrowedItem item = getItem(position);
        Spanned titleFromHtml = Html.fromHtml(item.getTitle());
        holder.name.setText(titleFromHtml.toString(), TextView.BufferType.SPANNABLE);
        Spannable daysRemaining = new SpannableString(String.format(getContext().getString(R.string.daysRemaining), item.getRemainingDays()));
        daysRemaining.setSpan(new ForegroundColorSpan(DateComparatorUtil.getBorrowColor(item.getRemainingDays())), 0, daysRemaining.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        holder.remainingDays.setText(daysRemaining);
        holder.plusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(parent.getContext(), PlusActivity.class);
                intent.putExtra(ITEM, item);
                intent.putExtra(ACCOUNT, account);

                parent.getContext().startActivity(intent);
//                new RenewAsyncTask(getContext()).execute(item.getItemPosition());
            }
        });
//        int renewVisibility = DateComparatorUtil.getRenewVisibility(getContext(), item.getRemainingDays());
        holder.plusButton.setVisibility(View.VISIBLE);
        holder.separator.setVisibility(View.VISIBLE);
        if (item.isRenewable()) {
            holder.renewable.setTextColor(Color.GREEN);
        } else {
            holder.renewable.setPaintFlags(holder.renewable.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.renewable.setTextColor(Color.RED);
        }
        return convertView;
    }

    private class ViewHolder {
        public TextView name;
        public TextView remainingDays;
        public Button plusButton;
        public View separator;
        public TextView renewable;
    }


}
