package net.dahanne.banq.notifications;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import net.dahanne.banq.model.BorrowedItem;

import java.util.List;

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
            holder.borrowedDate = (TextView) convertView.findViewById(R.id.borrowedDate);
            holder.toBeReturnedDate = (TextView) convertView.findViewById(R.id.toBeReturnedDate);
            convertView.setTag(holder);
        }
        holder = (ViewHolder) convertView.getTag();
        BorrowedItem book = getItem(position);
        holder.name.setText(book.getTitle());
        holder.shelfMark.setText(book.getShelfMark());
        holder.borrowedDate.setText(book.getBorrowedDate().toString());
        holder.toBeReturnedDate.setText(book.getToBeReturnedBefore().toString());
        return convertView;
    }

    private class ViewHolder {
        public TextView name;
        public TextView shelfMark;
        public TextView borrowedDate;
        public TextView toBeReturnedDate;
    }
}
