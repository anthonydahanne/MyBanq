package net.dahanne.banq.notifications;

import android.content.Context;
import android.graphics.Color;
import android.view.View;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;

import static net.dahanne.banq.notifications.DateComparatorUtil.BLUE_BANQ;
import static net.dahanne.banq.notifications.DateComparatorUtil.GREEN_BANQ;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by guilhem.demiollis on 13-08-20.
 */
@RunWith(RobolectricGradleTestRunner.class)
public class DateComparatorUtilTest {

    @Test
    public void testGetExpirationColor() {
        assertEquals(BLUE_BANQ, DateComparatorUtil.getExpirationColor(40));
        assertEquals(BLUE_BANQ, DateComparatorUtil.getExpirationColor(35));
        assertEquals(BLUE_BANQ, DateComparatorUtil.getExpirationColor(30));
        assertEquals(GREEN_BANQ, DateComparatorUtil.getExpirationColor(25));
        assertEquals(GREEN_BANQ, DateComparatorUtil.getExpirationColor(20));
        assertEquals(GREEN_BANQ, DateComparatorUtil.getExpirationColor(15));
        assertEquals(GREEN_BANQ, DateComparatorUtil.getExpirationColor(10));
        assertEquals(GREEN_BANQ, DateComparatorUtil.getExpirationColor(7));
        assertEquals(Color.RED, DateComparatorUtil.getExpirationColor(6));
        assertEquals(Color.RED, DateComparatorUtil.getExpirationColor(5));
        assertEquals(Color.RED, DateComparatorUtil.getExpirationColor(1));
        assertEquals(Color.RED, DateComparatorUtil.getExpirationColor(0));
        assertEquals(Color.RED, DateComparatorUtil.getExpirationColor(-1));
        assertEquals(Color.RED, DateComparatorUtil.getExpirationColor(-10));
    }

    @Test
    public void testGetBorrowColor() {
        assertEquals(BLUE_BANQ, DateComparatorUtil.getBorrowColor(10));
        assertEquals(BLUE_BANQ, DateComparatorUtil.getBorrowColor(8));
        assertEquals(BLUE_BANQ, DateComparatorUtil.getBorrowColor(7));
        assertEquals(GREEN_BANQ, DateComparatorUtil.getBorrowColor(6));
        assertEquals(GREEN_BANQ, DateComparatorUtil.getBorrowColor(5));
        assertEquals(GREEN_BANQ, DateComparatorUtil.getBorrowColor(4));
        assertEquals(GREEN_BANQ, DateComparatorUtil.getBorrowColor(3));
        assertEquals(Color.RED, DateComparatorUtil.getBorrowColor(2));
        assertEquals(Color.RED, DateComparatorUtil.getBorrowColor(1));
        assertEquals(Color.RED, DateComparatorUtil.getBorrowColor(0));
        assertEquals(Color.RED, DateComparatorUtil.getBorrowColor(-1));
    }

    @Test
    public void testGetRenewVisibility() {
        Context targetContext = Robolectric.application;
        assertEquals(View.GONE, DateComparatorUtil.getRenewVisibility(targetContext, 10));
        assertEquals(View.GONE, DateComparatorUtil.getRenewVisibility(targetContext, 8));
        assertEquals(View.GONE, DateComparatorUtil.getRenewVisibility(targetContext, 7));
        assertEquals(View.GONE, DateComparatorUtil.getRenewVisibility(targetContext, 6));
        assertEquals(View.GONE, DateComparatorUtil.getRenewVisibility(targetContext, 5));
        assertEquals(View.GONE, DateComparatorUtil.getRenewVisibility(targetContext, 4));
        assertEquals(View.GONE, DateComparatorUtil.getRenewVisibility(targetContext, 3));
        assertEquals(View.VISIBLE, DateComparatorUtil.getRenewVisibility(targetContext, 2));
        assertEquals(View.VISIBLE, DateComparatorUtil.getRenewVisibility(targetContext, 1));
        assertEquals(View.VISIBLE, DateComparatorUtil.getRenewVisibility(targetContext, 0));
        assertEquals(View.VISIBLE, DateComparatorUtil.getRenewVisibility(targetContext, -1));
    }

    @Test
    public void testShouldPopNotification() {
        Context targetContext = Robolectric.application;
        assertEquals(3, PreferenceHelper.getDaysToTrigger(targetContext));
        assertFalse(DateComparatorUtil.shouldPopNotification(targetContext, 10));
        assertFalse(DateComparatorUtil.shouldPopNotification(targetContext, 8));
        assertFalse(DateComparatorUtil.shouldPopNotification(targetContext, 7));
        assertFalse(DateComparatorUtil.shouldPopNotification(targetContext, 6));
        assertFalse(DateComparatorUtil.shouldPopNotification(targetContext, 5));
        assertFalse(DateComparatorUtil.shouldPopNotification(targetContext, 4));
        assertTrue(DateComparatorUtil.shouldPopNotification(targetContext, 3));
        assertTrue(DateComparatorUtil.shouldPopNotification(targetContext, 2));
        assertTrue(DateComparatorUtil.shouldPopNotification(targetContext, 1));
        assertTrue(DateComparatorUtil.shouldPopNotification(targetContext, 0));
        assertTrue(DateComparatorUtil.shouldPopNotification(targetContext, -1));
    }
}
