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

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

import org.jboss.ide.eclipse.as.openshift.core.ICartridge;
import org.jboss.ide.eclipse.as.openshift.core.internal.Application;
import org.jboss.ide.eclipse.as.openshift.core.internal.IOpenshiftService;
import org.jboss.ide.eclipse.as.openshift.core.internal.OpenshiftService;
import org.jboss.ide.eclipse.as.openshift.core.internal.User;
import org.jboss.ide.eclipse.as.openshift.test.internal.core.fakes.TestUser;
import org.jboss.ide.eclipse.as.openshift.test.internal.core.utils.ApplicationUtils;
import org.junit.Before;
import org.junit.Test;

/**
 * @author André Dietisheim
 */
public class ApplicationLogReaderIntegrationTest {

	private IOpenshiftService service;

	private User user;

	@Before
	public void setUp() {
		this.service = new OpenshiftService();
		this.user = new TestUser();
	}

	/**
	 * Asserts the service implementation: getStatus returns the same log if no
	 * new log entry is available
	 * 
	 * @throws Exception
	 */
	@Test
	public void getStatusReturnsTheWholeLogIfNoNewLogEntryOnServer() throws Exception {
		String applicationName = ApplicationUtils.createRandomApplicationName();
		try {
			Application application = service.createApplication(applicationName, ICartridge.JBOSSAS_7, user);
			String applicationStatus = service.getStatus(application.getName(), application.getCartridge(), user);
			String applicationStatus2 = service.getStatus(application.getName(), application.getCartridge(), user);
			assertEquals(applicationStatus, applicationStatus2);
		} finally {
			ApplicationUtils.silentlyDestroyAS7Application(applicationName, user, service);
		}
	}

	/**
	 * Asserts the service implementation: getStatus returns the new entries
	 * (and a tailing-header) if new log entries are available
	 */
	@Test
	public void getStatusReturnsNewEntriesIfNewLogEntriesOnServer() throws Exception {
		String applicationName = ApplicationUtils.createRandomApplicationName();
		try {
			Application application = service.createApplication(applicationName, ICartridge.JBOSSAS_7, user);
			String applicationStatus = service.getStatus(application.getName(), application.getCartridge(), user);
			application.restart();
			String applicationStatus2 = service.getStatus(application.getName(), application.getCartridge(), user);
			assertFalse(applicationStatus.equals(applicationStatus2));
		} finally {
			ApplicationUtils.silentlyDestroyAS7Application(applicationName, user, service);
		}
	}

}
