package net.dahanne.banq;

import net.dahanne.banq.exceptions.FailedToRenewException;
import net.dahanne.banq.exceptions.InvalidCredentialsException;
import net.dahanne.banq.exceptions.InvalidSessionException;
import net.dahanne.banq.model.BorrowedItem;
import net.dahanne.banq.model.ContactDetails;
import net.dahanne.banq.model.Details;
import net.dahanne.banq.model.LateFee;
import net.dahanne.banq.model.LateFees;
import net.dahanne.banq.model.Reservation;
import net.dahanne.banq.model.ReturnedLoan;

import org.hamcrest.core.IsNull;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeThat;

/**
 * @author Anthony Dahanne
 */
public class BanqClientTest {

    private static final String USERNAME = System.getProperty("username");
    private static final String PASSWORD = System.getProperty("password");

    private CookieManager cookieManager = new CookieManager(null, CookiePolicy.ACCEPT_ALL);


    @BeforeClass
    public static void checkVMArgumentsAreProvided() throws IOException {
        if (USERNAME == null || PASSWORD == null) {
            System.err.println("You did not specify USERNAME or PASSWORD vm arguments, integration tests can't be run");
        }
    }

    @Test
    public void toDateTest() throws ParseException {
        Date date = BanqClient.toDate("15/08/2013");
        Date expectedDate = getDate(2013, 7, 15);
        assertTrue(expectedDate.compareTo(date) == 0);
    }

    @Test
    public void parseEmptyDetailsTest() throws IOException, ParseException {
        InputStream resource = BanqClientTest.class.getClassLoader().getResourceAsStream("sampleDetails-mobile-empty-list.html");
        String sampleDetailsPage = HttpBuilder.toString(resource, "UTF-8");

        BanqClient bc = new BanqClient(cookieManager);
        Details details = bc.parseDetails(sampleDetailsPage);
        assertEquals("Dahanne Anthony", details.getName());
        assertEquals("0,00 $", details.getCurrentDebt());
        assertEquals(0, details.getBorrowedItems().size());

    }

    @Test
    public void parseDetailsTest() throws IOException, ParseException {
        InputStream resource = BanqClientTest.class.getClassLoader().getResourceAsStream("sampleDetails-mobile.html");
        String sampleDetailsPage = HttpBuilder.toString(resource, "UTF-8");

        BanqClient bc = new BanqClient(cookieManager);
        Details details = bc.parseDetails(sampleDetailsPage);
        assertEquals("Dahanne Anthony", details.getName());
        assertEquals("9,80 $", details.getCurrentDebt());
        assertEquals("ATTENTION : Vous ne pouvez plus vous prévaloir des services aux abonnés de Bibliothèque et Archives nationales du Québec parce que : " +
                "- les frais apparaissant à votre dossier ont atteint le maximum permis. Les frais peuvent être payés en ligne ou au comptoir de prêt. " +
                "Vous avez 2 document(s) en retard.", details.getImportantMessage());
        assertEquals("Obj_1700041386823739", details.getObjId());

        BorrowedItem borrowedItem = new BorrowedItem("Petit Ours brun veut faire comme papa", "Aubinais, Marie", "Grande Bibliothèque", getDate(2013, 10, 30), getDate(2013, 11, 21), "32002511383935", true, "0,10 $", 2, "Obj_1700041386823739");
        assertEquals(borrowedItem, details.getBorrowedItems().get(1));


        borrowedItem = new BorrowedItem("This tree, 1, 2, 3", "Formento, Alison", "Grande Bibliothèque", getDate(2013, 10, 30), getDate(2013, 11, 21), "32002515727087", true, null, 3, "Obj_1700041386823739");
        assertEquals(borrowedItem, details.getBorrowedItems().get(2));

        borrowedItem = new BorrowedItem("Green eggs and ham", "Seuss, Dr.", "Grande Bibliothèque", getDate(2013, 11, 1), getDate(2013, 11, 22), "32002501575128", false, null, 4, "Obj_1700041386823739");
        assertEquals(borrowedItem, details.getBorrowedItems().get(3));

    }


