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
package org.jboss.ide.eclipse.as.openshift.core.internal;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.jboss.ide.eclipse.as.openshift.core.IApplication;
import org.jboss.ide.eclipse.as.openshift.core.ICartridge;
import org.jboss.ide.eclipse.as.openshift.core.IHttpClient;
import org.jboss.ide.eclipse.as.openshift.core.ISSHPublicKey;
import org.jboss.ide.eclipse.as.openshift.core.InvalidCredentialsOpenshiftException;
import org.jboss.ide.eclipse.as.openshift.core.OpenshiftEndpointException;
import org.jboss.ide.eclipse.as.openshift.core.OpenshiftException;
import org.jboss.ide.eclipse.as.openshift.core.internal.httpclient.HttpClientException;
import org.jboss.ide.eclipse.as.openshift.core.internal.httpclient.UnauthorizedException;
import org.jboss.ide.eclipse.as.openshift.core.internal.httpclient.UrlConnectionHttpClient;
import org.jboss.ide.eclipse.as.openshift.core.internal.request.AbstractDomainRequest;
import org.jboss.ide.eclipse.as.openshift.core.internal.request.ApplicationAction;
import org.jboss.ide.eclipse.as.openshift.core.internal.request.ApplicationRequest;
import org.jboss.ide.eclipse.as.openshift.core.internal.request.ChangeDomainRequest;
import org.jboss.ide.eclipse.as.openshift.core.internal.request.CreateDomainRequest;
import org.jboss.ide.eclipse.as.openshift.core.internal.request.ListCartridgesRequest;
import org.jboss.ide.eclipse.as.openshift.core.internal.request.OpenshiftEnvelopeFactory;
import org.jboss.ide.eclipse.as.openshift.core.internal.request.UserInfoRequest;
import org.jboss.ide.eclipse.as.openshift.core.internal.request.marshalling.ApplicationRequestJsonMarshaller;
import org.jboss.ide.eclipse.as.openshift.core.internal.request.marshalling.DomainRequestJsonMarshaller;
import org.jboss.ide.eclipse.as.openshift.core.internal.request.marshalling.ListCartridgesRequestJsonMarshaller;
import org.jboss.ide.eclipse.as.openshift.core.internal.request.marshalling.UserInfoRequestJsonMarshaller;
import org.jboss.ide.eclipse.as.openshift.core.internal.response.OpenshiftResponse;
import org.jboss.ide.eclipse.as.openshift.core.internal.response.unmarshalling.ApplicationResponseUnmarshaller;
import org.jboss.ide.eclipse.as.openshift.core.internal.response.unmarshalling.ApplicationStatusResponseUnmarshaller;
import org.jboss.ide.eclipse.as.openshift.core.internal.response.unmarshalling.DomainResponseUnmarshaller;
import org.jboss.ide.eclipse.as.openshift.core.internal.response.unmarshalling.JsonSanitizer;
import org.jboss.ide.eclipse.as.openshift.core.internal.response.unmarshalling.ListCartridgesResponseUnmarshaller;
import org.jboss.ide.eclipse.as.openshift.core.internal.response.unmarshalling.UserInfoResponseUnmarshaller;

/**
 * @author André Dietisheim
 */
public class OpenshiftService implements IOpenshiftService {

	private static final String BASE_URL = "https://openshift.redhat.com/broker";

	@Override
	public UserInfo getUserInfo(InternalUser user) throws OpenshiftException {
		UserInfoRequest request = new UserInfoRequest(user.getRhlogin(), true);
		String url = request.getUrlString(BASE_URL);
		try {
			String requestString = new UserInfoRequestJsonMarshaller().marshall(request);
			String openShiftRequestString = new OpenshiftEnvelopeFactory(user.getPassword(), requestString)
					.createString();
			String responseString = createHttpClient(url).post(openShiftRequestString);
			responseString = JsonSanitizer.sanitize(responseString);
			OpenshiftResponse<UserInfo> response =
					new UserInfoResponseUnmarshaller().unmarshall(responseString);
			return response.getOpenshiftObject();
		} catch (MalformedURLException e) {
			throw new OpenshiftEndpointException(
					url, e, "Could not get InternalUser info for InternalUser \"{0}\" at \"{1}\"", user.getRhlogin(), url, e);
		} catch (HttpClientException e) {
			throw new OpenshiftEndpointException(
					url, e, "Could not get InternalUser info for InternalUser \"{0}\" at \"{1}\"", user.getRhlogin(), url, e);
		}
	}

