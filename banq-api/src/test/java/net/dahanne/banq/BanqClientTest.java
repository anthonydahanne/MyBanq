package net.dahanne.banq;

import net.dahanne.banq.exceptions.InvalidCredentialsException;
import net.dahanne.banq.model.BorrowedItem;
import net.dahanne.banq.model.ContactDetails;
import net.dahanne.banq.model.Details;
import net.dahanne.banq.model.LateFee;
import net.dahanne.banq.model.LateFees;
import net.dahanne.banq.model.Reservation;
import net.dahanne.banq.model.ReturnedLoan;

import org.hamcrest.core.IsNull;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
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
        if (USERNAME == null || PASSWORD == null) {
            System.err.println("You did not specify USERNAME or PASSWORD vm arguments, integration tests can't be run");
        }
        Assume.assumeThat(USERNAME, IsNull.notNullValue());
        Assume.assumeThat(PASSWORD, IsNull.notNullValue());
    }

    @Test
    public void toDateTest() throws ParseException {
        Date date = BanqClient.toDate("15/08/2013");
        Date expectedDate = getDate(2013, 7, 15);
        assertTrue(expectedDate.compareTo(date) == 0);
    }


    @Test
    public void parseDetailsTest() throws IOException, ParseException {
        InputStream resource = BanqClientTest.class.getClassLoader().getResourceAsStream("sampleDetails-mobile.html");
        String sampleDetailsPage = HttpBuilder.toString(resource, "UTF-8");

        BanqClient bc = new BanqClient();
        Details details = bc.parseDetails(sampleDetailsPage);
        assertEquals("Dahanne Anthony", details.getName());
//        assertEquals(getDate(2014,4,5,0,0),details.getExpirationDate());
        assertEquals("9,80 $", details.getCurrentDebt());
//        assertEquals("02002005631076",details.getUserID());

        BorrowedItem borrowedItem = new BorrowedItem("Petit Ours brun veut faire comme papa", "Aubinais, Marie", "Grande Bibliothèque", getDate(2013, 10, 30), getDate(2013, 11, 21), "32002511383935", true, "0,10 $");
        assertEquals(borrowedItem, details.getBorrowedItems().get(1));


        borrowedItem = new BorrowedItem("This tree, 1, 2, 3", "Formento, Alison", "Grande Bibliothèque", getDate(2013, 10, 30), getDate(2013, 11, 21), "32002515727087", true, null);
        assertEquals(borrowedItem, details.getBorrowedItems().get(2));

        borrowedItem = new BorrowedItem("Green eggs and ham", "Seuss, Dr.", "Grande Bibliothèque", getDate(2013, 11, 1), getDate(2013, 11, 22), "32002501575128", false, null);
        assertEquals(borrowedItem, details.getBorrowedItems().get(3));


    }


    @Test
    public void parseReservationsTest() throws IOException, ParseException {
        InputStream resource = BanqClientTest.class.getClassLoader().getResourceAsStream("reservations-mobile.html");
        String reservationsPage = HttpBuilder.toString(resource, "UTF-8");

        BanqClient bc = new BanqClient();
        List<Reservation> reservations = bc.parseReservations(reservationsPage);
        Reservation expectedMaisy = new Reservation(59280, "Les 4 soldats = The 4 soldiers [enregistrement vidéo]", getDate(2013, 11, 12), "Le document n'est pas encore disponible.", 5);
        assertEquals(expectedMaisy, reservations.get(1));
    }

    @Test
    public void parseLoansTest() throws IOException, ParseException {
        InputStream resource = BanqClientTest.class.getClassLoader().getResourceAsStream("loanshistory.html");
        String loansHistoryPage = HttpBuilder.toString(resource, "UTF-8");

        BanqClient bc = new BanqClient();
        List<ReturnedLoan> reservations = bc.parseLoansHistory(loansHistoryPage);
        ReturnedLoan expectedMaisy = new ReturnedLoan("Petit Ours brun veut faire comme papa / Aubinais, Marie", getDate(2013, 10, 10), getDate(2013, 10, 30));
        assertEquals(expectedMaisy, reservations.get(2));
    }


    @Test
    public void parseMyContactDetailsTest() throws IOException, ParseException {
        InputStream resource = BanqClientTest.class.getClassLoader().getResourceAsStream("mycontactdetails.html");
        String contactPage = HttpBuilder.toString(resource, "UTF-8");

        BanqClient bc = new BanqClient();
        ContactDetails contactDetails = bc.parseMyContactDetails(contactPage);
        ContactDetails expectedContactDetails = new ContactDetails("Dahanne Anthony", getDate(2014, 04, 5), "0200200999999", "555 rue Saint-André Montréal (QC) Canada H2L 4G4", "(514)316-5555");
        assertEquals(expectedContactDetails, contactDetails);
    }

    @Test
    public void parseMyAccountHistoryTest() throws IOException, ParseException {
        InputStream resource = BanqClientTest.class.getClassLoader().getResourceAsStream("accounthistory.html");
        String contactPage = HttpBuilder.toString(resource, "UTF-8");

        BanqClient bc = new BanqClient();
        LateFees lateFees = bc.parseAccountHistory(contactPage);
        assertEquals("9,80 $", lateFees.getCurrentDebt());
        assertEquals(new LateFee("Développer des applications An", "01/02/2013 - 09:26:59", "1,50 $", "Amende", "32002515791042" ), lateFees.getLateFees().get(4));

    }


    @Test
    public void sampleRunTest() throws Exception {
        BanqClient bc = new BanqClient();
        Set<String> cookies = bc.authenticate(USERNAME, PASSWORD);
        String detailsPage = bc.getDetailsPage(cookies);
        String reservationsPage = bc.getReservationsPage(cookies);
        String contactPage = bc.getContactDetailsPage(cookies);
        String loansHistory = bc.getLoansHistory(cookies);
        String accountHistoryPage = bc.getAccountHistory(cookies);

        Details details = bc.parseDetails(detailsPage);
        List<Reservation> reservations = bc.parseReservations(reservationsPage);
        List<ReturnedLoan> loans = bc.parseLoansHistory(loansHistory);

        System.out.println(details);

        for (Reservation reservation : reservations) {
            System.out.println(reservation);
        }

        System.out.println(bc.parseMyContactDetails(contactPage));

        for (ReturnedLoan loan : loans) {
            System.out.println(loan);
        }

        System.out.println(bc.parseAccountHistory(accountHistoryPage));
    }

    @Test(expected = InvalidCredentialsException.class)
    public void authenticateTest__failure() throws Exception {
        BanqClient bc = new BanqClient();
        bc.authenticate("99999999", "88888888");

    }

    private Date getDate(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day, 0, 0, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.setTimeZone(TimeZone.getTimeZone("America/Montreal"));
        return calendar.getTime();
    }
}
