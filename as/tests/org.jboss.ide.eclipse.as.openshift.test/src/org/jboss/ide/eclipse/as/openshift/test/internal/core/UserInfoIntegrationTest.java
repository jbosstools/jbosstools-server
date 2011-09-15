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

import static org.junit.Assert.assertNotNull;

import org.jboss.ide.eclipse.as.openshift.core.IOpenshiftService;
import org.jboss.ide.eclipse.as.openshift.core.UserInfo;
import org.jboss.ide.eclipse.as.openshift.core.internal.OpenshiftService;
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
	}
}
