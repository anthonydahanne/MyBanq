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

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Anthony Dahanne
 */
public class BanqClient {

    private static Logger LOG = LoggerFactory.getLogger(BanqClient.class);
    private final CookieManager cookieManager;

    public BanqClient(CookieManager cookieManager) {
        this.cookieManager = cookieManager;
    }

    synchronized public void authenticate(String username, String password) throws IOException, InterruptedException, InvalidCredentialsException {
        CookieHandler.setDefault(this.cookieManager);

        if (username.length() != 8) {
            throw new InvalidCredentialsException();
        }

        HttpURLConnection connect = null;
        String location = null;
        InputStream inputStream;
        String responseMessage;

        try {
            connect = new HttpBuilder("https://iris.banq.qc.ca/login/login.aspx?Lang=FRE&retObj=APS_ZONES%3Ffn%3DMyZoneHomePage%26Style%3DMobile%26SubStyle%3D%26Lang%3DFRE%26ResponseEncoding%3Dutf-8&retPage=").connect();

            location = getLocationHeader(connect);
            inputStream = connect.getInputStream();
            responseMessage = HttpBuilder.toString(inputStream, HttpBuilder.ISO_8859_1);
        } finally {
            if (connect != null) {
                connect.disconnect();
            }
        }
        LOG.debug("1st step completed (getting iris login page) cookies : " + cookieManager.getCookieStore().getCookies());


        //https://www.banq.qc.ca:443/idp/Authn/UserPassword for JSESSION_ID
        try {
            connect = new HttpBuilder(location).connect();
            location = getLocationHeader(connect);
            inputStream = connect.getInputStream();
            responseMessage = HttpBuilder.toString(inputStream, HttpBuilder.ISO_8859_1);
        } finally {
            if (connect != null) {
                connect.disconnect();
            }
        }
        LOG.debug("2nd step completed (getting auth. page), cookies : " + cookieManager.getCookieStore().getCookies());

        String relayState = null;
        String SAMLResponse = null;
        HashMap<String, String> data = new HashMap<String, String>();

        try {
            data.put("j_username", username);
            data.put("j_password", password);
            connect = new HttpBuilder(HttpBuilder.HttpMethod.POST, "https://www.banq.qc.ca/idp/Authn/UserPassword").data(data).connect();
            location = getLocationHeader(connect);
            inputStream = connect.getInputStream();
            responseMessage = HttpBuilder.toString(inputStream, HttpBuilder.ISO_8859_1);

            Document parse = Jsoup.parse(responseMessage);

            Elements echec = parse.getElementsByAttributeValue("class", "echec");
            if (!echec.isEmpty()) {
                throw new InvalidCredentialsException();
            }

            Elements p = parse.getElementsByTag("input");
            for (Element element : p) {
                String type = element.attr("type");
                if (type != null && type.equals("hidden")) {
                    String name = element.attr("name");
                    if (name != null && name.equals("RelayState")) {
                        relayState = element.attr("value");
                    } else if (name != null && name.equals("SAMLResponse")) {
                        SAMLResponse = element.attr("value");
                    }
                }
            }
            if (relayState == null || SAMLResponse == null) {
                throw new InvalidCredentialsException();
            }
        } finally {
            if (connect != null) {
                connect.disconnect();
            }
        }
        LOG.debug("3 rd step completed (password authentication) cookies : " + cookieManager.getCookieStore().getCookies());

        try {
            data = new HashMap<String, String>();
            data.put("RelayState", relayState);
            data.put("SAMLResponse", SAMLResponse);

            connect = new HttpBuilder(HttpBuilder.HttpMethod.POST, "https://iris.banq.qc.ca/Shibboleth.sso/SAML2/POST").data(data).connect();

            location = getLocationHeader(connect);

            inputStream = connect.getInputStream();
            responseMessage = HttpBuilder.toString(inputStream, HttpBuilder.ISO_8859_1);
        } finally {
            if (connect != null) {
                connect.disconnect();
            }
        }
        LOG.debug("4th step completed (sso authentication) cookies : " + cookieManager.getCookieStore().getCookies());
        try {
            connect = new HttpBuilder(location).connect();
            location = getLocationHeader(connect);

            if (location != null) {
                authenticate(username, password);
            }
        } finally {
            if (connect != null) {
                connect.disconnect();
            }
        }
        LOG.debug("5th step completed - all done  cookies : " + cookieManager.getCookieStore().getCookies());
    }

