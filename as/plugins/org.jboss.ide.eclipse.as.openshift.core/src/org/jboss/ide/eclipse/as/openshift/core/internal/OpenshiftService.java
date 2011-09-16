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

import org.jboss.ide.eclipse.as.openshift.core.Application;
import org.jboss.ide.eclipse.as.openshift.core.Cartridge;
import org.jboss.ide.eclipse.as.openshift.core.Domain;
import org.jboss.ide.eclipse.as.openshift.core.IHttpClient;
import org.jboss.ide.eclipse.as.openshift.core.IOpenshiftService;
import org.jboss.ide.eclipse.as.openshift.core.InvalidCredentialsOpenshiftException;
import org.jboss.ide.eclipse.as.openshift.core.OpenshiftEndpointException;
import org.jboss.ide.eclipse.as.openshift.core.OpenshiftException;
import org.jboss.ide.eclipse.as.openshift.core.SSHKey;
import org.jboss.ide.eclipse.as.openshift.core.UserInfo;
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
import org.jboss.ide.eclipse.as.openshift.core.internal.response.ApplicationResponseUnmarshaller;
import org.jboss.ide.eclipse.as.openshift.core.internal.response.ApplicationStatusResponseUnmarshaller;
import org.jboss.ide.eclipse.as.openshift.core.internal.response.DomainResponseUnmarshaller;
import org.jboss.ide.eclipse.as.openshift.core.internal.response.JsonSanitizer;
import org.jboss.ide.eclipse.as.openshift.core.internal.response.ListCartridgesResponseUnmarshaller;
import org.jboss.ide.eclipse.as.openshift.core.internal.response.OpenshiftResponse;
import org.jboss.ide.eclipse.as.openshift.core.internal.response.UserInfoResponseUnmarshaller;

/**
 * @author André Dietisheim
 */
public class OpenshiftService implements IOpenshiftService {

	private static final String BASE_URL = "https://openshift.redhat.com/broker";

	private String username;
	private String password;

	public OpenshiftService(String username, String password) {
		this.username = username;
		this.password = password;
	}

	public UserInfo getUserInfo() throws OpenshiftException {
		UserInfoRequest request = new UserInfoRequest(username, true);
		String url = request.getUrlString(BASE_URL);
		try {
			String requestString = new UserInfoRequestJsonMarshaller().marshall(request);
			String openShiftRequestString = new OpenshiftEnvelopeFactory(password, requestString).createString();
			String responseString = createHttpClient(url).post(openShiftRequestString);
			responseString = JsonSanitizer.sanitize(responseString);
			OpenshiftResponse<UserInfo> response =
					new UserInfoResponseUnmarshaller(this).unmarshall(responseString);
			return response.getOpenshiftObject();
		} catch (MalformedURLException e) {
			throw new OpenshiftEndpointException(
					url, e, "Could not get user info for user \"{0}\" at \"{1}\"", username, url, e);
		} catch (HttpClientException e) {
			throw new OpenshiftEndpointException(
					url, e, "Could not get user info for user \"{0}\" at \"{1}\"", username, url, e);
		}
	}

	/**
	 * WARNING: the current server implementation returns invalid json.
	 * 
	 * @see ListCartridgesResponseUnmarshaller
	 * @see JsonSanitizer#sanitize(String)
	 */
	@Override
	public List<Cartridge> getCartridges() throws OpenshiftException {
		ListCartridgesRequest listCartridgesRequest = new ListCartridgesRequest(username, true);
		String url = listCartridgesRequest.getUrlString(BASE_URL);
		try {
			String listCartridgesRequestString =
					new ListCartridgesRequestJsonMarshaller().marshall(listCartridgesRequest);
			String request = new OpenshiftEnvelopeFactory(password, listCartridgesRequestString).createString();
			String listCatridgesReponse = createHttpClient(url).post(request);
			listCatridgesReponse = JsonSanitizer.sanitize(listCatridgesReponse);
			OpenshiftResponse<List<Cartridge>> response =
					new ListCartridgesResponseUnmarshaller().unmarshall(listCatridgesReponse);
			return response.getOpenshiftObject();
		} catch (MalformedURLException e) {
			throw new OpenshiftEndpointException(url, e, "Could not list available cartridges at \"{0}\"", url);
		} catch (HttpClientException e) {
			throw new OpenshiftEndpointException(url, e, "Could not list available cartridges at \"{0}\"", url);
		}
	}

	@Override
	public SSHKey createKey(String passPhrase, String privateKeyPath, String publicKeyPath) throws OpenshiftException {
		return SSHKey.create(passPhrase, privateKeyPath, publicKeyPath);
	}

	@Override
	public SSHKey loadKey(String privateKeyPath, String publicKeyPath) throws OpenshiftException {
		return SSHKey.load(privateKeyPath, publicKeyPath);
	}

	@Override
	public Domain createDomain(String name, SSHKey sshKey) throws OpenshiftException {
		return requestDomainAction(new CreateDomainRequest(name, sshKey, username, true));
	}

