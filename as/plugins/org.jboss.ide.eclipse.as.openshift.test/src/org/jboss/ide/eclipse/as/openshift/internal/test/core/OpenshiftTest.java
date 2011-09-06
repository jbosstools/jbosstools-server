package org.jboss.ide.eclipse.as.openshift.internal.test.core;

import static org.junit.Assert.assertEquals;

import java.net.URLEncoder;

import org.jboss.ide.eclipse.as.openshift.core.internal.marshalling.ApplicationRequestJsonMarshaller;
import org.jboss.ide.eclipse.as.openshift.core.internal.marshalling.ListCartridgesRequestJsonMarshaller;
import org.jboss.ide.eclipse.as.openshift.core.internal.marshalling.UserInfoRequestJsonMarshaller;
import org.jboss.ide.eclipse.as.openshift.internal.core.Cartridge;
import org.jboss.ide.eclipse.as.openshift.internal.core.request.ApplicationAction;
import org.jboss.ide.eclipse.as.openshift.internal.core.request.ApplicationRequest;
import org.jboss.ide.eclipse.as.openshift.internal.core.request.ListCartridgesRequest;
import org.jboss.ide.eclipse.as.openshift.internal.core.request.OpenshiftJsonRequestFactory;
import org.jboss.ide.eclipse.as.openshift.internal.core.request.UserInfoRequest;
import org.junit.Test;

public class OpenshiftTest {

	private static final String USERNAME = "toolsjboss@gmail.com";
	private static final String PASSWORD = "1q2w3e";

	@Test
	public void canMarshallUserInfoRequest() throws Exception {
		String expectedRequestString =
				"password=" + PASSWORD
						+ "&json_data=%7B"
						+ "%22rhlogin%22+%3A+%22" + URLEncoder.encode(USERNAME, "UTF-8") + "%22%2C+"
						+ "%22debug%22+%3A+%22true%22"
						+ "%7D";

		String userInfoRequest = new UserInfoRequestJsonMarshaller().marshall(new UserInfoRequest(USERNAME, true));
		String effectiveRequest = new OpenshiftJsonRequestFactory(PASSWORD, userInfoRequest).create();

		assertEquals(expectedRequestString, effectiveRequest);
	}

	@Test
	public void canMarshallListCartridgesRequest() throws Exception {
		String expectedRequestString = "password=" + PASSWORD + "&json_data=%7B%22rhlogin%22+%3A+%22"
				+ URLEncoder.encode(USERNAME, "UTF-8")
				+ "%22%2C+%22debug%22+%3A+%22true%22%2C+%22cart_type%22+%3A+%22standalone%22%7D";

		String listCartridgeRequest = new ListCartridgesRequestJsonMarshaller().marshall(
				new ListCartridgesRequest(USERNAME, true));
		String effectiveRequest = new OpenshiftJsonRequestFactory(PASSWORD, listCartridgeRequest).create();

		assertEquals(expectedRequestString, effectiveRequest);
	}

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
						"test-application", new Cartridge("jbossas-7.0"), ApplicationAction.CONFIGURE, USERNAME, true));
		String effectiveRequest = new OpenshiftJsonRequestFactory(PASSWORD, createApplicationRequest).create();

		assertEquals(expectedRequestString, effectiveRequest);
	}
}
