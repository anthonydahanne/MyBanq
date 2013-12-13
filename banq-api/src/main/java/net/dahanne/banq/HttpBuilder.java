package net.dahanne.banq;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * HTTP Utility class
 */
public class HttpBuilder {

    private static final String COOKIE = "Cookie";
    public static final String ISO_8859_1 = "ISO-8859-1";

    public enum HttpMethod {
		GET, POST, PUT, DELETE
	}

	private HttpMethod httpMethod;
	private URL url;
	private Map<String, String> header;;
	private String data = "";
	private int connectionTimeout = -1;
	private int readTimeout = -1;
	private long ifModifiedSince = -1;
	private boolean useCache;
	private Set<String> cookie = new HashSet<String>();

	public HttpBuilder(HttpMethod method, String url)
			throws MalformedURLException {
		this.httpMethod = method;
		this.url = new URL(url);
		this.header = new HashMap<String, String>();
		this.header.put("Accept", "*/*");
	}

	public HttpBuilder(String url) throws MalformedURLException {
		this(HttpMethod.GET, url);
	}

	public HttpURLConnection connect() throws IOException {
		HttpURLConnection httpURLConnection = (HttpURLConnection) url
				.openConnection();
    httpURLConnection.setInstanceFollowRedirects(false);
    httpURLConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; U; Android 2.2; en-us; Nexus One Build/FRF91) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1");
		if (connectionTimeout != -1) {
			httpURLConnection.setConnectTimeout(connectionTimeout);
		}
		if (readTimeout != -1) {
			httpURLConnection.setReadTimeout(readTimeout);
		}
		if (ifModifiedSince != -1) {
			httpURLConnection.setIfModifiedSince(ifModifiedSince);
		}
		httpURLConnection.setUseCaches(useCache);
		httpURLConnection.setRequestMethod(httpMethod.toString());
		for (Entry<String, String> entry : header.entrySet()) {
			httpURLConnection.addRequestProperty(entry.getKey(),
					entry.getValue());
		}
		for (String c : cookie) {
			httpURLConnection.addRequestProperty(COOKIE, c);
		}
		if (HttpMethod.POST == httpMethod || HttpMethod.PUT == httpMethod) {
			httpURLConnection.setDoOutput(true);
			httpURLConnection.setDoInput(true);
		}
		if (!isBlank(data)) {
			byte[] bytes = data.getBytes(ISO_8859_1);
			httpURLConnection.setRequestProperty("Content-Length", ""
					+ bytes.length);
			OutputStream out = httpURLConnection.getOutputStream();
			out.write(bytes);
			out.flush();
			out.close();
		}
		if (validateResponseCode(httpURLConnection.getResponseCode())) {
			return httpURLConnection;
		}
		InputStream errorStream = httpURLConnection.getErrorStream();
		String erorMessage = "";
		if (errorStream != null) {
            erorMessage = toString(errorStream, ISO_8859_1);
        }
		throw new ConnectException("Bad response received ("
				+ httpURLConnection.getResponseCode() + ") for " + httpMethod
				+ " request : " + erorMessage);
	}

	private boolean validateResponseCode(int responseCode) {
		return (HttpURLConnection.HTTP_OK == responseCode && httpMethod == HttpMethod.GET)
        || HttpURLConnection.HTTP_MOVED_TEMP == responseCode
				|| (HttpURLConnection.HTTP_CREATED == responseCode && httpMethod == HttpMethod.PUT)
				|| (HttpURLConnection.HTTP_CREATED == responseCode && httpMethod == HttpMethod.POST)
				|| (HttpURLConnection.HTTP_ACCEPTED == responseCode && httpMethod == HttpMethod.POST)
				|| (HttpURLConnection.HTTP_OK == responseCode && httpMethod == HttpMethod.POST)
				|| (HttpURLConnection.HTTP_ACCEPTED == responseCode && httpMethod == HttpMethod.DELETE)
				|| (HttpURLConnection.HTTP_OK == responseCode && httpMethod == HttpMethod.DELETE);
	}

	public HttpBuilder header(Map<String, String> infosHeader) {
		this.header.putAll(infosHeader);
		return this;
	}

	public HttpBuilder data(String data) {
		this.data = data;
		return this;
	}

	public HttpBuilder data(InputStream stream) throws IOException {
        this.data = toString(stream, ISO_8859_1);
        return this;
	}

	public HttpBuilder data(Map<String, String> data) {
		try {
			for (Entry<String, String> entry : data.entrySet()) {
				this.data += entry.getKey() + "="
						+ URLEncoder.encode(entry.getValue(), ISO_8859_1) + "&";
			}
			this.data = this.data.substring(0, this.data.length() - 1);
//			Log.i(getClass().getSimpleName(), "Encoded payload : " + this.data);
		} catch (UnsupportedEncodingException e) {
//			Log.e(getClass().getSimpleName(), e.getMessage(), e);
		}
		return this;
	}

	public HttpBuilder readTimeOut(int readTimeout) {
		this.readTimeout = readTimeout;
		return this;
	}

	public HttpBuilder connectionTimeOut(int connectionTimeout) {
		this.connectionTimeout = connectionTimeout;
		return this;
	}

	public HttpBuilder ifModifiedSince(long ifModifiedSince) {
		this.ifModifiedSince = ifModifiedSince;
		return this;
	}

	public HttpBuilder useCache(boolean useCache) {
		this.useCache = useCache;
		return this;
	}

	public HttpBuilder cookie(Set<String> cookie) {
		this.cookie = cookie;
		return this;
	}

	private boolean isBlank(CharSequence cs) {
		int strLen;
		if (cs == null || (strLen = cs.length()) == 0) {
			return true;
		}
		for (int i = 0; i < strLen; i++) {
			if (Character.isWhitespace(cs.charAt(i)) == false) {
				return false;
			}
		}
		return true;
	}

    public static String toString(InputStream stream, String charset) throws IOException {
        Writer writer = new StringWriter();
		char[] buffer = new char[1024];
		try {
			Reader reader = new BufferedReader(new InputStreamReader(stream,
                    Charset.forName(charset)));
            int n;
			while ((n = reader.read(buffer)) != -1) {
				writer.write(buffer, 0, n);
			}
		} finally {
			stream.close();
		}
		return writer.toString();
	}

}
