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

import static org.jboss.ide.eclipse.as.openshift.test.internal.core.utils.ApplicationAsserts.assertGitUri;
import static org.jboss.ide.eclipse.as.openshift.test.internal.core.utils.ApplicationAsserts.assertAppliactionUrl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.jboss.ide.eclipse.as.openshift.core.IApplication;
import org.jboss.ide.eclipse.as.openshift.core.ICartridge;
import org.jboss.ide.eclipse.as.openshift.core.InvalidCredentialsOpenshiftException;
import org.jboss.ide.eclipse.as.openshift.core.OpenshiftException;
import org.jboss.ide.eclipse.as.openshift.core.internal.Application;
import org.jboss.ide.eclipse.as.openshift.core.internal.IOpenshiftService;
import org.jboss.ide.eclipse.as.openshift.core.internal.OpenshiftService;
import org.jboss.ide.eclipse.as.openshift.core.internal.User;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author André Dietisheim
 */
public class ApplicationIntegrationTest {

	private IOpenshiftService service;

	private static final String RHLOGIN = "toolsjboss@gmail.com";
	private static final String PASSWORD = "1q2w3e";
	private User user;
	private User invalidUser;

	@Ignore
	@Before
	public void setUp() {
		this.service = new OpenshiftService();
		this.user = new User(RHLOGIN, PASSWORD);
		this.invalidUser = new User(RHLOGIN, "bogusPassword");
	}

	@Ignore
	@Test(expected = InvalidCredentialsOpenshiftException.class)
	public void createApplicationWithInvalidCredentialsThrowsException() throws Exception {
		service.createApplication(createRandomApplicationName(), ICartridge.JBOSSAS_7, invalidUser);
	}

	@Ignore
	@Test
	public void canCreateApplication() throws Exception {
		String applicationName = createRandomApplicationName();
		try {
			ICartridge cartridge = ICartridge.JBOSSAS_7;
			Application application = service.createApplication(applicationName, cartridge, user);
			assertNotNull(application);
			assertEquals(applicationName, application.getName());
			assertEquals(cartridge, application.getCartridge());
		} finally {
			silentlyDestroyAS7Application(applicationName, service);
		}
	}

	@Ignore
	@Test
	public void canDestroyApplication() throws Exception {
		String applicationName = createRandomApplicationName();
		service.createApplication(applicationName, ICartridge.JBOSSAS_7, user);
		service.destroyApplication(applicationName, ICartridge.JBOSSAS_7, user);
	}

	@Ignore
	@Test(expected = OpenshiftException.class)
	public void createDuplicateApplicationThrowsException() throws Exception {
		String applicationName = createRandomApplicationName();
		try {
			service.createApplication(applicationName, ICartridge.JBOSSAS_7, user);
			service.createApplication(applicationName, ICartridge.JBOSSAS_7, user);
		} finally {
			silentlyDestroyAS7Application(applicationName, service);
		}
	}

	@Ignore
	@Test
	public void canStopApplication() throws Exception {
		String applicationName = createRandomApplicationName();
		try {
			service.createApplication(applicationName, ICartridge.JBOSSAS_7, user);
			service.stopApplication(applicationName, ICartridge.JBOSSAS_7, user);
		} finally {
			silentlyDestroyAS7Application(applicationName, service);
		}
	}

	@Ignore
	@Test
	public void canStartStoppedApplication() throws Exception {
		String applicationName = createRandomApplicationName();
		try {
			service.createApplication(applicationName, ICartridge.JBOSSAS_7, user);
			service.stopApplication(applicationName, ICartridge.JBOSSAS_7, user);
			service.startApplication(applicationName, ICartridge.JBOSSAS_7, user);
		} finally {
			silentlyDestroyAS7Application(applicationName, service);
		}
	}

