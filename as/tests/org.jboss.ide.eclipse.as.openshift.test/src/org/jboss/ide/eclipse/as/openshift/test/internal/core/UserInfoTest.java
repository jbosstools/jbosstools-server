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

import java.net.URLEncoder;

import org.jboss.ide.eclipse.as.openshift.core.internal.request.OpenshiftEnvelopeFactory;
import org.jboss.ide.eclipse.as.openshift.core.internal.request.UserInfoRequest;
import org.jboss.ide.eclipse.as.openshift.core.internal.request.marshalling.UserInfoRequestJsonMarshaller;
import org.junit.Test;

/**
 * @author André Dietisheim
 */
public class UserInfoTest {

	private static final String USERNAME = "toolsjboss@gmail.com";
	private static final String PASSWORD = "1q2w3e";

	private static final String userInfoRespose =
			"{"
					+ "	\"messages\":\"\","
					+ " 	\"debug\":\"\","
					+ "	\"data\":\""
					+ ""
					+ "	{"
					+ "		\\\"user_info\\\":"
					+ "		{"
					+ "			\\\"rhc_domain\\\":\\\"rhcloud.com\\\","
					+ "			\\\"rhlogin\\\":\\\"toolsjboss@gmail.com\\\","
					+ "			\\\"namespace\\\":\\\"1315839296868\\\","
					+ "			\\\"uuid\\\":\\\"5f34b742db754cc9ab70fd1db2c9a2bd\\\","
					+ "			\\\"ssh_key\\\":\\\"AAAAB3NzaC1yc2EAAAADAQABAAAAgQC6BGRDydfGsQHhnZgo43dEfLzSJBke/hE8MLBBG1+5ZwktsrE+f2VdVt0McRLVAO6rdJRyMUX0rTbm7SABRVSX+zeQjlfqbbUtYFc7TIfd4RQc3GaISG1rS3C4svRSjdWaG36vDY2KxowdFvpKj8i8IYNPlLoRA/7EzzyneS6iyw==\\\""
					+ "		},"
					+ "		\\\"app_info\\\":"
					+ "		{"
					+ "			\\\"1315836963263\\\":"
					+ "			{"
					+ "				\\\"embedded\\\":null,"
					+ "				\\\"uuid\\\":\\\"810540bafc1c4b5e8cac830fb8ca786f\\\","
					+ "				\\\"framework\\\":\\\"jbossas-7.0\\\","
					+ "				\\\"creation_time\\\":\\\"2011-09-12T10:15:48-04:00\\\""
					+ "			},"
					+ "			\\\"1315903559289\\\":"
					+ "			{"
					+ "				\\\"embedded\\\":null,"
					+ "				\\\"uuid\\\":\\\"f5496311f43b42cd8fa5db5ecf83a352\\\","
					+ "				\\\"framework\\\":\\\"jbossas-7.0\\\","
					+ "				\\\"creation_time\\\":\\\"2011-09-13T04:45:44-04:00\\\""
					+ "			}"
					+ "		}"
					+ "	}\","
					+ "	\"api\":\"1.1.1\","
					+ "	\"api_c\":[\"placeholder\"],"
					+ "	\"result\":null,"
					+ "	\"broker\":\"1.1.1\","
					+ "	\"broker_c\":["
					+ "		\"namespace\","
					+ "		\"rhlogin\","
					+ "		\"ssh\","
					+ "		\"app_uuid\","
					+ "		\"debug\","
					+ "		\"alter\","
					+ "		\"cartridge\","
					+ "		\"cart_type\","
					+ "		\"action\","
					+ "		\"app_name\","
					+ "		\"api\""
					+ "		],"
					+ "	\"exit_code\":0"
					+ "}";
	@Test
	public void canMarshallUserInfoRequest() throws Exception {
		String expectedRequestString =
				"password=" + PASSWORD
						+ "&json_data=%7B"
						+ "%22rhlogin%22+%3A+%22" + URLEncoder.encode(USERNAME, "UTF-8") + "%22%2C+"
						+ "%22debug%22+%3A+%22true%22"
						+ "%7D";

		String userInfoRequest = new UserInfoRequestJsonMarshaller().marshall(new UserInfoRequest(USERNAME, true));
		String effectiveRequest = new OpenshiftEnvelopeFactory(PASSWORD, userInfoRequest).createString();

		assertEquals(expectedRequestString, effectiveRequest);
	}

	@Test
	public void canUnmarshallUserInfoResponse() throws Exception {
		
	}

}
