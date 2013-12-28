package net.dahanne.banq.model;

import org.junit.Test;

import java.util.Calendar;

import static org.junit.Assert.assertEquals;

/**
 * Created by guilhem.demiollis on 13-08-20.
 */
public class BorrowedItemTest {

    @Test
    public void testGetRemainingDays() {
        Calendar instance = Calendar.getInstance();
        instance.add(Calendar.DAY_OF_YEAR, 3);
        assertEquals(3, new BorrowedItem(null, null, null, null, instance.getTime(), null, false, null).getRemainingDays());
    }
}
