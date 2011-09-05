package org.jboss.ide.eclipse.as.openshift.internal.core;

public interface HttpClient {

	public String post(String data) throws HttpClientException;
}
