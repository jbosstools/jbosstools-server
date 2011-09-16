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

import java.util.List;

import org.jboss.ide.eclipse.as.openshift.core.Application;
import org.jboss.ide.eclipse.as.openshift.core.Cartridge;
import org.jboss.ide.eclipse.as.openshift.core.IOpenshiftService;
import org.jboss.ide.eclipse.as.openshift.core.ISSHPublicKey;
import org.jboss.ide.eclipse.as.openshift.core.OpenshiftException;
import org.jboss.ide.eclipse.as.openshift.core.User;
import org.jboss.ide.eclipse.as.openshift.core.UserInfo;
import org.jboss.ide.eclipse.as.openshift.core.internal.OpenshiftService;
import org.jboss.ide.eclipse.as.openshift.test.internal.core.utils.ApplicationAsserts;
import org.junit.Before;
import org.junit.Test;

/**
 * @author André Dietisheim
 */
public class UserInfoIntegrationTest {

	private IOpenshiftService openshiftService;

	private static final String USERNAME = "toolsjboss@gmail.com";
	private static final String PASSWORD = "1q2w3e";

	@Before
	public void setUp() {
		this.openshiftService = new OpenshiftService(USERNAME, PASSWORD);
	}

	@Test
	public void canGetUserInfo() throws Exception {
		UserInfo userInfo = openshiftService.getUserInfo();
		assertNotNull(userInfo);

		User user = userInfo.getUser();
		assertEquals(USERNAME, user.getRhlogin());
	}

	@Test
	public void userInfoContainsOneMoreApplicationAfterCreatingNewApplication() throws Exception {
		UserInfo userInfo = openshiftService.getUserInfo();
		assertNotNull(userInfo);

		List<Application> applications = userInfo.getApplications();
		assertNotNull(applications);
		int numberOfApplications = applications.size();

		String applicationName = createRandomName();
		try {
			openshiftService.createApplication(applicationName, Cartridge.JBOSSAS_7);

			UserInfo userInfo2 = openshiftService.getUserInfo();
			assertEquals(numberOfApplications + 1, userInfo2.getApplications().size());
			ApplicationAsserts.assertThatContainsApplication(applicationName, userInfo2.getApplications());
		} finally {
			silentlyDestroyAS7Application(applicationName, openshiftService);
		}
	}

	@Test
	public void canUseReturnedSSHKeyToChangeDomain() throws Exception {
		UserInfo userInfo = openshiftService.getUserInfo();
		assertNotNull(userInfo);

		User user = userInfo.getUser();
		assertNotNull(user);
		ISSHPublicKey sshKey = user.getSshKey();
		openshiftService.changeDomain(createRandomName(), sshKey);
	}
	
	private String createRandomName() {
		return String.valueOf(System.currentTimeMillis());
	}

	private void silentlyDestroyAS7Application(String name, IOpenshiftService service) {
		try {
			service.destroyApplication(name, Cartridge.JBOSSAS_7);
		} catch (OpenshiftException e) {
			e.printStackTrace();
		}
	}

}
