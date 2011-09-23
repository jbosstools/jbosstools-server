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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jboss.ide.eclipse.as.openshift.core.Application;
import org.jboss.ide.eclipse.as.openshift.core.ApplicationInfo;
import org.jboss.ide.eclipse.as.openshift.core.ApplicationLogReader;
import org.jboss.ide.eclipse.as.openshift.core.Cartridge;
import org.jboss.ide.eclipse.as.openshift.core.Domain;
import org.jboss.ide.eclipse.as.openshift.core.IOpenshiftService;
import org.jboss.ide.eclipse.as.openshift.core.OpenshiftException;
import org.jboss.ide.eclipse.as.openshift.core.User;
import org.jboss.ide.eclipse.as.openshift.core.UserInfo;
import org.jboss.ide.eclipse.as.openshift.core.internal.OpenshiftService;
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

	public static final Pattern GIT_URI_REGEXP = Pattern.compile("ssh://(.+)@(.+)-([^\\.]+)\\.(.+)/~/git/(.+).git/");
	
	private User user = new User(ApplicationResponseFake.RHLOGIN, ApplicationResponseFake.PASSWORD, new NoopOpenshiftServiceFake());

	@Test
	public void canMarshallApplicationCreateRequest() throws Exception {
		String expectedRequestString =
				"password="
						+ URLEncoder.encode(ApplicationResponseFake.PASSWORD, "UTF-8")
						+ "&json_data=%7B"
						+ "%22rhlogin%22+%3A+%22"
						+ URLEncoder.encode(ApplicationResponseFake.RHLOGIN, "UTF-8")
						+ "%22"
						+ "%2C+%22debug%22+%3A+%22true%22"
						+ "%2C+%22cartridge%22+%3A+%22jbossas-7.0%22"
						+ "%2C+%22action%22+%3A+%22"
						+ "configure"
						+ "%22%2C+%22app_name%22+%3A+%22test-application%22"
						+ "%7D";

		String createApplicationRequest = new ApplicationRequestJsonMarshaller().marshall(
				new ApplicationRequest(
						"test-application", Cartridge.JBOSSAS_7, ApplicationAction.CONFIGURE, ApplicationResponseFake.RHLOGIN, true));
		String effectiveRequest = new OpenshiftEnvelopeFactory(ApplicationResponseFake.PASSWORD, createApplicationRequest).createString();

		assertEquals(expectedRequestString, effectiveRequest);
	}

	@Test
	public void canMarshallApplicationDestroyRequest() throws Exception {
		String expectedRequestString =
				"password="
						+ URLEncoder.encode(ApplicationResponseFake.PASSWORD, "UTF-8")
						+ "&json_data=%7B"
						+ "%22rhlogin%22+%3A+"
						+ "%22" + URLEncoder.encode(ApplicationResponseFake.RHLOGIN, "UTF-8") + "%22"
						+ "%2C+%22debug%22+%3A+%22true%22"
						+ "%2C+%22cartridge%22+%3A+%22jbossas-7.0%22"
						+ "%2C+%22action%22+%3A+%22"
						+ "deconfigure"
						+ "%22%2C+%22app_name%22+%3A+%22test-application%22"
						+ "%7D";

		String createApplicationRequest = new ApplicationRequestJsonMarshaller().marshall(
				new ApplicationRequest(
						"test-application", Cartridge.JBOSSAS_7, ApplicationAction.DECONFIGURE, ApplicationResponseFake.RHLOGIN, true));
		String effectiveRequest = new OpenshiftEnvelopeFactory(ApplicationResponseFake.PASSWORD, createApplicationRequest).createString();

		assertEquals(expectedRequestString, effectiveRequest);
	}

	@Test
	public void canUnmarshallApplicationResponse() throws OpenshiftException {
		String response = JsonSanitizer.sanitize(ApplicationResponseFake.appResponse);
		OpenshiftResponse<Application> openshiftResponse =
				new ApplicationResponseUnmarshaller(
						ApplicationResponseFake.APPLICATION_NAME, ApplicationResponseFake.APPLICATION_CARTRIDGE,
						user, new NoopOpenshiftServiceFake())
						.unmarshall(response);
		Application application = openshiftResponse.getOpenshiftObject();
		assertNotNull(application);
		assertEquals(ApplicationResponseFake.APPLICATION_NAME, application.getName());
		assertEquals(ApplicationResponseFake.APPLICATION_CARTRIDGE, application.getCartridge());
	}

	@Test
	public void canGetGitUri() throws OpenshiftException {
		OpenshiftService userInfoService = new NoopOpenshiftServiceFake() {
			@Override
			public UserInfo getUserInfo(User user) throws OpenshiftException {
				ApplicationInfo applicationInfo = new ApplicationInfo(
						ApplicationResponseFake.APPLICATION_NAME,
						ApplicationResponseFake.APPLICATION_UUID,
						ApplicationResponseFake.APPLICATION_EMBEDDED,
						ApplicationResponseFake.APPLICATION_CARTRIDGE,
						ApplicationResponseFake.APPLICATION_CREATIONTIME);
				return new UserInfo(
						ApplicationResponseFake.RHLOGIN, 
						ApplicationResponseFake.UUID,
						ApplicationResponseFake.SSHPUBLICKEY,
						ApplicationResponseFake.RHC_DOMAIN,
						ApplicationResponseFake.NAMESPACE,
						Arrays.asList(new ApplicationInfo[] { applicationInfo }));
			}
		};
		User user = new User(ApplicationResponseFake.RHLOGIN, ApplicationResponseFake.PASSWORD, userInfoService);

		String response = JsonSanitizer.sanitize(ApplicationResponseFake.appResponse);
		OpenshiftResponse<Application> openshiftResponse =
				new ApplicationResponseUnmarshaller(
						ApplicationResponseFake.APPLICATION_NAME, ApplicationResponseFake.APPLICATION_CARTRIDGE,
						user, userInfoService)
						.unmarshall(response);
		Application application = openshiftResponse.getOpenshiftObject();
		assertNotNull(application);
		String gitUri = application.getGitUri();
		assertNotNull(gitUri);
		Domain domain = user.getDomain();
		assertNotNull(domain);
		assertGitUri(gitUri, application.getUUID(), application.getName(), domain.getNamespace(), domain.getRhcDomain());
	}

	private void assertGitUri(String gitUri, String uuid, String name, String namespace, String rhcDomain) {
		Matcher matcher = GIT_URI_REGEXP.matcher(gitUri);
		assertTrue(matcher.matches());
		assertEquals(5, matcher.groupCount());
		assertEquals(uuid, matcher.group(1));
		assertEquals(name, matcher.group(2));
		assertEquals(namespace, matcher.group(3));
		assertEquals(rhcDomain, matcher.group(4));
		assertEquals(name, matcher.group(5));
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
			public String getStatus(String applicationName, Cartridge cartridge, User user) throws OpenshiftException {
				return ApplicationResponseFake.tail;
			}
		};

		Application application =
				new Application(ApplicationResponseFake.APPLICATION_NAME,
						ApplicationResponseFake.APPLICATION_CARTRIDGE, user, service);
		ApplicationLogReader reader = new ApplicationLogReader(application, user, service);

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
