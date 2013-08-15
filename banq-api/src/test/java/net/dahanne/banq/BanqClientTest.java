package net.dahanne.banq;

import net.dahanne.banq.model.BorrowedItem;
import net.dahanne.banq.model.Details;

import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.Set;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by anthony on 13-08-15.
 */
public class BanqClientTest {

    private static final String USERNAME = System.getProperty("username");
    private static final String PASSWORD = System.getProperty("password");

    @Test
    public void toDateTest() throws ParseException {
        Date date = BanqClient.toDate("2013-08-15-01:00");
        Date expectedDate = getDate(2013, 7, 15, 01, 0);
        assertTrue(expectedDate.compareTo(date) == 0);
    }


    @Test
    public void parseDetailsTest() throws IOException, ParseException {
        InputStream resource = BanqClientTest.class.getClassLoader().getResourceAsStream("sampleDetails.html");
        String sampleDetailsPage =  HttpBuilder.toString(resource);

        BanqClient bc = new BanqClient();
        Details details = bc.parseDetails(sampleDetailsPage);
        assertEquals("Dahanne Anthony",details.getName());
        assertEquals(getDate(2014,4,5,0,0),details.getExpirationDate());
        assertEquals("9.50$",details.getCurrentDebt());

        BorrowedItem expectedMaisy =  new BorrowedItem("Maisy's colors / Lucy Cousins.","COU", getDate(2013,7,13,23,27),getDate(2013,8,3,23,59));
        assertEquals(expectedMaisy,details.getBorrowedItems().get(2));
    }

    @Test
    public void sampleRunTest()  throws Exception{
        BanqClient bc = new BanqClient();
        Set<String> cookies = bc.authenticate(USERNAME, PASSWORD);
        String detailsPage = bc.getDetailsPage(cookies);
        Details details =  bc.parseDetails(detailsPage);

        System.out.println("Borrower name  : " + details.getName());
        System.out.println("Current debt  : " + details.getCurrentDebt());
        System.out.println("Subscription expiration date : " + details.getExpirationDate());
    }
    private Date getDate(int year, int month, int day, int hourOfDay, int minute) {
        Calendar calendar =  Calendar.getInstance();
        calendar.set(year, month, day, hourOfDay, minute,0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.setTimeZone(TimeZone.getTimeZone("America/Montreal"));
        return calendar.getTime();
    }
}