	@Override
	public List<ICartridge> getCartridges(InternalUser user) throws OpenshiftException {
		ListCartridgesRequest listCartridgesRequest = new ListCartridgesRequest(user.getRhlogin(), true);
		String url = listCartridgesRequest.getUrlString(BASE_URL);
		try {
			String listCartridgesRequestString =
					new ListCartridgesRequestJsonMarshaller().marshall(listCartridgesRequest);
			String request = new OpenshiftEnvelopeFactory(user.getPassword(), listCartridgesRequestString)
					.createString();
			String listCatridgesReponse = createHttpClient(url).post(request);
			listCatridgesReponse = JsonSanitizer.sanitize(listCatridgesReponse);
			OpenshiftResponse<List<ICartridge>> response =
					new ListCartridgesResponseUnmarshaller().unmarshall(listCatridgesReponse);
			return response.getOpenshiftObject();
		} catch (MalformedURLException e) {
			throw new OpenshiftEndpointException(url, e, "Could not list available cartridges at \"{0}\"", url);
		} catch (HttpClientException e) {
			throw new OpenshiftEndpointException(url, e, "Could not list available cartridges at \"{0}\"", url);
		}
	}

	@Override
	public IDomain createDomain(String name, ISSHPublicKey sshKey, InternalUser user) throws OpenshiftException {
		return requestDomainAction(new CreateDomainRequest(name, sshKey, user.getRhlogin(), true), user);
	}

	@Override
	public IDomain changeDomain(String newName, ISSHPublicKey sshKey, InternalUser user) throws OpenshiftException {
		return requestDomainAction(new ChangeDomainRequest(newName, sshKey, user.getRhlogin(), true), user);
	}

	protected IDomain requestDomainAction(AbstractDomainRequest request, InternalUser user) throws OpenshiftException {
		String url = request.getUrlString(BASE_URL);
		try {
			String requestString =
					new OpenshiftEnvelopeFactory(
							user.getPassword(),
							new DomainRequestJsonMarshaller().marshall(request))
							.createString();
			String responseString = createHttpClient(url).post(requestString);
			responseString = JsonSanitizer.sanitize(responseString);
			OpenshiftResponse<IDomain> response =
					new DomainResponseUnmarshaller(request.getName(), user).unmarshall(responseString);
			return response.getOpenshiftObject();
		} catch (MalformedURLException e) {
			throw new OpenshiftEndpointException(url, e, "Could not list available cartridges at \"{0}\"", url);
		} catch (HttpClientException e) {
			throw new OpenshiftEndpointException(url, e, "Could not list available cartridges at \"{0}\"", url);
		}
	}

	@Override
	public Application createApplication(String name, ICartridge cartridge, InternalUser user) throws OpenshiftException {
		Application application = requestApplicationAction(new ApplicationRequest(name, cartridge, ApplicationAction.CONFIGURE,
				user.getRhlogin(), true), user);
		return application;
	}

	@Override
	public void destroyApplication(String name, ICartridge cartridge, InternalUser user) throws OpenshiftException {
		IApplication application = requestApplicationAction(new ApplicationRequest(name, cartridge, ApplicationAction.DECONFIGURE,
				user.getRhlogin(), true), user);
		user.remove(application); 
	}

	@Override
	public IApplication startApplication(String name, ICartridge cartridge, InternalUser user) throws OpenshiftException {
		return requestApplicationAction(new ApplicationRequest(name, cartridge, ApplicationAction.START,
				user.getRhlogin(), true), user);
	}

