package org.jboss.ide.eclipse.as.openshift.internal.core;

public class InternalServerErrorException extends HttpClientException {

	InternalServerErrorException(String message, Throwable cause) {
		super(message, cause);
	}

	InternalServerErrorException(String message) {
		super(message);
	}

	InternalServerErrorException(Throwable cause) {
		super(cause);
	}

}
