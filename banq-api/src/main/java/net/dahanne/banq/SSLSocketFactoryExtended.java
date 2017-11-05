package net.dahanne.banq;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

/*
Support all ciphers and TLS protocols supported by the device

Explanation :

Since iris.banq.qc.ca sends back :
    Protocol  : TLSv1
    Cipher    : RC4-MD5
and www.banq.qc.ca sends back :
    Protocol  : TLSv1
    Cipher    : DHE-RSA-AES256-SHA

and that apparently Android Nougat 7.0 does not accept many ciphers by default,
this SSLSocketFactoryExtended class will allow MaBanq to still connect to banq servers

Basically, the security level will be set by Banq https servers.

Code is coming from :
https://stackoverflow.com/a/40198170/24069
 */
public class SSLSocketFactoryExtended extends SSLSocketFactory {
    private SSLContext sslContext;
    private String[] ciphers;
    private String[] protocols;

    public SSLSocketFactoryExtended() throws NoSuchAlgorithmException, KeyManagementException {
        initSSLSocketFactoryEx(null, null, null);
    }

    public String[] getDefaultCipherSuites() {
        return ciphers;
    }

    public String[] getSupportedCipherSuites() {
        return ciphers;
    }

    public Socket createSocket(Socket s, String host, int port, boolean autoClose) throws IOException {
        SSLSocketFactory factory = sslContext.getSocketFactory();
        SSLSocket sslSocket = (SSLSocket) factory.createSocket(s, host, port, autoClose);

        sslSocket.setEnabledProtocols(protocols);
        sslSocket.setEnabledCipherSuites(ciphers);

        return sslSocket;
    }

    public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
        SSLSocketFactory factory = sslContext.getSocketFactory();
        SSLSocket ss = (SSLSocket) factory.createSocket(address, port, localAddress, localPort);

        ss.setEnabledProtocols(protocols);
        ss.setEnabledCipherSuites(ciphers);

        return ss;
    }

    public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException {
        SSLSocketFactory factory = sslContext.getSocketFactory();
        SSLSocket sslSocket = (SSLSocket) factory.createSocket(host, port, localHost, localPort);

        sslSocket.setEnabledProtocols(protocols);
        sslSocket.setEnabledCipherSuites(ciphers);

        return sslSocket;
    }

    public Socket createSocket(InetAddress host, int port) throws IOException {
        SSLSocketFactory factory = sslContext.getSocketFactory();
        SSLSocket sslSocket = (SSLSocket) factory.createSocket(host, port);

        sslSocket.setEnabledProtocols(protocols);
        sslSocket.setEnabledCipherSuites(ciphers);

        return sslSocket;
    }

    public Socket createSocket(String host, int port) throws IOException {
        SSLSocketFactory factory = sslContext.getSocketFactory();
        SSLSocket sslSocket = (SSLSocket) factory.createSocket(host, port);

        sslSocket.setEnabledProtocols(protocols);
        sslSocket.setEnabledCipherSuites(ciphers);

        return sslSocket;
    }

    private void initSSLSocketFactoryEx(KeyManager[] km, TrustManager[] tm, SecureRandom random)
            throws NoSuchAlgorithmException, KeyManagementException {
        sslContext = SSLContext.getInstance("TLS");
        sslContext.init(km, tm, random);

        protocols = getProtocolList();
        ciphers = getCipherList();
    }

    protected String[] getProtocolList() {
        String[] protocols = {"TLSv1", "TLSv1.1", "TLSv1.2", "TLSv1.3"};
        String[] availableProtocols;

        SSLSocket socket = null;

        try {
            SSLSocketFactory factory = sslContext.getSocketFactory();
            socket = (SSLSocket) factory.createSocket();

            availableProtocols = socket.getSupportedProtocols();
        } catch (Exception e) {
            return new String[]{"TLSv1"};
        } finally {
            if (socket != null)
                try {
                    socket.close();
                } catch (IOException e) {
                }
        }

        List<String> resultList = new ArrayList<String>();
        for (int i = 0; i < protocols.length; i++) {
            int idx = Arrays.binarySearch(availableProtocols, protocols[i]);
            if (idx >= 0) {
                resultList.add(protocols[i]);
            }
        }

        return resultList.toArray(new String[0]);
    }

    protected String[] getCipherList() {
        List<String> resultList = new ArrayList<String>();
        SSLSocketFactory factory = sslContext.getSocketFactory();
        for (String s : factory.getSupportedCipherSuites()) {
//            LOG.error("CipherSuite type = " + s);
            resultList.add(s);
        }
        return resultList.toArray(new String[resultList.size()]);
    }

}