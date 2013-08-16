package net.dahanne.banq.notifications;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import net.dahanne.banq.model.BorrowedItem;

/**
 * Created by guilhem.demiollis on 13-08-16.
 */
public class NotificationHelper {

    public static void launchNotification(Context context, BorrowedItem borrowedItem) {
        // Prepare intent which is triggered if the
// notification is selected

        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(context, 0, intent, 0);

// Build notification
// Actions are just fake
        Notification noti = new Notification.Builder(context)
                .setContentTitle(context.getString(R.string.item_to_return_to_banq))
                .setContentText(borrowedItem.getTitle())
                .setSubText(borrowedItem.getToBeReturnedBefore().toString())
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentIntent(pIntent)
                .addAction(android.R.drawable.ic_menu_more, context.getString(R.string.see_all_items), pIntent)
                .addAction(android.R.drawable.ic_menu_rotate, context.getString(R.string.renew), pIntent).build();


        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);

// Hide the notification after its selected
        noti.flags |= Notification.FLAG_AUTO_CANCEL;

        notificationManager.notify(0, noti);
    }
}
