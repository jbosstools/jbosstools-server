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
package org.jboss.ide.eclipse.as.openshift.test.internal.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Arrays;

import org.jboss.ide.eclipse.as.openshift.core.Application;
import org.jboss.ide.eclipse.as.openshift.core.ApplicationLogReader;
import org.jboss.ide.eclipse.as.openshift.core.Cartridge;
import org.jboss.ide.eclipse.as.openshift.core.Domain;
import org.jboss.ide.eclipse.as.openshift.core.IOpenshiftService;
import org.jboss.ide.eclipse.as.openshift.core.OpenshiftException;
import org.jboss.ide.eclipse.as.openshift.core.User;
import org.jboss.ide.eclipse.as.openshift.core.UserInfo;
import org.jboss.ide.eclipse.as.openshift.core.internal.request.ApplicationAction;
import org.jboss.ide.eclipse.as.openshift.core.internal.request.ApplicationRequest;
import org.jboss.ide.eclipse.as.openshift.core.internal.request.OpenshiftEnvelopeFactory;
import org.jboss.ide.eclipse.as.openshift.core.internal.request.marshalling.ApplicationRequestJsonMarshaller;
import org.jboss.ide.eclipse.as.openshift.core.internal.response.ApplicationResponseUnmarshaller;
import org.jboss.ide.eclipse.as.openshift.core.internal.response.ApplicationStatusResponseUnmarshaller;
import org.jboss.ide.eclipse.as.openshift.core.internal.response.JsonSanitizer;
import org.jboss.ide.eclipse.as.openshift.core.internal.response.OpenshiftResponse;
import org.jboss.ide.eclipse.as.openshift.test.internal.core.fakes.ApplicationResponseFake;
import org.jboss.ide.eclipse.as.openshift.test.internal.core.fakes.NoopOpenshiftServiceFake;
import org.junit.Test;

/**
 * @author André Dietisheim
 */
public class ApplicationTest {

	private static final String USERNAME = "toolsjboss@gmail.com";
	private static final String PASSWORD = "1q2w3e";

	@Test
	public void canMarshallApplicationCreateRequest() throws Exception {
		String expectedRequestString =
				"password="
						+ PASSWORD
						+ "&json_data=%7B"
						+ "%22rhlogin%22+%3A+%22"
						+ URLEncoder.encode(USERNAME, "UTF-8")
						+ "%22"
						+ "%2C+%22debug%22+%3A+%22true%22"
						+ "%2C+%22cartridge%22+%3A+%22jbossas-7.0%22"
						+ "%2C+%22action%22+%3A+%22configure%22"
						+ "%2C+%22app_name%22+%3A+%22test-application%22"
						+ "%7D";

		String createApplicationRequest = new ApplicationRequestJsonMarshaller().marshall(
				new ApplicationRequest(
						"test-application", Cartridge.JBOSSAS_7, ApplicationAction.CONFIGURE, USERNAME, true));
		String effectiveRequest = new OpenshiftEnvelopeFactory(PASSWORD, createApplicationRequest).createString();

		assertEquals(expectedRequestString, effectiveRequest);
	}

	@Test
	public void canMarshallApplicationDestroyRequest() throws Exception {
		String expectedRequestString =
				"password="
						+ PASSWORD
						+ "&json_data=%7B"
						+ "%22rhlogin%22+%3A+"
						+ "%22" + URLEncoder.encode(USERNAME, "UTF-8") + "%22"
						+ "%2C+%22debug%22+%3A+%22true%22"
						+ "%2C+%22cartridge%22+%3A+%22jbossas-7.0%22"
						+ "%2C+%22action%22+%3A+%22deconfigure%22"
						+ "%2C+%22app_name%22+%3A+%22test-application%22"
						+ "%7D";

		String createApplicationRequest = new ApplicationRequestJsonMarshaller().marshall(
				new ApplicationRequest(
						"test-application", Cartridge.JBOSSAS_7, ApplicationAction.DECONFIGURE, USERNAME, true));
		String effectiveRequest = new OpenshiftEnvelopeFactory(PASSWORD, createApplicationRequest).createString();

		assertEquals(expectedRequestString, effectiveRequest);
	}

