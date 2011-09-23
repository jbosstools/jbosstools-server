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

import static org.jboss.ide.eclipse.as.openshift.test.internal.core.utils.ApplicationInfoAsserts.assertThatContainsApplicationInfo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.net.URLEncoder;
import java.util.List;

import org.jboss.ide.eclipse.as.openshift.core.ApplicationInfo;
import org.jboss.ide.eclipse.as.openshift.core.ISSHPublicKey;
import org.jboss.ide.eclipse.as.openshift.core.OpenshiftException;
import org.jboss.ide.eclipse.as.openshift.core.UserInfo;
import org.jboss.ide.eclipse.as.openshift.core.internal.request.OpenshiftEnvelopeFactory;
import org.jboss.ide.eclipse.as.openshift.core.internal.request.UserInfoRequest;
import org.jboss.ide.eclipse.as.openshift.core.internal.request.marshalling.UserInfoRequestJsonMarshaller;
import org.jboss.ide.eclipse.as.openshift.core.internal.response.JsonSanitizer;
import org.jboss.ide.eclipse.as.openshift.core.internal.response.OpenshiftResponse;
import org.jboss.ide.eclipse.as.openshift.core.internal.response.UserInfoResponseUnmarshaller;
import org.jboss.ide.eclipse.as.openshift.test.internal.core.fakes.UserInfoResponseFake;
import org.junit.Test;

/**
 * @author André Dietisheim
 */
public class UserInfoTest {

	@Test
	public void canMarshallUserInfoRequest() throws Exception {
		String expectedRequestString =
				"password=" + URLEncoder.encode(UserInfoResponseFake.PASSWORD, "UTF-8")
						+ "&json_data=%7B"
						+ "%22rhlogin%22+%3A+%22" + URLEncoder.encode(UserInfoResponseFake.RHLOGIN, "UTF-8")
						+ "%22%2C+"
						+ "%22debug%22+%3A+%22true%22"
						+ "%7D";

		String userInfoRequest = new UserInfoRequestJsonMarshaller().marshall(
				new UserInfoRequest(UserInfoResponseFake.RHLOGIN, true));
		String effectiveRequest =
				new OpenshiftEnvelopeFactory(UserInfoResponseFake.PASSWORD, userInfoRequest).createString();

		assertEquals(expectedRequestString, effectiveRequest);
	}

	@Test
	public void canUnmarshallUserInfoResponse() throws Exception {
		UserInfo userInfo = getUserInfo(JsonSanitizer.sanitize(UserInfoResponseFake.RESPONSE));
		assertNotNull(userInfo);

		assertEquals(UserInfoResponseFake.RHLOGIN, userInfo.getRhLogin());
		assertEquals(UserInfoResponseFake.UUID, userInfo.getUuid());
		ISSHPublicKey sshPublicKey = userInfo.getSshPublicKey();
		assertNotNull(sshPublicKey);
		assertEquals(UserInfoResponseFake.SSH_KEY, sshPublicKey.getPublicKey());

		assertEquals(UserInfoResponseFake.NAMESPACE, userInfo.getNamespace());
		assertEquals(UserInfoResponseFake.RHC_DOMAIN, userInfo.getRhcDomain());

		List<ApplicationInfo> applicationInfos = userInfo.getApplicationInfos();
		assertNotNull(applicationInfos);
		assertEquals(2, applicationInfos.size());
		assertThatContainsApplicationInfo(
				UserInfoResponseFake.APP1_NAME,
				UserInfoResponseFake.APP1_EMBEDDED,
				UserInfoResponseFake.APP1_UUID,
				UserInfoResponseFake.APP1_CARTRIDGE,
				UserInfoResponseFake.APP1_CREATION_TIME,
				applicationInfos);
		assertThatContainsApplicationInfo(
				UserInfoResponseFake.APP2_NAME,
				UserInfoResponseFake.APP2_EMBEDDED,
				UserInfoResponseFake.APP2_UUID,
				UserInfoResponseFake.APP2_CARTRIDGE,
				UserInfoResponseFake.APP2_CREATION_TIME,
				applicationInfos);
	}

	@Test
	public void canGetApplicationByName() throws OpenshiftException {
		UserInfo userInfo = getUserInfo(JsonSanitizer.sanitize(UserInfoResponseFake.RESPONSE));
		ApplicationInfo applicationInfo = userInfo.getApplicationInfoByName(UserInfoResponseFake.APP1_NAME);
		assertNotNull(applicationInfo);
		assertEquals(UserInfoResponseFake.APP1_NAME, applicationInfo.getName());
	}

	protected UserInfo getUserInfo(String response) throws OpenshiftException {
		UserInfoResponseUnmarshaller unmarshaller = new UserInfoResponseUnmarshaller();
		OpenshiftResponse<UserInfo> openshiftResponse = unmarshaller.unmarshall(response);
		return openshiftResponse.getOpenshiftObject();
	}
}