	@Override
	public IApplication restartApplication(String name, ICartridge cartridge, InternalUser user) throws OpenshiftException {
		return requestApplicationAction(new ApplicationRequest(name, cartridge, ApplicationAction.RESTART,
				user.getRhlogin(), true), user);
	}

	@Override
	public IApplication stopApplication(String name, ICartridge cartridge, InternalUser user) throws OpenshiftException {
		return requestApplicationAction(new ApplicationRequest(name, cartridge, ApplicationAction.STOP,
				user.getRhlogin(), true), user);
	}

	@Override
	public String getStatus(String applicationName, ICartridge cartridge, InternalUser user) throws OpenshiftException {
		ApplicationRequest applicationRequest =
				new ApplicationRequest(applicationName, cartridge, ApplicationAction.STATUS, user.getRhlogin(), true);
		String url = applicationRequest.getUrlString(BASE_URL);
		try {
			String applicationRequestString =
					new ApplicationRequestJsonMarshaller().marshall(applicationRequest);
			String request = new OpenshiftEnvelopeFactory(user.getPassword(), applicationRequestString).createString();
			String response = createHttpClient(url).post(request);

			response = JsonSanitizer.sanitize(response);
			OpenshiftResponse<String> openshiftResponse =
					new ApplicationStatusResponseUnmarshaller().unmarshall(response);
			return openshiftResponse.getOpenshiftObject();
		} catch (MalformedURLException e) {
			throw new OpenshiftException(
					e, "Could not {0} application \"{1}\" at \"{2}\": Invalid url \"{2}\"",
					applicationRequest.getAction().toHumanReadable(), applicationRequest.getName(), url);
		} catch (UnauthorizedException e) {
			throw new InvalidCredentialsOpenshiftException(
					url, e,
					"Could not {0} application \"{1}\" at \"{2}\": Invalid credentials InternalUser \"{3}\", password \"{4}\"",
					applicationRequest.getAction().toHumanReadable(), applicationRequest.getName(), url,
					user.getRhlogin(), user.getPassword());
		} catch (HttpClientException e) {
			throw new OpenshiftEndpointException(
					url, e, "Could not {0} application \"{1}\" at \"{2}\"",
					applicationRequest.getAction().toHumanReadable(), applicationRequest.getName(), url);
		}
	}

	protected Application requestApplicationAction(ApplicationRequest applicationRequest, InternalUser user)
			throws OpenshiftException {
		String url = applicationRequest.getUrlString(BASE_URL);
		try {
			String applicationRequestString =
					new ApplicationRequestJsonMarshaller().marshall(applicationRequest);
			String request = new OpenshiftEnvelopeFactory(user.getPassword(), applicationRequestString).createString();
			String response = createHttpClient(url).post(request);

			response = JsonSanitizer.sanitize(response);
			OpenshiftResponse<Application> openshiftResponse =
					new ApplicationResponseUnmarshaller(applicationRequest.getName(),
							applicationRequest.getCartridge(), user, this).unmarshall(response);
			return openshiftResponse.getOpenshiftObject();
		} catch (MalformedURLException e) {
			throw new OpenshiftException(
					e, "Could not {0} application \"{1}\" at \"{2}\": Invalid url \"{2}\"",
					applicationRequest.getAction().toHumanReadable(), applicationRequest.getName(), url);
		} catch (UnauthorizedException e) {
			throw new InvalidCredentialsOpenshiftException(
					url, e,
					"Could not {0} application \"{1}\" at \"{2}\": Invalid credentials InternalUser \"{3}\", password \"{4}\"",
					applicationRequest.getAction().toHumanReadable(), applicationRequest.getName(), url,
					user.getRhlogin(),
					user.getPassword());
		} catch (HttpClientException e) {
			throw new OpenshiftEndpointException(
					url, e, "Could not {0} application \"{1}\" at \"{2}\"",
					applicationRequest.getAction().toHumanReadable(), applicationRequest.getName(), url);
		}
	}

	private IHttpClient createHttpClient(String url) throws MalformedURLException {
		return new UrlConnectionHttpClient(new URL(url));
	}
}