	@Test
	public void canUnmarshallApplicationResponse() throws OpenshiftException {
		String response = JsonSanitizer.sanitize(ApplicationResponseFake.appResponse);
		OpenshiftResponse<Application> openshiftResponse =
				new ApplicationResponseUnmarshaller(
						ApplicationResponseFake.APPLICATION_NAME, ApplicationResponseFake.APPLICATION_CARTRIDGE,
						new NoopOpenshiftServiceFake())
						.unmarshall(response);
		Application application = openshiftResponse.getOpenshiftObject();
		assertNotNull(application);
		assertEquals(ApplicationResponseFake.APPLICATION_NAME, application.getName());
		assertEquals(ApplicationResponseFake.APPLICATION_CARTRIDGE, application.getCartridge());
	}

	@Test
	public void canGetGitUri() throws OpenshiftException {
		String response = JsonSanitizer.sanitize(ApplicationResponseFake.appResponse);
		IOpenshiftService service = new NoopOpenshiftServiceFake() {
			@Override
			public UserInfo getUserInfo() throws OpenshiftException {
				Domain domain =
						new Domain("adietish", "openshift.redhat.com");
				User user = new User(
						ApplicationResponseFake.USERNAME,
						"1234567890abcdef",
						null,
						domain);
				Application application = new Application(
						ApplicationResponseFake.APPLICATION_NAME,
						ApplicationResponseFake.APPLICATION_UUID,
						ApplicationResponseFake.APPLICATION_CARTRIDGE,
						ApplicationResponseFake.APPLICATION_EMBEDDED,
						ApplicationResponseFake.APPLICATION_CREATIONTIME,
						user,
						this);
				return new UserInfo(user, Arrays.asList(new Application[] { application }));
			}
		};
		OpenshiftResponse<Application> openshiftResponse =
				new ApplicationResponseUnmarshaller(
						ApplicationResponseFake.APPLICATION_NAME, ApplicationResponseFake.APPLICATION_CARTRIDGE,
						service)
						.unmarshall(response);
		Application application = openshiftResponse.getOpenshiftObject();
		assertNotNull(application);
		String gitUri = application.getGitUri();
		assertNotNull(gitUri);
	}

	@Test
	public void canUnmarshallApplicationStatus() throws OpenshiftException {
		String response = JsonSanitizer.sanitize(ApplicationResponseFake.statusResponse);
		OpenshiftResponse<String> openshiftResponse =
				new ApplicationStatusResponseUnmarshaller().unmarshall(response);
		String status = openshiftResponse.getOpenshiftObject();
		assertNotNull(status);
		assertTrue(status.startsWith("tailing "));
	}

	@Test
	public void applicationLogReaderReturnsAllowsToReadFromStatus() throws IOException {

		IOpenshiftService service = new NoopOpenshiftServiceFake() {
			@Override
			public String getStatus(String applicationName, Cartridge cartridge) throws OpenshiftException {
				return ApplicationResponseFake.tail;
			}
		};

		Application application =
				new Application(ApplicationResponseFake.APPLICATION_NAME,
						ApplicationResponseFake.APPLICATION_CARTRIDGE, service);
		ApplicationLogReader reader = new ApplicationLogReader(application, service);

		int toMatchIndex = 0;
		for (int character = -1; (character = reader.read()) != -1;) {
			assertEquals(
					"character at position " + toMatchIndex
							+ " was '" + ((char) character) + "'"
							+ " but we expected '" + ApplicationResponseFake.log.charAt(toMatchIndex) + "'.",
					ApplicationResponseFake.log.charAt(toMatchIndex++), character);
		}
	}
}