    private static String getLocationHeader(HttpURLConnection connect) {
        String headerName;
        String location = null;
        for (int i = 1; (headerName = connect.getHeaderFieldKey(i)) != null; i++) {
            if (headerName.equalsIgnoreCase("Location")) {
                location = connect.getHeaderField(i);
            }
        }
        return location;
    }


    synchronized private String getPage(String url) throws IOException, InvalidSessionException {
        CookieHandler.setDefault(this.cookieManager);
        HttpURLConnection connect = null;
        try {
            connect = new HttpBuilder(url).connect();
            if (connect.getResponseCode() == 302) {
                // the session is not usable, we should re authenticate from there.
                throw new InvalidSessionException();
            }

            InputStream inputStream = connect.getInputStream();
            String page = HttpBuilder.toString(inputStream, "UTF-8");
            if (page.contains("window.location = \"https://iris.banq.qc.ca/login/login.aspx")) {
                throw new InvalidSessionException();
            }
            return page;
        } finally {
            if (connect != null) {
                connect.disconnect();
            }
        }
    }

    String getDetailsPage() throws IOException, ParseException, InvalidSessionException {
        return getPage("https://iris.banq.qc.ca/alswww2.dll/APS_ZONES?fn=MyZone&Style=Mobile&Lang=FRE&ResponseEncoding=utf-8");
    }

    String getContactDetailsPage() throws IOException, ParseException, InvalidSessionException {
        return getPage("https://iris.banq.qc.ca/alswww2.dll/APS_ZONES?fn=MyContactDetails&Style=Mobile&Lang=FRE&ResponseEncoding=utf-8");
    }

    String getReservationsPage() throws IOException, ParseException, InvalidSessionException {
        return getPage("https://iris.banq.qc.ca/alswww2.dll/APS_ZONES?fn=MyReservations&Style=Mobile&SubStyle=&Lang=FRE&ResponseEncoding=utf-8");
    }

    String getLoansHistory() throws IOException, ParseException, InvalidSessionException {
        return getPage("https://iris.banq.qc.ca/alswww2.dll/APS_ZONES?fn=MyLoansHistory&Style=Mobile&SubStyle=&Lang=FRE&ResponseEncoding=utf-8");
    }

    String getAccountHistory() throws IOException, ParseException, InvalidSessionException {
        StringBuilder sb = new StringBuilder();
        String originalPage = getPage("https://iris.banq.qc.ca/alswww2.dll/APS_ZONES?fn=MyAccountHistory&Style=Mobile&SubStyle=&Lang=FRE&ResponseEncoding=utf-8");
//        System.out.println("originalPage count : " + originalPage.length());
        sb.append(originalPage);
//        while (originalPage.contains("Method=PageDown")) {
//            //<META NAME="ZonesObjName"  CONTENT="Obj_1630781387085165">
//            Document parse = Jsoup.parse(originalPage);
//            Element zonesObjNameTag = parse.getElementsByAttributeValue("NAME", "ZonesObjName").first();
//            String objName = zonesObjNameTag.attr("CONTENT");
//            String url = "https://iris.banq.qc.ca/alswww2.dll/" + objName + "?Style=Mobile&SubStyle=&Lang=FRE&ResponseEncoding=utf-8&Method=PageDown&PageSize=10";
////            System.out.println(url);
//            originalPage = getPage(cookies, url);
////            System.out.println("originalPage count : " + originalPage.length());
//            sb.append(originalPage);
//        }
//        System.out.println("sb count : " + sb.length());
        return sb.toString();
    }


