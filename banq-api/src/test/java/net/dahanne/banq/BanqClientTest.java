package net.dahanne.banq;

import net.dahanne.banq.exceptions.FailedToRenewException;
import net.dahanne.banq.exceptions.InvalidCredentialsException;
import net.dahanne.banq.model.BorrowedItem;
import net.dahanne.banq.model.Details;

import org.hamcrest.core.IsNull;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
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


    @BeforeClass
    public static void checkVMArgumentsAreProvided() throws IOException {
        if(USERNAME==null || PASSWORD ==null) {
            System.err.println("You did not specify USERNAME or PASSWORD vm arguments, integration tests can't be run");
        }
        Assume.assumeThat(USERNAME,IsNull.notNullValue());
        Assume.assumeThat(PASSWORD,IsNull.notNullValue());
    }

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
        assertEquals("02002005631076",details.getUserID());

        BorrowedItem expectedMaisy =  new BorrowedItem("Maisy's colors / Lucy Cousins.","COU", getDate(2013,7,13,23,27),getDate(2013,8,3,23,59), "32002500975246","02002005631076" );
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


        // we try to renew a doc that can't be renewed
        String docNo = "3200251908339";
        String userId = "02002005631076";
        try {
        bc.renew(cookies, userId, docNo);
        }
        catch (FailedToRenewException itre) {
            System.out.println(itre.getMessage());
        }

    }

    @Test(expected = InvalidCredentialsException.class)
    public void authenticateTest__failure()  throws Exception{
        BanqClient bc = new BanqClient();
        bc.authenticate("99999999", "88888888");

    }

    private Date getDate(int year, int month, int day, int hourOfDay, int minute) {
        Calendar calendar =  Calendar.getInstance();
        calendar.set(year, month, day, hourOfDay, minute,0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.setTimeZone(TimeZone.getTimeZone("America/Montreal"));
        return calendar.getTime();
    }
}
