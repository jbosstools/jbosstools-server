package org.jboss.ide.eclipse.as.openshift.internal.core;

public class HttpClientNotFoundException extends HttpClientException {

	public HttpClientNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

	public HttpClientNotFoundException(String message) {
		super(message);
	}

	private HttpClientNotFoundException(Throwable cause) {
		super(cause);
	}

}
