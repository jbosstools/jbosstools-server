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
package org.jboss.ide.eclipse.as.openshift.internal.test.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.jboss.ide.eclipse.as.openshift.core.Domain;
import org.jboss.ide.eclipse.as.openshift.core.OpenshiftException;
import org.jboss.ide.eclipse.as.openshift.core.SSHKey;
import org.jboss.ide.eclipse.as.openshift.core.User;
import org.jboss.ide.eclipse.as.openshift.core.internal.marshalling.DomainRequestJsonMarshaller;
import org.jboss.ide.eclipse.as.openshift.core.internal.request.ChangeDomainRequest;
import org.jboss.ide.eclipse.as.openshift.core.internal.request.CreateDomainRequest;
import org.jboss.ide.eclipse.as.openshift.core.internal.request.OpenshiftJsonRequestFactory;
import org.jboss.ide.eclipse.as.openshift.core.internal.response.DomainResponseUnmarshaller;
import org.jboss.ide.eclipse.as.openshift.core.internal.response.JsonSanitizer;
import org.jboss.ide.eclipse.as.openshift.core.internal.response.OpenshiftResponse;
import org.jboss.ide.eclipse.as.openshift.internal.test.core.fakes.TestSSHKey;
import org.junit.Test;

/**
 * @author André Dietisheim
 */
public class DomainTest {

	private static final String USERNAME = "toolsjboss@gmail.com";
	private static final String PASSWORD = "1q2w3e";
	private static final String UUID = "0c82860dae904a4d87f8e5d87a5af840";

	@Test
	public void canMarshallDomainCreateRequest() throws IOException, OpenshiftException {
		SSHKey sshKey = TestSSHKey.create();
		String expectedRequestString = createDomainRequestString(PASSWORD, USERNAME, true, "myDomain", false,
				sshKey.getPublicKeyContent());

		CreateDomainRequest request = new CreateDomainRequest("myDomain", sshKey, USERNAME, true);
		String requestString =
				new OpenshiftJsonRequestFactory(
						PASSWORD,
						new DomainRequestJsonMarshaller().marshall(request))
						.create();
		assertEquals(expectedRequestString, requestString);
	}

	@Test
	public void canUnmarshallDomainCreateResponse() throws IOException, OpenshiftException {
		String domainName = "myDomain";
		String responseString = createDomainResponseString(USERNAME, UUID);

		responseString = JsonSanitizer.sanitize(responseString);
		OpenshiftResponse<Domain> response = new DomainResponseUnmarshaller(domainName).unmarshall(responseString);

		assertNotNull(response);
		Domain domain = response.getData();
		assertEquals(domainName, domain.getName());
		User user = domain.getUser();
		assertNotNull(user);
		assertEquals(USERNAME, user.getRhlogin());
		assertEquals(UUID, user.getUuid());
	}

	@Test
	public void canMarshallDomainAlterRequest() throws IOException, OpenshiftException {
		SSHKey sshKey = TestSSHKey.create();
		String expectedRequestString = createDomainRequestString(PASSWORD, USERNAME, true, "myDomain", true,
				sshKey.getPublicKeyContent());

		ChangeDomainRequest request = new ChangeDomainRequest("myDomain", sshKey, USERNAME, true);
		String requestString =
				new OpenshiftJsonRequestFactory(
						PASSWORD,
						new DomainRequestJsonMarshaller().marshall(request))
						.create();
		assertEquals(expectedRequestString, requestString);
	}

	private String createDomainRequestString(String password, String username, boolean debug, String namespace,
			boolean alter, String sshPublicKey) throws UnsupportedEncodingException {
		return "password="
				+ password
				+ "&json_data=%7B"
				+ "%22rhlogin%22+%3A+"
				+ "%22"
				+ URLEncoder.encode(username, "UTF-8")
				+ "%22"
				+ "%2C+%22debug%22+%3A+%22" + String.valueOf(debug) + "%22"
				+ "%2C+%22namespace%22+%3A+%22" + URLEncoder.encode(namespace, "UTF-8") + "%22"
				+ "%2C+%22alter%22+%3A+%22" + String.valueOf(alter) + "%22"
				+ "%2C+%22ssh%22+%3A+%22"
				+ URLEncoder.encode(sshPublicKey, "UTF-8")
				+ "%22"
				+ "%7D";
	}

	/**
	 * WARNING: the response this method returns matches the actual response
	 * from the openshift service (9-12-2011). It is not valid json since it quotes the
	 * nested json object
	 * <p>
	 * "data": "{\"rhlogin\": ...
	 */
	private String createDomainResponseString(String username, String uuid) {
		return "{\"messages\":\"\",\"debug\":\"\",\"data\":\""
				+ "{\\\"rhlogin\\\":\\\""
				+ username
				+ "\\\",\\\"uuid\\\":\\\""
				+ uuid
				+ "\\\"}"
				+ "\",\"api\":\"1.1.1\",\"api_c\":[\"placeholder\"],\"result\":null,\"broker\":\"1.1.1\",\"broker_c\":[\"namespace\",\"rhlogin\",\"ssh\",\"app_uuid\",\"debug\",\"alter\",\"cartridge\",\"cart_type\",\"action\",\"app_name\",\"api\"],\"exit_code\":0}";
	}
}
