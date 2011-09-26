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

import static org.jboss.ide.eclipse.as.openshift.test.internal.core.utils.CartridgeAsserts.assertThatContainsCartridge;
import static org.junit.Assert.assertEquals;

import java.net.URLEncoder;
import java.util.List;

import org.jboss.ide.eclipse.as.openshift.core.Cartridge;
import org.jboss.ide.eclipse.as.openshift.core.OpenshiftException;
import org.jboss.ide.eclipse.as.openshift.core.internal.request.ListCartridgesRequest;
import org.jboss.ide.eclipse.as.openshift.core.internal.request.OpenshiftEnvelopeFactory;
import org.jboss.ide.eclipse.as.openshift.core.internal.request.marshalling.ListCartridgesRequestJsonMarshaller;
import org.jboss.ide.eclipse.as.openshift.core.internal.response.OpenshiftResponse;
import org.jboss.ide.eclipse.as.openshift.core.internal.response.unmarshalling.JsonSanitizer;
import org.jboss.ide.eclipse.as.openshift.core.internal.response.unmarshalling.ListCartridgesResponseUnmarshaller;
import org.junit.Test;

/**
 * @author André Dietisheim
 */
public class ListCartridgesTest {

	private static final String USERNAME = "toolsjboss@gmail.com";
	private static final String PASSWORD = "1q2w3e";

	@Test
	public void canMarshallListCartridgesRequest() throws Exception {
		String expectedRequestString = "password=" + PASSWORD + "&json_data=%7B%22rhlogin%22+%3A+%22"
				+ URLEncoder.encode(USERNAME, "UTF-8")
				+ "%22%2C+%22debug%22+%3A+%22true%22%2C+%22cart_type%22+%3A+%22standalone%22%7D";

		String listCartridgeRequest = new ListCartridgesRequestJsonMarshaller().marshall(
				new ListCartridgesRequest(USERNAME, true));
		String effectiveRequest = new OpenshiftEnvelopeFactory(PASSWORD, listCartridgeRequest).createString();

		assertEquals(expectedRequestString, effectiveRequest);
	}

	@Test
	public void canUnmarshallCartridgeListResponse() throws OpenshiftException {
		String cartridgeListResponse =
				"{"
						+ "\"messages\":\"\","
						+ "\"debug\":\"\","
						+ "\"data\":"
						+ "\"{\\\"carts\\\":[\\\"perl-5.10\\\",\\\"jbossas-7.0\\\",\\\"wsgi-3.2\\\",\\\"rack-1.1\\\",\\\"php-5.3\\\"]}\","
						+ "\"api\":\"1.1.1\","
						+ "\"api_c\":[\"placeholder\"],"
						+ "\"result\":null,"
						+ "\"broker\":\"1.1.1\","
						+ "\"broker_c\":["
						+ "\"namespace\","
						+ "\"rhlogin\","
						+ "\"ssh\","
						+ "\"app_uuid\","
						+ "\"debug\","
						+ "\"alter\","
						+ "\"cartridge\","
						+ "\"cart_type\","
						+ "\"action\","
						+ "\"app_name\","
						+ "\"api"
						+ "\"],"
						+ "\"exit_code\":0}";

		cartridgeListResponse = JsonSanitizer.sanitize(cartridgeListResponse);
		OpenshiftResponse<List<Cartridge>> response =
				new ListCartridgesResponseUnmarshaller().unmarshall(cartridgeListResponse);
		assertEquals("", response.getMessages());
		assertEquals(false, response.isDebug());

		List<Cartridge> cartridges = response.getOpenshiftObject();
		assertEquals(5, cartridges.size());
		assertThatContainsCartridge("perl-5.10", cartridges);
		assertThatContainsCartridge("jbossas-7.0", cartridges);
		assertThatContainsCartridge("wsgi-3.2", cartridges);
		assertThatContainsCartridge("rack-1.1", cartridges);
		assertThatContainsCartridge("php-5.3", cartridges);
		assertEquals(null, response.getResult());
		assertEquals(0, response.getExitCode());
	}

}