    LateFees parseAccountHistory(String responseMessage) throws ParseException {
        List<LateFee> lateFees = new ArrayList<LateFee>();
        Document parse = Jsoup.parse(responseMessage);
        String currentDebt = parse.getElementsByClass("TotalFinesCell").text().trim();
        Elements browseListElements = parse.getElementsByClass("browseList");
        for (Element browseListElement : browseListElements) {
            Elements reservationBrowseTable = browseListElement.getElementsByClass("MessageBrowseTable");
            for (Element element : reservationBrowseTable) {
                Elements reservationBrowseFieldDataCell = element.getElementsByClass("MessageBrowseFieldDataCell");
                String title = reservationBrowseFieldDataCell.get(0).child(0).text().trim();
                String dateAsString = reservationBrowseFieldDataCell.get(1).text().trim();
                String fee = reservationBrowseFieldDataCell.get(2).text().trim();
                String feeType = reservationBrowseFieldDataCell.get(3).text().trim();
                String feeId = reservationBrowseFieldDataCell.get(4).text().trim();
                lateFees.add(new LateFee(title, dateAsString, fee, feeType, feeId));
            }
        }
        return new LateFees(currentDebt, lateFees);
    }


    List<Reservation> parseReservations(String responseMessage) throws ParseException {
        List<Reservation> reservations = new ArrayList<Reservation>();
        Document parse = Jsoup.parse(responseMessage);

        Element browseList = parse.getElementById("BrowseList");
        if (browseList != null) {
            Elements reservationBrowseTable = browseList.getElementsByClass("ReservationBrowseTable");
            for (Element element : reservationBrowseTable) {
                Elements reservationBrowseFieldDataCell = element.getElementsByClass("ReservationBrowseFieldDataCell");
                String title = reservationBrowseFieldDataCell.get(0).child(0).text().trim();
                Date bookedSince = toDate(reservationBrowseFieldDataCell.get(1).text().trim());
                String status = reservationBrowseFieldDataCell.get(2).text().trim();
                int rank = Integer.valueOf(reservationBrowseFieldDataCell.get(3).text().trim());
                String linkId = reservationBrowseFieldDataCell.get(4).getElementsByTag("a").first().id();
                int id = Integer.valueOf(linkId.substring(linkId.indexOf("_") + 1));
                reservations.add(new Reservation(id, title, bookedSince, status, rank));
            }
        }
        return reservations;
    }


    List<ReturnedLoan> parseLoansHistory(String responseMessage) throws ParseException {
        List<ReturnedLoan> returnedLoans = new ArrayList<ReturnedLoan>();
        Document parse = Jsoup.parse(responseMessage);

        Element browseList = parse.getElementById("BrowseList");
        if (browseList != null) {
            Elements loansBrowseItemDetailsCell = browseList.getElementsByAttributeValueStarting("class", "LoansBrowseItemDetailsCell");
            for (Element element : loansBrowseItemDetailsCell) {
                Elements reservationBrowseFieldDataCell = element.getElementsByClass("LoanBrowseFieldDataCell");
                String title = reservationBrowseFieldDataCell.get(0).text().trim();
                Date bookedSince = toDate(reservationBrowseFieldDataCell.get(1).text().trim());
                Date returnDate = toDate(reservationBrowseFieldDataCell.get(2).text().trim());
                returnedLoans.add(new ReturnedLoan(title, bookedSince, returnDate));
            }
        }
        return returnedLoans;
    }


    ContactDetails parseMyContactDetails(String responseMessage) throws ParseException {
        ContactDetails contactDetails = null;
        Document parse = Jsoup.parse(responseMessage);

        Element accountDetailsTable = parse.getElementsByClass("AccountDetailsTable").first();
        if (accountDetailsTable != null) {
            Elements accountDetailFieldValueCell = accountDetailsTable.getElementsByClass("AccountDetailFieldValueCell");
            Elements accountDetailFieldValueCellStripe = accountDetailsTable.getElementsByClass("AccountDetailFieldValueCellStripe");


            String name = accountDetailFieldValueCellStripe.get(0).text().trim();
            Date expirationDate = toDate(accountDetailFieldValueCell.get(0).text().trim());
            String accountNumber = accountDetailFieldValueCellStripe.get(1).text().trim();
            String address = accountDetailFieldValueCell.get(1).text();
            String phoneNumber = accountDetailFieldValueCell.get(2).text();
            contactDetails = new ContactDetails(name, expirationDate, accountNumber, address, phoneNumber);
        }
        return contactDetails;
    }


