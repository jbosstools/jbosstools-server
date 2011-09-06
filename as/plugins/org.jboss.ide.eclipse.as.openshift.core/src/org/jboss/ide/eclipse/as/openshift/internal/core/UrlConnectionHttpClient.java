package org.jboss.ide.eclipse.as.openshift.internal.core;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.MessageFormat;

import org.jboss.ide.eclipse.as.openshift.core.IHttpClient;
import org.jboss.ide.eclipse.as.openshift.internal.core.utils.StreamUtils;

public class UrlConnectionHttpClient implements IHttpClient {

	private static final String PROPERTY_CONTENT_TYPE = "Content-Type";
	private static final int TIMEOUT = 10 * 1024;

	private URL url;

	public UrlConnectionHttpClient(URL url) {
		this.url = url;
	}

	public String post(String data) throws HttpClientException {
		HttpURLConnection connection = null;
		try {
			connection = createConnection(url);
			connection.setDoOutput(true);
			StreamUtils.writeTo(data.getBytes(), connection.getOutputStream());
			return StreamUtils.readToString(connection.getInputStream());
		} catch (FileNotFoundException e) {
			/*
			 * thrown by #connect when server resonds with 404
			 */
			throw new HttpClientNotFoundException(
					MessageFormat.format("Could not find resource {0}", url.toString()));
		} catch (IOException e) {
			throw getException(e, connection);
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}
	}

	private HttpClientException getException(IOException ioe, HttpURLConnection connection) {
		try {
			int responseCode = connection.getResponseCode();
			if (responseCode == 500) {
				return new InternalServerErrorException(ioe);
			} else {
				return new HttpClientException(ioe);
			}
		} catch (IOException e) {
			return new HttpClientException(e);
		}
	}

	private HttpURLConnection createConnection(URL url) throws IOException {
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setUseCaches(false);
		connection.setDoInput(true);
		connection.setAllowUserInteraction(false);
		connection.setConnectTimeout(TIMEOUT);
		connection.setRequestProperty(PROPERTY_CONTENT_TYPE, "application/x-www-form-urlencoded");
		connection.setInstanceFollowRedirects(true);
		return connection;
	}
}
