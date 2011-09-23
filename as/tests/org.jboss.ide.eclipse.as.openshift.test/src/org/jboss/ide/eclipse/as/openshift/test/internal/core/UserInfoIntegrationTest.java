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

import org.jboss.ide.eclipse.as.openshift.core.ApplicationInfo;
import org.jboss.ide.eclipse.as.openshift.core.Cartridge;
import org.jboss.ide.eclipse.as.openshift.core.IOpenshiftService;
import org.jboss.ide.eclipse.as.openshift.core.ISSHPublicKey;
import org.jboss.ide.eclipse.as.openshift.core.OpenshiftException;
import org.jboss.ide.eclipse.as.openshift.core.User;
import org.jboss.ide.eclipse.as.openshift.core.UserInfo;
import org.jboss.ide.eclipse.as.openshift.core.internal.OpenshiftService;
import org.jboss.ide.eclipse.as.openshift.test.internal.core.utils.ApplicationInfoAsserts;
import org.junit.Before;
import org.junit.Test;

/**
 * @author André Dietisheim
 */
public class UserInfoIntegrationTest {

	private IOpenshiftService openshiftService;

	private static final String RHLOGIN = "toolsjboss@gmail.com";
	private static final String PASSWORD = "1q2w3e";

	@Before
	public void setUp() {
		this.openshiftService = new OpenshiftService();
	}

	@Test
	public void canGetUserInfo() throws Exception {
		UserInfo userInfo = openshiftService.getUserInfo(new User(RHLOGIN, PASSWORD));
		assertNotNull(userInfo);

		assertEquals(RHLOGIN, userInfo.getRhLogin());
	}

	@Test
	public void userInfoContainsOneMoreApplicationAfterCreatingNewApplication() throws Exception {
		User user = new User(RHLOGIN, PASSWORD);
		UserInfo userInfo = openshiftService.getUserInfo(user);
		assertNotNull(userInfo);

		List<ApplicationInfo> applicationInfos = userInfo.getApplicationInfos();
		assertNotNull(applicationInfos);
		int numberOfApplicationInfos = applicationInfos.size();

		String applicationName = createRandomName();
		try {
			openshiftService.createApplication(applicationName, Cartridge.JBOSSAS_7, user);

			UserInfo userInfo2 = openshiftService.getUserInfo(user);
			assertEquals(numberOfApplicationInfos + 1, userInfo2.getApplicationInfos().size());
			ApplicationInfoAsserts.assertThatContainsApplicationInfo(applicationName, userInfo2.getApplicationInfos());
		} finally {
			silentlyDestroyAS7Application(applicationName, openshiftService);
		}
	}

	@Test
	public void canUseReturnedSSHKeyToChangeDomain() throws Exception {
		User user = new User(RHLOGIN, PASSWORD);
		UserInfo userInfo = openshiftService.getUserInfo(user);
		assertNotNull(userInfo);

		ISSHPublicKey sshKey = userInfo.getSshPublicKey();
		openshiftService.changeDomain(createRandomName(), sshKey, user);
	}
	
	private String createRandomName() {
		return String.valueOf(System.currentTimeMillis());
	}

	private void silentlyDestroyAS7Application(String name, IOpenshiftService service) {
		try {
			service.destroyApplication(name, Cartridge.JBOSSAS_7, new User(RHLOGIN, PASSWORD));
		} catch (OpenshiftException e) {
			e.printStackTrace();
		}
	}
}