    Details parseDetails(String responseMessage) throws ParseException {
        LOG.debug("Starting parsing details");
        long startTime = System.currentTimeMillis();
        Document parse = Jsoup.parse(responseMessage.substring(responseMessage.indexOf("<body")));
        Element contenu = parse.getElementById("pageContent");
        Details details = null;
        if (contenu != null) {

            Element userIdElement = contenu.getElementsByClass("MyZonetitleText").first();
            String name = userIdElement.text().trim();


            String importantMessage = "";
            if (contenu.getElementsByAttributeValueStarting("class", "AccountTrap").last() != null) {
                importantMessage = contenu.getElementsByAttributeValueStarting("class", "AccountTrap").last().text().trim();
            }
            Element summaryDetailsTable = contenu.getElementsByClass("SummaryDetailsTable").first();
            String detailsLink = summaryDetailsTable.getElementsByClass("AccountSummaryCounterLink").first().attr("href");
            String detailsObjId = detailsLink.substring(0, detailsLink.indexOf("?"));


            Elements accountSummaryCounterValueCells = summaryDetailsTable.getElementsByAttributeValueStarting("class", "AccountSummaryCounterValueCell");
            int reservationsNumber = Integer.valueOf(accountSummaryCounterValueCells.get(1).text().trim());
            int messagesNumber = Integer.valueOf(accountSummaryCounterValueCells.get(2).text().trim());
            String debtText = accountSummaryCounterValueCells.get(3).text();
            String currentDebt = debtText.substring(0, debtText.indexOf("$") + 1).trim();
            String lateFeesText = accountSummaryCounterValueCells.get(5).text();
            String lateFeesToCome = lateFeesText.substring(0, lateFeesText.indexOf("$") + 1).trim();

            Element inlineCurrentLoans = contenu.getElementsByClass("InlineCurrentLoans").first();
            Elements loanBrowseTables = null;
            if (inlineCurrentLoans != null) {
                loanBrowseTables = inlineCurrentLoans.getElementsByClass("LoanBrowseTable");
            }
            if (loanBrowseTables == null) {
                details = new Details(name, currentDebt, lateFeesToCome, messagesNumber, reservationsNumber, importantMessage, detailsObjId);
            } else {
                List<BorrowedItem> borrowedItemsList = new ArrayList<BorrowedItem>();
                int itemPosition = 1;
                for (Element loanBrowseTable : loanBrowseTables) {
                    String authorInfo = null;
                    Elements loanBrowseFieldDataCells = loanBrowseTable.getElementsByClass("LoanBrowseFieldDataCell");
                    String title = loanBrowseFieldDataCells.get(0).child(0).text();
                    String titleAndAuthorHtml = loanBrowseFieldDataCells.get(0).html();
                    if (titleAndAuthorHtml.contains("/ ")) {
                        authorInfo = titleAndAuthorHtml.substring(titleAndAuthorHtml.indexOf("/ ") + 2).trim();
                    }
                    String documentNumber = loanBrowseFieldDataCells.get(1).text().trim();
                    String borrowedItemLocation = loanBrowseFieldDataCells.get(2).text().trim();
                    Date borrowedDate = toDate(loanBrowseFieldDataCells.get(3).text().trim());
                    Date returnDate = toDate(loanBrowseFieldDataCells.get(4).getElementsByClass("LoanDate").first().child(0).text().toString().trim());

                    boolean isRenewable = loanBrowseTable.getElementById("buttonRenewLoan") != null;

                    String lateFees = null;
                    boolean lateFeesDued = loanBrowseTable.getElementsByClass("LateLoan").first().text().trim().length() != 0;
                    if (lateFeesDued) {
                        lateFees = loanBrowseFieldDataCells.get(5).text().trim();
                    }

                    borrowedItemsList.add(new BorrowedItem(title, authorInfo, borrowedItemLocation, borrowedDate, returnDate, documentNumber, isRenewable, lateFees, itemPosition, detailsObjId));
                    itemPosition++;
                }

                details = new Details(name, currentDebt, lateFeesToCome, messagesNumber, reservationsNumber, importantMessage, detailsObjId, borrowedItemsList);
            }
        }
        LOG.debug("Finished parsing details, it took {} ms", System.currentTimeMillis() - startTime);
        return details;
    }

