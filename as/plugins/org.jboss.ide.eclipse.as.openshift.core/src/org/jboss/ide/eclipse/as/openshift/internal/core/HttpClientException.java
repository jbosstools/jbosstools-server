package org.jboss.ide.eclipse.as.openshift.internal.core;

public class HttpClientException extends Exception {

	public  HttpClientException(String message, Throwable cause) {
		super(message, cause);
	}

	public HttpClientException(String message) {
		super(message);
	}

	public HttpClientException(Throwable cause) {
		super(cause);
	}

}
