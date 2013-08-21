package net.dahanne.banq.notifications;

import android.content.Context;
import android.graphics.Color;
import android.test.InstrumentationTestCase;
import android.view.View;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.robolectric.Robolectric;

/**
 * Created by guilhem.demiollis on 13-08-20.
 */
@RunWith(RobolectricGradleTestRunner.class)
public class DateComparatorUtilTest {

    @Test
    public void testGetExpirationColor() {
        assertEquals(Color.GREEN, DateComparatorUtil.getExpirationColor(40));
        assertEquals(Color.GREEN, DateComparatorUtil.getExpirationColor(35));
        assertEquals(Color.GREEN, DateComparatorUtil.getExpirationColor(30));
        assertEquals(Color.YELLOW, DateComparatorUtil.getExpirationColor(25));
        assertEquals(Color.YELLOW, DateComparatorUtil.getExpirationColor(20));
        assertEquals(Color.YELLOW, DateComparatorUtil.getExpirationColor(15));
        assertEquals(Color.YELLOW, DateComparatorUtil.getExpirationColor(10));
        assertEquals(Color.YELLOW, DateComparatorUtil.getExpirationColor(7));
        assertEquals(Color.RED, DateComparatorUtil.getExpirationColor(6));
        assertEquals(Color.RED, DateComparatorUtil.getExpirationColor(5));
        assertEquals(Color.RED, DateComparatorUtil.getExpirationColor(1));
        assertEquals(Color.RED, DateComparatorUtil.getExpirationColor(0));
        assertEquals(Color.RED, DateComparatorUtil.getExpirationColor(-1));
        assertEquals(Color.RED, DateComparatorUtil.getExpirationColor(-10));
    }

    @Test
    public void testGetBorrowColor() {
        assertEquals(Color.GREEN, DateComparatorUtil.getBorrowColor(10));
        assertEquals(Color.GREEN, DateComparatorUtil.getBorrowColor(8));
        assertEquals(Color.GREEN, DateComparatorUtil.getBorrowColor(7));
        assertEquals(Color.YELLOW, DateComparatorUtil.getBorrowColor(6));
        assertEquals(Color.YELLOW, DateComparatorUtil.getBorrowColor(5));
        assertEquals(Color.YELLOW, DateComparatorUtil.getBorrowColor(4));
        assertEquals(Color.YELLOW, DateComparatorUtil.getBorrowColor(3));
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
