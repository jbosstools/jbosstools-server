package org.jboss.ide.eclipse.as.openshift.core;

import org.jboss.ide.eclipse.as.openshift.core.internal.httpclient.HttpClientException;

public interface IHttpClient {

	public String post(String data) throws HttpClientException;
}
