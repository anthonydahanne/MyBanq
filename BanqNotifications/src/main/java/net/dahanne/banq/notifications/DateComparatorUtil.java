package net.dahanne.banq.notifications;

import android.content.Context;
import android.graphics.Color;
import android.view.View;

/**
 * Created by guilhem.demiollis on 13-08-20.
 */
public class DateComparatorUtil {

    private static long _1_week = 1000l * 60l * 60l * 24l * 7l;

    public static int getExpirationColor(long remainingDays) {
        if (remainingDays >= 30) {
            return Color.GREEN;
        } else if (remainingDays >= 7) {
            return Color.YELLOW;
        } else {
            return Color.RED;
        }
    }

    public static int getBorrowColor(long dayRemaining) {
        if (dayRemaining >= 7) {
            return Color.GREEN;
        } else if (dayRemaining >= 3) {
            return Color.YELLOW;
        } else {
            return Color.RED;
        }
    }

    public static int getRenewVisibility(Context context, long remainingDays) {
        return remainingDays >= PreferenceHelper.getDaysToTrigger(context) ? View.GONE : View.VISIBLE;
    }

    public static boolean shouldPopNotification(Context context, long diffDays) {
        return PreferenceHelper.getDaysToTrigger(context) >= diffDays;
    }
}