	@Override
	public Domain changeDomain(String newName, SSHKey sshKey) throws OpenshiftException {
		return requestDomainAction(new ChangeDomainRequest(newName, sshKey, username, true));
	}

	protected Domain requestDomainAction(AbstractDomainRequest request) throws OpenshiftException {
		String url = request.getUrlString(BASE_URL);
		try {
			String requestString =
					new OpenshiftEnvelopeFactory(
							password,
							new DomainRequestJsonMarshaller().marshall(request))
							.createString();
			String responseString = createHttpClient(url).post(requestString);
			responseString = JsonSanitizer.sanitize(responseString);
			OpenshiftResponse<Domain> response =
					new DomainResponseUnmarshaller(request.getName()).unmarshall(responseString);
			return response.getOpenshiftObject();
		} catch (MalformedURLException e) {
			throw new OpenshiftEndpointException(url, e, "Could not list available cartridges at \"{0}\"", url);
		} catch (HttpClientException e) {
			throw new OpenshiftEndpointException(url, e, "Could not list available cartridges at \"{0}\"", url);
		}
	}

	@Override
	public Application createApplication(String name, Cartridge cartridge) throws OpenshiftException {
		return requestApplicationAction(new ApplicationRequest(name, cartridge, ApplicationAction.CONFIGURE, username,
				true));
	}

	@Override
	public Application destroyApplication(String name, Cartridge cartridge) throws OpenshiftException {
		return requestApplicationAction(new ApplicationRequest(name, cartridge, ApplicationAction.DECONFIGURE,
				username, true));
	}

	@Override
	public Application startApplication(String name, Cartridge cartridge) throws OpenshiftException {
		return requestApplicationAction(new ApplicationRequest(name, cartridge, ApplicationAction.START, username, true));
	}

	@Override
	public Application restartApplication(String name, Cartridge cartridge) throws OpenshiftException {
		return requestApplicationAction(new ApplicationRequest(name, cartridge, ApplicationAction.RESTART, username,
				true));
	}

	@Override
	public Application stopApplication(String name, Cartridge cartridge) throws OpenshiftException {
		return requestApplicationAction(new ApplicationRequest(name, cartridge, ApplicationAction.STOP, username, true));
	}

	/**
	 * This seems not implemented yet on the server. The service simply returns
	 * a <code>null</code> data object. example response:
	 * <p>
	 * {"messages":"","debug":"","data":null,"api":"1.1.1","api_c":[
	 * "placeholder"
	 * ],"result":"Success","broker":"1.1.1","broker_c":["namespace"
	 * ,"rhlogin","ssh"
	 * ,"app_uuid","debug","alter","cartridge","cart_type","action"
	 * ,"app_name","api"],"exit_code":0}
	 */
	@Override
	public String getStatus(String applicationName, Cartridge cartridge) throws OpenshiftException {
		ApplicationRequest applicationRequest = 
				new ApplicationRequest(applicationName, cartridge, ApplicationAction.STATUS, username, true);
		String url = applicationRequest.getUrlString(BASE_URL);
		try {
			String applicationRequestString =
					new ApplicationRequestJsonMarshaller().marshall(applicationRequest);
			String request = new OpenshiftEnvelopeFactory(password, applicationRequestString).createString();
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
					"Could not {0} application \"{1}\" at \"{2}\": Invalid credentials user \"{3}\", password \"{4}\"",
					applicationRequest.getAction().toHumanReadable(), applicationRequest.getName(), url, username,
					password);
		} catch (HttpClientException e) {
			throw new OpenshiftEndpointException(
					url, e, "Could not {0} application \"{1}\" at \"{2}\"",
					applicationRequest.getAction().toHumanReadable(), applicationRequest.getName(), url);
		}
	}

	protected Application requestApplicationAction(ApplicationRequest applicationRequest) throws OpenshiftException {
		String url = applicationRequest.getUrlString(BASE_URL);
		try {
			String applicationRequestString =
					new ApplicationRequestJsonMarshaller().marshall(applicationRequest);
			String request = new OpenshiftEnvelopeFactory(password, applicationRequestString).createString();
			String response = createHttpClient(url).post(request);

			response = JsonSanitizer.sanitize(response);
			OpenshiftResponse<Application> openshiftResponse =
					new ApplicationResponseUnmarshaller(applicationRequest.getName(),
							applicationRequest.getCartridge(), this).unmarshall(response);
			return openshiftResponse.getOpenshiftObject();
		} catch (MalformedURLException e) {
			throw new OpenshiftException(
					e, "Could not {0} application \"{1}\" at \"{2}\": Invalid url \"{2}\"",
					applicationRequest.getAction().toHumanReadable(), applicationRequest.getName(), url);
		} catch (UnauthorizedException e) {
			throw new InvalidCredentialsOpenshiftException(
					url, e,
					"Could not {0} application \"{1}\" at \"{2}\": Invalid credentials user \"{3}\", password \"{4}\"",
					applicationRequest.getAction().toHumanReadable(), applicationRequest.getName(), url, username,
					password);
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
