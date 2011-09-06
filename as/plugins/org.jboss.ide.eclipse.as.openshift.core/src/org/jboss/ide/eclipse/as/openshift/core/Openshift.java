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
import java.util.ArrayList;
import java.util.List;

import org.eclipse.osgi.util.NLS;
import org.jboss.dmr.ModelNode;
import org.jboss.ide.eclipse.as.openshift.core.internal.marshalling.ListCartridgesRequestJsonMarshaller;
import org.jboss.ide.eclipse.as.openshift.core.internal.marshalling.OpenshiftJsonRequestFactory;
import org.jboss.ide.eclipse.as.openshift.core.internal.marshalling.UserInfoRequestJsonMarshaller;
import org.jboss.ide.eclipse.as.openshift.internal.core.Cartridge;
import org.jboss.ide.eclipse.as.openshift.internal.core.HttpClientException;
import org.jboss.ide.eclipse.as.openshift.internal.core.UrlConnectionHttpClient;
import org.jboss.ide.eclipse.as.openshift.internal.core.UserInfo;
import org.jboss.ide.eclipse.as.openshift.internal.core.request.ListCartridgesRequest;
import org.jboss.ide.eclipse.as.openshift.internal.core.request.UserInfoRequest;
import org.jboss.ide.eclipse.as.openshift.internal.core.utils.UrlBuilder;

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
		UrlBuilder userInfoUrlBuilder = new UrlBuilder(BASE_URL).path("userinfo");
		try {
			String userInfoRequest = new UserInfoRequestJsonMarshaller().marshall(new UserInfoRequest(username, true));
			String request = new OpenshiftJsonRequestFactory(password, userInfoRequest).create();
			String userInfoResponse = createHttpClient(userInfoUrlBuilder.toUrl()).post(request);
			ModelNode userInfoReponse = ModelNode.fromJSONString(userInfoResponse);
			return new UserInfo(
					userInfoReponse.get("rhlogin").asString(),
					userInfoReponse.get("uuid").asString(),
					userInfoReponse.get("ssh_key").asString(),
					userInfoReponse.get("rhc_domain").asString(),
					userInfoReponse.get("namespace").asString());
		} catch (MalformedURLException e) {
			throw new OpenshiftException(
					NLS.bind("Could not get user info for user \"{0}\" at \"{1}\"", username,
							userInfoUrlBuilder.toString()), e);
		} catch (HttpClientException e) {
			throw new OpenshiftException(
					NLS.bind("Could not get user info for user \"{0}\" at \"{1}\"", username,
							userInfoUrlBuilder.toString()), e);
		}
	}

	public UserInfo createApplication(String name) throws OpenshiftException {
		throw new UnsupportedOperationException();
	}
	
	private IHttpClient createHttpClient(URL url) {
		return new UrlConnectionHttpClient(url);
	}

	@Override
	public List<Cartridge> getCartridges() throws OpenshiftException {
		UrlBuilder userInfoUrlBuilder = new UrlBuilder(BASE_URL).path("userinfo");
		try {
			String listCartridgesRequest = new ListCartridgesRequestJsonMarshaller().marshall(new ListCartridgesRequest(username, true));
			String request = new OpenshiftJsonRequestFactory(password, listCartridgesRequest).create();
			String listCatridgesReponse = createHttpClient(userInfoUrlBuilder.toUrl()).post(request);
			ModelNode userInfoReponse = ModelNode.fromJSONString(listCatridgesReponse);
			List<Cartridge> cartridges = new ArrayList<Cartridge>();
			return cartridges;
		} catch (MalformedURLException e) {
			throw new OpenshiftException(
					NLS.bind("Could not get user info for user \"{0}\" at \"{1}\"", username,
							userInfoUrlBuilder.toString()), e);
		} catch (HttpClientException e) {
			throw new OpenshiftException(
					NLS.bind("Could not get user info for user \"{0}\" at \"{1}\"", username,
							userInfoUrlBuilder.toString()), e);
		}
	}

}
