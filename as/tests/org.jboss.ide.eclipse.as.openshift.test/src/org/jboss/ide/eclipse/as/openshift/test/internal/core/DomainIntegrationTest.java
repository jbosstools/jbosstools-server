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

import org.jboss.ide.eclipse.as.openshift.core.Domain;
import org.jboss.ide.eclipse.as.openshift.core.SSHKey;
import org.jboss.ide.eclipse.as.openshift.core.internal.OpenshiftService;
import org.jboss.ide.eclipse.as.openshift.test.internal.core.fakes.TestSSHKey;
import org.junit.Before;
import org.junit.Test;

public class DomainIntegrationTest {

	private OpenshiftService openshiftService;

	private static final String USERNAME = "toolsjboss@gmail.com";
	private static final String PASSWORD = "1q2w3e";

	@Before
	public void setUp() {
		this.openshiftService = new OpenshiftService(USERNAME, PASSWORD);
	}

	@Test
	public void canCreateDomain() throws Exception {

		String domainName = createRandomString();
		SSHKey sshKey = TestSSHKey.create();
		Domain domain = openshiftService.createDomain(domainName, sshKey);

		assertNotNull(domain);
		assertEquals(domainName, domain.getNamespace());
	}

	@Test
	public void canChangeDomain() throws Exception {

		String domainName = createRandomString();
		SSHKey sshKey = TestSSHKey.create();
		Domain domain = openshiftService.changeDomain(domainName, sshKey);

		assertNotNull(domain);
		assertEquals(domainName, domain.getNamespace());
	}

	private String createRandomString() {
		return String.valueOf(System.currentTimeMillis());
	}
}
