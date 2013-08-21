package net.dahanne.banq;

import net.dahanne.banq.exceptions.FailedToRenewException;
import net.dahanne.banq.exceptions.InvalidSessionException;
import net.dahanne.banq.model.BorrowedItem;
import net.dahanne.banq.model.Details;

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

    public Set<String> authenticate(String username, String password) throws IOException, InterruptedException {
        Set<String> cookies = new HashSet<String>();

        HttpURLConnection connect = null;
        String location = null;
        InputStream inputStream = null;
        String responseMessage = null;
        try {
            connect = new HttpBuilder("http://www.banq.qc.ca/mobile2/mon_dossier/detail.jsp").cookie(cookies).connect();
            location = getLocationHeader(connect);
            enrichCookies(connect, cookies);
            inputStream = connect.getInputStream();
            responseMessage = HttpBuilder.toString(inputStream);
//      System.out.println(responseMessage);
        } finally {
            if (connect != null) {
                connect.disconnect();
            }
        }

        try {
            connect = new HttpBuilder(location).connect();
            location = getLocationHeader(connect);
            enrichCookies(connect, cookies);
            inputStream = connect.getInputStream();
            responseMessage = HttpBuilder.toString(inputStream);
//      System.out.println(responseMessage);
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
            responseMessage = HttpBuilder.toString(inputStream);
//    System.out.println(responseMessage);
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
            responseMessage = HttpBuilder.toString(inputStream);
//    System.out.println(responseMessage);


            Document parse = Jsoup.parse(responseMessage);
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

            connect = new HttpBuilder(HttpBuilder.HttpMethod.POST, "https://www.banq.qc.ca/Shibboleth.sso/SAML2/POST").data(data).cookie(cookies).connect();

            location = getLocationHeader(connect);
            enrichCookies(connect, cookies);

            inputStream = connect.getInputStream();
            responseMessage = HttpBuilder.toString(inputStream);
//    System.out.println(responseMessage);
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
                System.out.println("COOKIE : " + cookie);
                cookies.add(cookie);
            }
        }
    }

    private static String getLocationHeader(HttpURLConnection connect) {
        String headerName;
        String location = null;
        for (int i = 1; (headerName = connect.getHeaderFieldKey(i)) != null; i++) {
            if (headerName.equals("Location")) {
                location = connect.getHeaderField(i);
            }
        }
        return location;
    }

    public static void main(String[] args) throws IOException, InterruptedException, ParseException, InvalidSessionException {
        if (args == null || args.length < 2) {
            System.out.println("Wrong number of arguments, please supply 2 arguments : username and password");
        }
        BanqClient bc = new BanqClient();
        Set<String> cookies = bc.authenticate(args[0], args[1]);
        String detailsPage = bc.getDetailsPage(cookies);
        Details details = bc.parseDetails(detailsPage);

        System.out.println("Borrower name  : " + details.getName());
        System.out.println("Current debt  : " + details.getCurrentDebt());
        System.out.println("Subscription expiration date : " + details.getExpirationDate());

    }

    String getDetailsPage(Set<String> cookies) throws IOException, ParseException, InvalidSessionException {
        HttpURLConnection connect = null;
        try {
            connect = new HttpBuilder("http://www.banq.qc.ca/mobile2/mon_dossier/detail.jsp").cookie(cookies).connect();
            if (connect.getResponseCode() == 302) {
                // the session is not usable, we should re authenticate from there.
                throw new InvalidSessionException();
            }

            InputStream inputStream = connect.getInputStream();
            return HttpBuilder.toString(inputStream);
        } finally {
            if (connect != null) {
                connect.disconnect();
            }
        }
    }

    Details parseDetails(String responseMessage) throws ParseException {
//        System.out.println(responseMessage);
        Document parse = Jsoup.parse(responseMessage);
        Element contenu = parse.getElementById("Contenu");
        Details details = null;
        if (contenu != null) {

            Element detailsElement = contenu.getElementsByTag("p").first();
            List<Node> nodes = detailsElement.childNodes();
            String name = nodes.get(2).toString().trim();
            String expirationDate = nodes.get(4).toString();
            Date expirationDateAsDate = toDate(expirationDate.substring(expirationDate.indexOf(":") + 2) + "-00:00");

            Element userIdElement = contenu.getElementsByAttributeValue("name", "userID").first();

            String currentDebtText = nodes.get(6).toString();
            String currentDebt = currentDebtText.substring(currentDebtText.indexOf(":") + 2).trim();

            if (userIdElement == null) {
                details = new Details(name, expirationDateAsDate, currentDebt);
            } else {
                List<BorrowedItem> borrowedItemsList = new ArrayList<BorrowedItem>();
                String userIdValue = userIdElement.attr("value");

                Elements borrowedItems = contenu.getElementsByTag("li");
                for (Element borrowedItem : borrowedItems) {
                    List<Node> borrowedItemProperties = borrowedItem.childNodes();

                    String title = borrowedItemProperties.get(0).toString();

                    String shelfMarkText = borrowedItemProperties.get(2).toString();
                    String shelfMark = shelfMarkText.substring(shelfMarkText.indexOf(":") + 2).trim();

                    String borrowedDate = borrowedItemProperties.get(4).toString();
                    Date borrowedDateAsDate = toDate(borrowedDate.substring(borrowedDate.indexOf(":") + 2));

                    String toBeReturnedBefore = borrowedItemProperties.get(6).toString();
                    Date toBeReturnedBeforeAsDate = toDate(toBeReturnedBefore.substring(toBeReturnedBefore.indexOf(":") + 2));

                    Element docNoElement = borrowedItem.getElementsByAttributeValue("name", "docNo").first();
                    String docNoValue = docNoElement.attr("value");

                    borrowedItemsList.add(new BorrowedItem(title, shelfMark, borrowedDateAsDate, toBeReturnedBeforeAsDate, docNoValue, userIdValue));

                }
                details = new Details(name, expirationDateAsDate, currentDebt, userIdValue, borrowedItemsList);
            }

        }
        return details;
    }

    static Date toDate(String substring) throws ParseException {
        substring = substring + " EDT";
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH:mm zzz");
        return formatter.parse(substring);
    }

    public Details getDetails(Set<String> cookies) throws ParseException, InvalidSessionException, IOException {
        String detailsPage = this.getDetailsPage(cookies);
        Details details = this.parseDetails(detailsPage);
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
            responseMessage = HttpBuilder.toString(inputStream);
//    System.out.println(responseMessage);
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