	@Ignore
	@Test
	public void canStartStartedApplication() throws Exception {
		String applicationName = createRandomApplicationName();
		try {
			/**
			 * freshly created apps are started
			 * 
			 * @link 
			 *       https://github.com/openshift/os-client-tools/blob/master/express
			 *       /doc/API
			 */
			service.createApplication(applicationName, ICartridge.JBOSSAS_7, user);
			service.startApplication(applicationName, ICartridge.JBOSSAS_7, user);
		} finally {
			silentlyDestroyAS7Application(applicationName, service);
		}
	}

	@Ignore
	@Test
	public void canStopStoppedApplication() throws Exception {
		String applicationName = createRandomApplicationName();
		try {
			/**
			 * freshly created apps are started
			 * 
			 * @link 
			 *       https://github.com/openshift/os-client-tools/blob/master/express
			 *       /doc/API
			 */
			service.createApplication(applicationName, ICartridge.JBOSSAS_7, user);
			service.stopApplication(applicationName, ICartridge.JBOSSAS_7, user);
			service.stopApplication(applicationName, ICartridge.JBOSSAS_7, user);
		} finally {
			silentlyDestroyAS7Application(applicationName, service);
		}
	}

	@Ignore
	@Test
	public void canRestartApplication() throws Exception {
		String applicationName = createRandomApplicationName();
		try {
			/**
			 * freshly created apps are started
			 * 
			 * @link 
			 *       https://github.com/openshift/os-client-tools/blob/master/express
			 *       /doc/API
			 */
			service.createApplication(applicationName, ICartridge.JBOSSAS_7, user);
			service.restartApplication(applicationName, ICartridge.JBOSSAS_7, user);
		} finally {
			silentlyDestroyAS7Application(applicationName, service);
		}
	}

	@Ignore
	@Test
	public void canGetStatus() throws Exception {
		String applicationName = createRandomApplicationName();
		try {
			Application application = service.createApplication(applicationName, ICartridge.JBOSSAS_7, user);
			String applicationStatus = service.getStatus(application.getName(), application.getCartridge(), user);
			assertNotNull(applicationStatus);
		} finally {
			silentlyDestroyAS7Application(applicationName, service);
		}
	}

	@Ignore
	@Test
	public void getStatusReturnsTheWholeLog() throws Exception {
		String applicationName = createRandomApplicationName();
		try {
			Application application = service.createApplication(applicationName, ICartridge.JBOSSAS_7, user);
			String applicationStatus = service.getStatus(application.getName(), application.getCartridge(), user);
			String applicationStatus2 = service.getStatus(application.getName(), application.getCartridge(), user);
			assertEquals(applicationStatus, applicationStatus2);
		} finally {
			silentlyDestroyAS7Application(applicationName, service);
		}
	}

	@Test
	public void returnsValidGitUri() throws Exception {
		String applicationName = createRandomApplicationName();
		try {
			IApplication application = service.createApplication(applicationName, ICartridge.JBOSSAS_7, user);
			String gitUri = application.getGitUri();
			assertNotNull(gitUri);
			assertGitUri(applicationName, gitUri);
		} finally {
			silentlyDestroyAS7Application(applicationName, service);
		}
	}

	@Test
	public void returnsValidApplicationUrl() throws Exception {
		String applicationName = createRandomApplicationName();
		try {
			IApplication application = service.createApplication(applicationName, ICartridge.JBOSSAS_7, user);
			String applicationUrl = application.getApplicationUrl();
			assertNotNull(applicationUrl);
			assertAppliactionUrl(applicationName, applicationUrl);
		} finally {
			silentlyDestroyAS7Application(applicationName, service);
		}
	}

	private String createRandomApplicationName() {
		return String.valueOf(System.currentTimeMillis());
	}

	private void silentlyDestroyAS7Application(String name, IOpenshiftService service) {
		try {
			service.destroyApplication(name, ICartridge.JBOSSAS_7, user);
		} catch (OpenshiftException e) {
			e.printStackTrace();
		}
	}
}
