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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.jboss.ide.eclipse.as.openshift.core.OpenshiftException;
import org.jboss.ide.eclipse.as.openshift.core.SSHKey;
import org.jboss.ide.eclipse.as.openshift.core.internal.marshalling.DomainRequestJsonMarshaller;
import org.jboss.ide.eclipse.as.openshift.internal.core.request.ChangeDomainRequest;
import org.jboss.ide.eclipse.as.openshift.internal.core.request.CreateDomainRequest;
import org.jboss.ide.eclipse.as.openshift.internal.core.request.OpenshiftJsonRequestFactory;
import org.jboss.ide.eclipse.as.openshift.internal.test.core.fakes.TestSSHKey;
import org.junit.Test;

/**
 * @author André Dietisheim
 */
public class DomainTest {

	private static final String USERNAME = "toolsjboss@gmail.com";
	private static final String PASSWORD = "1q2w3e";

	@Test
	public void canMarshallDomainCreateRequest() throws IOException, OpenshiftException {
		SSHKey sshKey = TestSSHKey.create();
		String expectedRequestString = createDomainRequestString(PASSWORD, USERNAME, true, "myDomain", false, sshKey.getPublicKeyContent());
		
		CreateDomainRequest request = new CreateDomainRequest("myDomain", sshKey, USERNAME, true);
		String requestString =
				new OpenshiftJsonRequestFactory(
						PASSWORD,
						new DomainRequestJsonMarshaller().marshall(request))
						.create();
		assertEquals(expectedRequestString, requestString);
	}
	
	@Test
	public void canMarshallDomainAlterRequest() throws IOException, OpenshiftException {
		SSHKey sshKey = TestSSHKey.create();
		String expectedRequestString = createDomainRequestString(PASSWORD, USERNAME, true, "myDomain", true, sshKey.getPublicKeyContent());
		
		ChangeDomainRequest request = new ChangeDomainRequest("myDomain", sshKey, USERNAME, true);
		String requestString =
				new OpenshiftJsonRequestFactory(
						PASSWORD,
						new DomainRequestJsonMarshaller().marshall(request))
						.create();
		assertEquals(expectedRequestString, requestString);
	}

	private String createDomainRequestString(String password, String username, boolean debug, String namespace, boolean alter, String sshPublicKey) throws UnsupportedEncodingException {
		String request = 
				"password="
				+ password
				+ "&json_data=%7B"
				+ "%22rhlogin%22+%3A+"
				+ "%22"
				+ URLEncoder.encode(username, "UTF-8")
				+ "%22"
				+ "%2C+%22debug%22+%3A+%22" + String.valueOf(debug)+ "%22"
				+ "%2C+%22namespace%22+%3A+%22" + URLEncoder.encode(namespace, "UTF-8")+ "%22"
				+ "%2C+%22alter%22+%3A+%22" + String.valueOf(alter)+ "%22"
				+ "%2C+%22ssh%22+%3A+%22"
				+ URLEncoder.encode(sshPublicKey, "UTF-8")
				+ "%22"
				+ "%7D";
				return request;
	}
}
