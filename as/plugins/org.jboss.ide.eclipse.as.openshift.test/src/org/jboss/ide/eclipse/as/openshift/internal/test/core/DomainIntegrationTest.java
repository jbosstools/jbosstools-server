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
package org.jboss.ide.eclipse.as.openshift.internal.test.core;

import static org.junit.Assert.assertNotNull;

import org.jboss.ide.eclipse.as.openshift.core.Domain;
import org.jboss.ide.eclipse.as.openshift.internal.core.OpenshiftService;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class DomainIntegrationTest {

	private OpenshiftService openshiftService;

	private static final String USERNAME = "toolsjboss@gmail.com";
	private static final String PASSWORD = "1q2w3e";

	@Before
	public void setUp() {
		this.openshiftService = new OpenshiftService(USERNAME, PASSWORD);
	}

	@Ignore
	@Test
	public void canCreateDomain() throws Exception {
		String name = createRandomDomainName();
		Domain domain = openshiftService.createDomain(name, (String) null);
		assertNotNull(domain);
	}

	private String createRandomDomainName() {
		return String.valueOf(System.currentTimeMillis());
	}
}
