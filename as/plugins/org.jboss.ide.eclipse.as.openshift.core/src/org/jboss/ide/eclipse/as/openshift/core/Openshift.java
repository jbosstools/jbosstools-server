/******************************************************************************* 
 * Copyright (c) 2007 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/
package org.jboss.ide.eclipse.as.openshift.core;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.eclipse.osgi.util.NLS;
import org.jboss.ide.eclipse.as.openshift.core.internal.marshalling.ApplicationRequestJsonMarshaller;
import org.jboss.ide.eclipse.as.openshift.core.internal.marshalling.ListCartridgesRequestJsonMarshaller;
import org.jboss.ide.eclipse.as.openshift.core.internal.marshalling.UserInfoRequestJsonMarshaller;
import org.jboss.ide.eclipse.as.openshift.internal.core.Cartridge;
import org.jboss.ide.eclipse.as.openshift.internal.core.HttpClientException;
import org.jboss.ide.eclipse.as.openshift.internal.core.UrlConnectionHttpClient;
import org.jboss.ide.eclipse.as.openshift.internal.core.UserInfo;
import org.jboss.ide.eclipse.as.openshift.internal.core.request.ApplicationAction;
import org.jboss.ide.eclipse.as.openshift.internal.core.request.ApplicationRequest;
import org.jboss.ide.eclipse.as.openshift.internal.core.request.ListCartridgesRequest;
import org.jboss.ide.eclipse.as.openshift.internal.core.request.OpenshiftJsonRequestFactory;
import org.jboss.ide.eclipse.as.openshift.internal.core.request.UserInfoRequest;
import org.jboss.ide.eclipse.as.openshift.internal.core.response.ApplicationResponseFactory;
import org.jboss.ide.eclipse.as.openshift.internal.core.response.OpenshiftResponse;

/**
 * @author André Dietisheim
 */
public class Openshift implements IOpenshift {

	private static final String BASE_URL = "https://openshift.redhat.com/broker";

	private String username;
	private String password;

	public Openshift(String username, String password) {
		this.username = username;
		this.password = password;
	}

	public UserInfo getUserInfo() throws OpenshiftException {
		UserInfoRequest userInfoRequest = new UserInfoRequest(username, true);
		try {
			String userInfoRequestString = new UserInfoRequestJsonMarshaller().marshall(userInfoRequest);
			String request = new OpenshiftJsonRequestFactory(password, userInfoRequestString).create();
			String userInfoResponse = createHttpClient(userInfoRequest.getUrl(BASE_URL)).post(request);
			throw new UnsupportedOperationException();
		} catch (MalformedURLException e) {
			throw new OpenshiftException(
					NLS.bind("Could not get user info for user \"{0}\" at \"{1}\"", username,
							userInfoRequest.getUrlString(BASE_URL)), e);
		} catch (HttpClientException e) {
			throw new OpenshiftException(
					NLS.bind("Could not get user info for user \"{0}\" at \"{1}\"", username,
							userInfoRequest.getUrlString(BASE_URL)), e);
		}
	}

	@Override
	public List<Cartridge> getCartridges() throws OpenshiftException {
		ListCartridgesRequest listCartridgesRequest = new ListCartridgesRequest(username, true);
		try {
			String listCartridgesRequestString =
					new ListCartridgesRequestJsonMarshaller().marshall(listCartridgesRequest);
			String request = new OpenshiftJsonRequestFactory(password, listCartridgesRequestString).create();
			String listCatridgesReponse = createHttpClient(listCartridgesRequest.getUrl(BASE_URL)).post(request);
			throw new UnsupportedOperationException();
		} catch (MalformedURLException e) {
			throw new OpenshiftException(
					NLS.bind("Could not list available cartridges at \"{0}\"",
							listCartridgesRequest.getUrlString(BASE_URL)), e);
		} catch (HttpClientException e) {
			throw new OpenshiftException(
					NLS.bind("Could not list available cartridges at \"{0}\"",
							listCartridgesRequest.getUrlString(BASE_URL)), e);
		}
	}	

	@Override
	public Application createApplication(String name, Cartridge cartridge) throws OpenshiftException {
		ApplicationRequest applicationRequest = new ApplicationRequest(name, cartridge, ApplicationAction.CONFIGURE, username, true);
		try {
			String listCartridgesRequestString =
					new ApplicationRequestJsonMarshaller().marshall(applicationRequest);
			String request = new OpenshiftJsonRequestFactory(password, listCartridgesRequestString).create();
			String response = createHttpClient(applicationRequest.getUrl(BASE_URL)).post(request);
			OpenshiftResponse<Application> openshiftResponse = new ApplicationResponseFactory(response, name, cartridge).create();
			return openshiftResponse.getData();
		} catch (MalformedURLException e) {
			throw new OpenshiftException(
					NLS.bind("Could not create application \"{0}\" at \"{1}\"",
							name, applicationRequest.getUrlString(BASE_URL)), e);
		} catch (HttpClientException e) {
			throw new OpenshiftException(
					NLS.bind("Could not create application \"{0}\" at \"{1}\"",
							name, applicationRequest.getUrlString(BASE_URL)), e);
		}

	}

	private IHttpClient createHttpClient(URL url) {
		return new UrlConnectionHttpClient(url);
	}

}