    static Date toDate(String substring) throws ParseException {
        substring = substring + " EDT";
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy 'EDT'");
        return formatter.parse(substring);
    }

    public Details getDetails() throws ParseException, InvalidSessionException, IOException {
        String detailsPage = this.getDetailsPage();
        LOG.debug("sent cookies : " + ((CookieManager) CookieHandler.getDefault()).getCookieStore().getCookies());
        Details details = this.parseDetails(detailsPage);
        LOG.debug("objectListId is : " + details.getObjId());

        return details;
    }

    synchronized public void renew(String objId, int itemPosition) throws FailedToRenewException, IOException, InvalidSessionException {
        // temporary cookie manager is an empty cookiemanager, so that we can craft the cookies ourselves
        // by default java.net.CookieStore specifies domain at the end of the cookies, but Banq does not like it
        CookieManager temporary = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
        CookieHandler.setDefault(temporary);

        LOG.debug("renewing item " + itemPosition + " from object list " + objId);
        String responseMessage;
        HttpURLConnection connect = null;
        try {
            String url = "https://iris.banq.qc.ca/alswww2.dll/" + objId + "?Style=Mobile&SubStyle=&Lang=FRE&ResponseEncoding=utf-8&Method=Renew&Accepted=1&Item=P" + itemPosition;
            Set<String> cookies = new HashSet<String>();

            List<HttpCookie> originalCookies = cookieManager.getCookieStore().getCookies();
            String manuallyCraftedCookie = "";
            for (HttpCookie originalCookie : originalCookies) {
                if (originalCookie.getName().equals("SID") || originalCookie.getName().startsWith("_shibsession")) {
//                    cookies.add(originalCookie.getName() + "=" +originalCookie.getValue());
                    if (manuallyCraftedCookie.length() == 0) {
                        manuallyCraftedCookie = originalCookie.getName() + "=" + originalCookie.getValue();
                    } else {
                        manuallyCraftedCookie = manuallyCraftedCookie + "; " + originalCookie.getName() + "=" + originalCookie.getValue();
                    }
                }
            }
            cookies.add(manuallyCraftedCookie);
//            cookies.add("SID=S65521389070137339398776811499; _shibsession_64656661756c7468747470733a2f2f697269732e62616e712e71632e63612f73686962626f6c6574682d7370=_0d85c1bccc5d5a82796d426ac065bf8d");
            LOG.debug("sent cookies : " + manuallyCraftedCookie);
            connect = new HttpBuilder(url).cookie(cookies).connect();
            if (connect.getResponseCode() == 302) {
                // the session is not usable, we should re authenticate from there.
                throw new InvalidSessionException();
            }
            InputStream inputStream = connect.getInputStream();
            responseMessage = HttpBuilder.toString(inputStream, HttpBuilder.UTF_8);
            LOG.debug("response : " + responseMessage);
            LOG.debug("request : " + url);
            if (!responseMessage.contains("\"success\" : \"true\"")) {
                throw new FailedToRenewException("Renewal not successful.");
            }
            if (!responseMessage.contains("\"success\" : \"true\"")) {
                throw new FailedToRenewException("Renewal not successful.");
            }
            if (responseMessage.contains("\"Transaction refu")) {
                String end = responseMessage.substring(responseMessage.indexOf("\"Transaction refu") + 1);
                String errorMessage = end.substring(0, end.indexOf("\""));
                LOG.debug("Renewal not successful : " + errorMessage);
                throw new FailedToRenewException(errorMessage);
            }
        } finally {
            if (connect != null) {
                connect.disconnect();
            }
            CookieHandler.setDefault(cookieManager);
        }
    }
}
