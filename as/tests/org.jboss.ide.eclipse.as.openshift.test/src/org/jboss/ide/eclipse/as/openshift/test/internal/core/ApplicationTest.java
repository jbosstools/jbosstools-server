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

import org.jboss.ide.eclipse.as.openshift.core.Application;
import org.jboss.ide.eclipse.as.openshift.core.ApplicationLogReader;
import org.jboss.ide.eclipse.as.openshift.core.Cartridge;
import org.jboss.ide.eclipse.as.openshift.core.IOpenshiftService;
import org.jboss.ide.eclipse.as.openshift.core.OpenshiftException;
import org.jboss.ide.eclipse.as.openshift.core.internal.request.ApplicationAction;
import org.jboss.ide.eclipse.as.openshift.core.internal.request.ApplicationRequest;
import org.jboss.ide.eclipse.as.openshift.core.internal.request.OpenshiftEnvelopeFactory;
import org.jboss.ide.eclipse.as.openshift.core.internal.request.marshalling.ApplicationRequestJsonMarshaller;
import org.jboss.ide.eclipse.as.openshift.core.internal.response.ApplicationResponseUnmarshaller;
import org.jboss.ide.eclipse.as.openshift.core.internal.response.ApplicationStatusResponseUnmarshaller;
import org.jboss.ide.eclipse.as.openshift.core.internal.response.JsonSanitizer;
import org.jboss.ide.eclipse.as.openshift.core.internal.response.OpenshiftResponse;
import org.jboss.ide.eclipse.as.openshift.test.internal.core.fakes.NoopOpenshiftServiceFake;
import org.junit.Test;

/**
 * @author André Dietisheim
 */
public class ApplicationTest {

	private static final String USERNAME = "toolsjboss@gmail.com";
	private static final String PASSWORD = "1q2w3e";

	private static final String APPLICATION_NAME = "1316010645406";
	private static final Cartridge APPLICATION_CARTRIDGE = Cartridge.JBOSSAS_7;

	private static final String appResponse = 
			"{"
					+ "	\"messages\":\"\","
					+ "	\"debug\":\"Validating application limit toolsjboss@gmail.com: num of apps(0) must be < app limit(5)\n\","
					+ "	\"data\":{"
					+ "		\"health_check_path\":\"health\""
					+ "	},"
					+ "	\"api\":\"1.1.1\","
					+ "	\"api_c\":[\"placeholder\"],"
					+ "	\"result\":\"Successfully created application: "
					
					+ APPLICATION_NAME 
					
					+ "\","
					+ "	\"broker\":\"1.1.1\","
					+ "	\"broker_c\":[\"namespace\","
					+ "	\"rhlogin\","
					+ "	\"ssh\","
					+ "	\"app_uuid\","
					+ "	\"debug\","
					+ "	\"alter\","
					+ "	\"cartridge\","
					+ "	\"cart_type\","
					+ "	\"action\","
					+ "	\"app_name\","
					+ "	\"api\"],"
					+ "	\"exit_code\":0"
					+ "}";
	
	private static final String log =
			"10:30:38,700 INFO  [org.apache.catalina.core.AprLifecycleListener] (MSC service thread 1-1) "
					+ "The Apache Tomcat Native library which allows optimal performance in production environments was not found on the java.library.path:"
					+ "/usr/lib/jvm/java-1.6.0-openjdk-1.6.0.0.x86_64/jre/lib/amd64/server:/usr/lib/jvm/java-1.6.0-openjdk-1.6.0.0.x86_64/jre/lib/amd64:"
					+ "/usr/lib/jvm/java-1.6.0-openjdk-1.6.0.0.x86_64/jre/../lib/amd64:/usr/java/packages/lib/amd64:/usr/lib64:/lib64:/lib:/usr/lib\n"
					+ "10:30:38,792 INFO  [org.apache.coyote.http11.Http11Protocol] (MSC service thread 1-3) Starting Coyote HTTP/1.1 on http--127.1.7.1-8080\n"
					+ "10:30:38,836 INFO  [org.jboss.as.connector] (MSC service thread 1-4) Starting JCA Subsystem (JBoss IronJacamar 1.0.3.Final)\n"
					+ "10:30:38,892 INFO  [org.jboss.as.connector.subsystems.datasources] (MSC service thread 1-1) Bound data source [java:jboss/datasources/ExampleDS]\n"
					+ "10:30:39,293 INFO  [org.jboss.as.deployment] (MSC service thread 1-2) Started FileSystemDeploymentService for directory /var/lib/libra/664e4d4dbce74c69ac321053149546df/1316010645406/jbossas-7.0/standalone/deployments\n"
					+ "10:30:39,314 INFO  [org.jboss.as] (Controller Boot Thread) JBoss AS 7.0.1.Final \\\"Zap\\\" started in 2732ms - Started 82 of 107 services (22 services are passive or on-demand)\n"
					+ "10:30:39,339 INFO  [org.jboss.as.server.deployment] (MSC service thread 1-3) Starting deployment of \\\"ROOT.war\\\"\n"
					+ "10:30:39,424 INFO  [org.jboss.as.jpa] (MSC service thread 1-1) added javax.persistence.api dependency to ROOT.war\n"
					+ "10:30:39,700 INFO  [org.jboss.web] (MSC service thread 1-2) registering web context: \n"
					+ "10:30:39,742 INFO  [org.jboss.as.server.controller] (DeploymentScanner-threads - 2) Deployed \\\"ROOT.war\\\"\n";

	private static final String tail =
			"tailing /var/lib/libra/664e4d4dbce74c69ac321053149546df/"

					+ APPLICATION_NAME

					+ "//"

					+ APPLICATION_CARTRIDGE

					+ "/standalone/log/server.log\n"
					+ "------ Tail of 1316010645406 application server.log ------\n"

					+ log;

	private static final String statusResponse =
			"{\"messages\":\"\","
					+ "\"debug\":\"\","
					+ "\"data\":null,"
					+ "\"api\":\"1.1.1\","
					+ "\"api_c\":[\"placeholder\"],"
					+ "\"result\":\""

					+ tail

					+ "\","
					+ "\"broker\":\"1.1.1\","
					+ "\"broker_c\":[\"namespace\",\"rhlogin\",\"ssh\",\"app_uuid\",\"debug\",\"alter\",\"cartridge\",\"cart_type\",\"action\",\"app_name\",\"api\"],"
					+ "\"exit_code\":0}";

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
		String response = JsonSanitizer.sanitize(statusResponse);
		OpenshiftResponse<Application> openshiftResponse =
				new ApplicationResponseUnmarshaller(APPLICATION_NAME, APPLICATION_CARTRIDGE, new NoopOpenshiftServiceFake())
				.unmarshall(response);
		Application application = openshiftResponse.getOpenshiftObject();
		assertNotNull(application);
		assertEquals(APPLICATION_NAME, application.getName());
		assertEquals(APPLICATION_CARTRIDGE, application.getCartridge());
	}
	
	@Test
	public void canUnmarshallApplicationStatus() throws OpenshiftException {
		String response = JsonSanitizer.sanitize(statusResponse);
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
				return tail;
			}
		};

		Application application = new Application(APPLICATION_NAME, APPLICATION_CARTRIDGE, service);
		ApplicationLogReader reader = new ApplicationLogReader(application, service);

		int toMatchIndex = 0;
		for (int character = -1; (character = reader.read()) != -1;) {
			assertEquals(
					"character at position " + toMatchIndex
							+ " was '" + ((char) character) + "'"
							+ " but we expected '" + log.charAt(toMatchIndex) + "'.",
					log.charAt(toMatchIndex++), character);
		}
	}
}
