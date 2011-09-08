package org.jboss.ide.eclipse.as.openshift.internal.test.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.List;

import org.jboss.ide.eclipse.as.openshift.core.Cartridge;
import org.jboss.ide.eclipse.as.openshift.core.OpenshiftException;
import org.jboss.ide.eclipse.as.openshift.core.internal.marshalling.ApplicationRequestJsonMarshaller;
import org.jboss.ide.eclipse.as.openshift.core.internal.marshalling.ListCartridgesRequestJsonMarshaller;
import org.jboss.ide.eclipse.as.openshift.core.internal.marshalling.UserInfoRequestJsonMarshaller;
import org.jboss.ide.eclipse.as.openshift.internal.core.request.ApplicationAction;
import org.jboss.ide.eclipse.as.openshift.internal.core.request.ApplicationRequest;
import org.jboss.ide.eclipse.as.openshift.internal.core.request.ListCartridgesRequest;
import org.jboss.ide.eclipse.as.openshift.internal.core.request.OpenshiftJsonRequestFactory;
import org.jboss.ide.eclipse.as.openshift.internal.core.request.UserInfoRequest;
import org.jboss.ide.eclipse.as.openshift.internal.core.response.ListCartridgesResponseUnmarshaller;
import org.jboss.ide.eclipse.as.openshift.internal.core.response.OpenshiftResponse;
import org.junit.Test;

public class OpenshiftServiceTest {

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
						"test-application", Cartridge.JBOSSAS_7, ApplicationAction.CONFIGURE, USERNAME, true));
		String effectiveRequest = new OpenshiftJsonRequestFactory(PASSWORD, createApplicationRequest).create();

		assertEquals(expectedRequestString, effectiveRequest);
	}

	@Test
	public void canMarshallApplicationDestroyRequest() throws Exception {
		String expectedRequestString =
				"password="
						+ PASSWORD
						+ "&json_data=%7B"
						+ "%22rhlogin%22+%3A+%22"
						+ URLEncoder.encode(USERNAME, "UTF-8")
						+ "%22"
						+ "%2C+%22debug%22+%3A+%22true%22"
						+ "%2C+%22cartridge%22+%3A+%22jbossas-7.0%22"
						+ "%2C+%22action%22+%3A+%22deconfigure%22"
						+ "%2C+%22app_name%22+%3A+%22test-application%22"
						+ "%7D";

		String createApplicationRequest = new ApplicationRequestJsonMarshaller().marshall(
				new ApplicationRequest(
						"test-application", Cartridge.JBOSSAS_7, ApplicationAction.DECONFIGURE, USERNAME, true));
		String effectiveRequest = new OpenshiftJsonRequestFactory(PASSWORD, createApplicationRequest).create();

		assertEquals(expectedRequestString, effectiveRequest);
	}
	
	@Test
	public void canUnmarshallCartridgeListResponse() throws OpenshiftException {
		String cartridgeListResponse =
				"{"
						+ "\"messages\":\"\","
						+ "\"debug\":\"\","
						+ "\"data\":"
						+ "{\"carts\":[\"perl-5.10\",\"jbossas-7.0\",\"wsgi-3.2\",\"rack-1.1\",\"php-5.3\"]},"
						+ "\"api\":\"1.1.1\","
						+ "\"api_c\":[\"placeholder\"],"
						+ "\"result\":null,"
						+ "\"broker\":\"1.1.1\","
						+ "\"broker_c\":[\"namespace\",\"rhlogin\",\"ssh\",\"app_uuid\",\"debug\",\"alter\",\"cartridge\",\"cart_type\",\"action\",\"app_name\",\"api\"],"
						+ "\"exit_code\":0}";

		OpenshiftResponse<List<Cartridge>> response = new ListCartridgesResponseUnmarshaller(cartridgeListResponse)
				.unmarshall();
		assertEquals("", response.getMessages());
		assertEquals(false, response.isDebug());

		List<Cartridge> cartridges = response.getData();
		assertEquals(5, cartridges.size());
		assertThatContainsCartridge("perl-5.10", cartridges);
		assertThatContainsCartridge("jbossas-7.0", cartridges);
		assertThatContainsCartridge("wsgi-3.2", cartridges);
		assertThatContainsCartridge("rack-1.1", cartridges);
		assertThatContainsCartridge("php-5.3", cartridges);
		assertEquals(null, response.getResult());
		assertEquals(0, response.getExitCode());

	}

	private void assertThatContainsCartridge(String cartridgeName, List<Cartridge> cartridges) {
		boolean found = false;
		for (Cartridge cartridge : cartridges) {
			if (cartridgeName.equals(cartridge.getName())) {
				found = true;
				break;
			}
		}
		if (!found) {
			fail(MessageFormat.format("Could not find cartridge with name \"{0}\"", cartridgeName));
		}
	}
}
