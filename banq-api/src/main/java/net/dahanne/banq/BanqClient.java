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
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
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

    public Set<String> authenticate(String username, String password) throws IOException, InterruptedException, InvalidCredentialsException {
        Set<String> cookies = new HashSet<String>();

        if (username.length() != 8) {
            throw new InvalidCredentialsException();
        }

        HttpURLConnection connect = null;
        String location = null;
        InputStream inputStream = null;
        String responseMessage = null;
        try {
//            connect = new HttpBuilder("http://www.banq.qc.ca/mobile2/mon_dossier/detail.jsp").cookie(cookies).connect();
            connect = new HttpBuilder("https://iris.banq.qc.ca/alswww2.dll/APS_ZONES?fn=MyZone&Style=Mobile&Lang=FRE").cookie(cookies).connect();

            location = getLocationHeader(connect);
            enrichCookies(connect, cookies);
            inputStream = connect.getInputStream();
            responseMessage = HttpBuilder.toString(inputStream, HttpBuilder.ISO_8859_1);
        } finally {
            if (connect != null) {
                connect.disconnect();
            }
        }

        try {
//            connect = new HttpBuilder(location).connect();
            connect = new HttpBuilder("https://iris.banq.qc.ca/login/login.aspx?Lang=FRE&retObj=APS_ZONES%3Ffn%3DMyZoneHomePage%26Style%3DMobile%26SubStyle%3D%26Lang%3DFRE%26ResponseEncoding%3Dutf-8&retPage=").connect();

            location = getLocationHeader(connect);
            enrichCookies(connect, cookies);
            inputStream = connect.getInputStream();
            responseMessage = HttpBuilder.toString(inputStream, HttpBuilder.ISO_8859_1);
        } finally {
            if (connect != null) {
                connect.disconnect();
            }
        }

        //https://www.banq.qc.ca:443/idp/Authn/UserPassword for JSESSION_ID
        try {
            connect = new HttpBuilder(location).cookie(cookies).connect();
            location = getLocationHeader(connect);
            enrichCookies(connect, cookies);
            inputStream = connect.getInputStream();
            responseMessage = HttpBuilder.toString(inputStream, HttpBuilder.ISO_8859_1);
        } finally {
            if (connect != null) {
                connect.disconnect();
            }
        }


        try {
            connect = new HttpBuilder(location).cookie(cookies).connect();
            location = getLocationHeader(connect);
            enrichCookies(connect, cookies);
            inputStream = connect.getInputStream();
            responseMessage = HttpBuilder.toString(inputStream, HttpBuilder.ISO_8859_1);
        } finally {
            if (connect != null) {
                connect.disconnect();
            }
        }


        String relayState = null;
        String SAMLResponse = null;
        HashMap<String, String> data = new HashMap<String, String>();

        try {
            data.put("j_username", username);
            data.put("j_password", password);
            connect = new HttpBuilder(HttpBuilder.HttpMethod.POST, "https://www.banq.qc.ca/idp/Authn/UserPassword").data(data).cookie(cookies).connect();
            location = getLocationHeader(connect);
            enrichCookies(connect, cookies);
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
        } finally {
            if (connect != null) {
                connect.disconnect();
            }
        }

        try {
            data = new HashMap<String, String>();
            data.put("RelayState", relayState);
            data.put("SAMLResponse", SAMLResponse);

            connect = new HttpBuilder(HttpBuilder.HttpMethod.POST, "https://iris.banq.qc.ca/Shibboleth.sso/SAML2/POST").data(data).cookie(cookies).connect();

            location = getLocationHeader(connect);
            enrichCookies(connect, cookies);

            inputStream = connect.getInputStream();
            responseMessage = HttpBuilder.toString(inputStream, HttpBuilder.ISO_8859_1);
        } finally {
            if (connect != null) {
                connect.disconnect();
            }
        }
        try {
            connect = new HttpBuilder(location).cookie(cookies).connect();
            location = getLocationHeader(connect);

            if (location != null) {
                return authenticate(username, password);
            }
        } finally {
            if (connect != null) {
                connect.disconnect();
            }
        }
        return cookies;
    }

    private void enrichCookies(HttpURLConnection connect, Set<String> cookies) {
        String headerName;
        for (int i = 1; (headerName = connect.getHeaderFieldKey(i)) != null; i++) {
            if (headerName.equals("Set-Cookie")) {
                String cookie = connect.getHeaderField(i);
//                System.out.println("COOKIE : " + cookie);
                cookies.add(cookie);
            }
        }
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


    private String getPage(Set<String> cookies, String url) throws IOException, InvalidSessionException {
        HttpURLConnection connect = null;
        try {
            connect = new HttpBuilder(url).cookie(cookies).connect();
            if (connect.getResponseCode() == 302) {
                // the session is not usable, we should re authenticate from there.
                throw new InvalidSessionException();
            }

            InputStream inputStream = connect.getInputStream();
            return HttpBuilder.toString(inputStream, "UTF-8");
        } finally {
            if (connect != null) {
                connect.disconnect();
            }
        }
    }

    String getDetailsPage(Set<String> cookies) throws IOException, ParseException, InvalidSessionException {
        return getPage(cookies, "https://iris.banq.qc.ca/alswww2.dll/APS_ZONES?fn=MyZone&Style=Mobile&Lang=FRE&ResponseEncoding=utf-8");
    }

    String getContactDetailsPage(Set<String> cookies) throws IOException, ParseException, InvalidSessionException {
        return getPage(cookies, "https://iris.banq.qc.ca/alswww2.dll/APS_ZONES?fn=MyContactDetails&Style=Mobile&Lang=FRE&ResponseEncoding=utf-8");
    }

    String getReservationsPage(Set<String> cookies) throws IOException, ParseException, InvalidSessionException {
        return getPage(cookies, "https://iris.banq.qc.ca/alswww2.dll/APS_ZONES?fn=MyReservations&Style=Mobile&SubStyle=&Lang=FRE&ResponseEncoding=utf-8");
    }

    String getLoansHistory(Set<String> cookies) throws IOException, ParseException, InvalidSessionException {
        return getPage(cookies, "https://iris.banq.qc.ca/alswww2.dll/APS_ZONES?fn=MyLoansHistory&Style=Mobile&SubStyle=&Lang=FRE&ResponseEncoding=utf-8");
    }

    String getAccountHistory(Set<String> cookies) throws IOException, ParseException, InvalidSessionException {
        StringBuilder sb = new StringBuilder();
        String originalPage = getPage(cookies, "https://iris.banq.qc.ca/alswww2.dll/APS_ZONES?fn=MyAccountHistory&Style=Mobile&SubStyle=&Lang=FRE&ResponseEncoding=utf-8");
//        System.out.println("originalPage count : " + originalPage.length());
        sb.append(originalPage);
        while (originalPage.contains("Method=PageDown")) {
            //<META NAME="ZonesObjName"  CONTENT="Obj_1630781387085165">
            Document parse = Jsoup.parse(originalPage);
            Element zonesObjNameTag = parse.getElementsByAttributeValue("NAME", "ZonesObjName").first();
            String objName = zonesObjNameTag.attr("CONTENT");
            String url = "https://iris.banq.qc.ca/alswww2.dll/" + objName + "?Style=Mobile&SubStyle=&Lang=FRE&ResponseEncoding=utf-8&Method=PageDown&PageSize=10";
//            System.out.println(url);
            originalPage = getPage(cookies, url);
//            System.out.println("originalPage count : " + originalPage.length());
            sb.append(originalPage);
        }
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
        Document parse = Jsoup.parse(responseMessage);
        Element contenu = parse.getElementById("pageContent");
        Details details = null;
        if (contenu != null) {

            Element userIdElement = contenu.getElementsByClass("MyZonetitleText").first();
            String name = userIdElement.text().trim();


            Element summaryDetailsTable = contenu.getElementsByClass("SummaryDetailsTable").first();
            Elements accountSummaryCounterValueCells = summaryDetailsTable.getElementsByAttributeValueStarting("class", "AccountSummaryCounterValueCell");
            int reservationsNumber = Integer.valueOf(accountSummaryCounterValueCells.get(1).text().trim());
            int messagesNumber = Integer.valueOf(accountSummaryCounterValueCells.get(2).text().trim());
            String debtText = accountSummaryCounterValueCells.get(3).text();
            String currentDebt = debtText.substring(0, debtText.indexOf("$") + 1).trim();
            String lateFeesToCome = accountSummaryCounterValueCells.get(4).text();

            Element inlineCurrentLoans = contenu.getElementsByClass("InlineCurrentLoans").first();
            Elements loanBrowseTables = inlineCurrentLoans.getElementsByClass("LoanBrowseTable");

            if (loanBrowseTables == null) {
                details = new Details(name, currentDebt, lateFeesToCome, messagesNumber, reservationsNumber);
            } else {
                List<BorrowedItem> borrowedItemsList = new ArrayList<BorrowedItem>();

                for (Element loanBrowseTable : loanBrowseTables) {

                    String authorInfo = null;
                    Elements loanBrowseFieldDataCells = loanBrowseTable.getElementsByClass("LoanBrowseFieldDataCell");
                    String title = loanBrowseFieldDataCells.get(0).child(0).text();
                    String titleAndAuthorHtml = loanBrowseFieldDataCells.get(0).html();
                    if (titleAndAuthorHtml.indexOf("/ ") != -1) {
                        authorInfo = titleAndAuthorHtml.substring(titleAndAuthorHtml.indexOf("/ ") + 2).trim();
                    }
                    String documentNumber = loanBrowseFieldDataCells.get(1).text().trim();
                    String borrowedItemLocation = loanBrowseFieldDataCells.get(2).text().trim();
                    Date borrowedDate = toDate(loanBrowseFieldDataCells.get(3).text().trim());
                    Date returnDate = toDate(loanBrowseFieldDataCells.get(4).getElementsByClass("LoanDate").first().child(0).text().toString().trim());
                    //TODO : LateLoan field

                    boolean isRenewable = loanBrowseTable.getElementById("buttonRenewLoan") != null;
                    borrowedItemsList.add(new BorrowedItem(title, authorInfo, borrowedItemLocation, borrowedDate, returnDate, documentNumber, isRenewable));
                }

                details = new Details(name, currentDebt, lateFeesToCome, messagesNumber, reservationsNumber, borrowedItemsList);
            }
        }
        return details;
    }

    static Date toDate(String substring) throws ParseException {
        substring = substring + " EDT";
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy 'EDT'");
        return formatter.parse(substring);
    }

    public Details getDetails(Set<String> cookies) throws ParseException, InvalidSessionException, IOException {
        String detailsPage = this.getDetailsPage(cookies);
        Details details = this.parseDetails(detailsPage);
        String reservationsPage;
//        List<Reservation> reservations = this.parseReservations(reservationsPage);
        return details;
    }

    public void renew(Set<String> cookies, String userId, String docNo) throws FailedToRenewException, IOException, InvalidSessionException {

        HttpURLConnection connect = null;
        connect = new HttpBuilder("http://www.banq.qc.ca/mobile2/mon_dossier/detail.jsp").cookie(cookies).connect();
        if (connect.getResponseCode() == 302) {
            // the session is not usable, we should re authenticate from there.
            throw new InvalidSessionException();
        }

        String location = "http://www.banq.qc.ca/mobile2/renew.jsp";
        connect = null;
        InputStream inputStream = null;
        String responseMessage = null;
        HashMap<String, String> data = new HashMap<String, String>();
        data.put("docNo", docNo);
        data.put("userID", userId);
        try {
            connect = new HttpBuilder(HttpBuilder.HttpMethod.POST, location).data(data).cookie(cookies).connect();
            location = getLocationHeader(connect);
            enrichCookies(connect, cookies);
            inputStream = connect.getInputStream();
            responseMessage = HttpBuilder.toString(inputStream, HttpBuilder.ISO_8859_1);
        } finally {
            if (connect != null) {
                connect.disconnect();
            }
        }

        Document parse = Jsoup.parse(responseMessage);
        Element contenu = parse.getElementById("Contenu");
        if (contenu.html().contains("La transaction a &eacute;chou&eacute;e")) {
            StringBuilder sb = new StringBuilder();
            for (Node node : contenu.childNodes()) {
                String nodeString = node.toString();
                // banq returns some soap envelope, with some internal error codes, we filter this
                if (!nodeString.contains("soap:envelope") && !nodeString.contains("encoding=") && !nodeString.contains("<br />") && !nodeString.trim().equals("")) {
                    sb.append(nodeString.trim()).append("<br />");
                }
            }
            throw new FailedToRenewException(sb.toString());
        }

    }
}