    @Test
    public void parseReservationsTest() throws IOException, ParseException {
        InputStream resource = BanqClientTest.class.getClassLoader().getResourceAsStream("reservations-mobile.html");
        String reservationsPage = HttpBuilder.toString(resource, "UTF-8");

        BanqClient bc = new BanqClient(cookieManager);
        List<Reservation> reservations = bc.parseReservations(reservationsPage);
        Reservation expectedMaisy = new Reservation(59280, "Les 4 soldats = The 4 soldiers [enregistrement vidéo]", getDate(2013, 11, 12), "Le document n'est pas encore disponible.", 5);
        assertEquals(expectedMaisy, reservations.get(1));
    }

    @Test
    public void parseLoansTest() throws IOException, ParseException {
        InputStream resource = BanqClientTest.class.getClassLoader().getResourceAsStream("loanshistory.html");
        String loansHistoryPage = HttpBuilder.toString(resource, "UTF-8");

        BanqClient bc = new BanqClient(cookieManager);
        List<ReturnedLoan> reservations = bc.parseLoansHistory(loansHistoryPage);
        ReturnedLoan expectedMaisy = new ReturnedLoan("Petit Ours brun veut faire comme papa / Aubinais, Marie", getDate(2013, 10, 10), getDate(2013, 10, 30));
        assertEquals(expectedMaisy, reservations.get(2));
    }


    @Test
    public void parseMyContactDetailsTest() throws IOException, ParseException {
        InputStream resource = BanqClientTest.class.getClassLoader().getResourceAsStream("mycontactdetails.html");
        String contactPage = HttpBuilder.toString(resource, "UTF-8");

        BanqClient bc = new BanqClient(cookieManager);
        ContactDetails contactDetails = bc.parseMyContactDetails(contactPage);
        ContactDetails expectedContactDetails = new ContactDetails("Dahanne Anthony", getDate(2014, 4, 5), "0200200999999", "555 rue Saint-André Montréal (QC) Canada H2L 4G4", "(514)316-5555");
        assertEquals(expectedContactDetails, contactDetails);
    }

    @Test
    public void parseMyAccountHistoryTest() throws IOException, ParseException {
        InputStream resource = BanqClientTest.class.getClassLoader().getResourceAsStream("accounthistory.html");
        String contactPage = HttpBuilder.toString(resource, "UTF-8");

        BanqClient bc = new BanqClient(cookieManager);
        LateFees lateFees = bc.parseAccountHistory(contactPage);
        assertEquals("9,80 $", lateFees.getCurrentDebt());
        assertEquals(new LateFee("Développer des applications An", "01/02/2013 - 09:26:59", "1,50 $", "Amende", "32002515791042" ), lateFees.getLateFees().get(4));

    }

    @Test
    public void sampleRunTest() throws Exception {
        assumeThat(USERNAME, IsNull.notNullValue());
        assumeThat(PASSWORD, IsNull.notNullValue());
        BanqClient bc = new BanqClient(cookieManager);
        bc.authenticate(USERNAME, PASSWORD);
        String detailsPage = bc.getDetailsPage();
        Details details = bc.parseDetails(detailsPage);
        String reservationsPage = bc.getReservationsPage();
        String contactPage = bc.getContactDetailsPage();
        String loansHistory = bc.getLoansHistory();
        String accountHistoryPage = bc.getAccountHistory();

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


    @Ignore
    @Test(expected = FailedToRenewException.class)
    public void renew() throws Exception {
        BanqClient bc = new BanqClient(cookieManager);
//        bc.authenticate(USERNAME, PASSWORD);
        String detailsPage;
        try {
            detailsPage = bc.getDetailsPage();
        } catch (InvalidSessionException ise) {
            bc.authenticate(USERNAME, PASSWORD);
            detailsPage = bc.getDetailsPage();
        }
        Details details = bc.parseDetails(detailsPage);
        bc.renew(details.getObjId(), 1);


    }

    @Test(expected = InvalidCredentialsException.class)
    public void authenticateTest__failure() throws Exception {
        BanqClient bc = new BanqClient(cookieManager);
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
