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

import static org.jboss.ide.eclipse.as.openshift.test.internal.core.utils.ApplicationAsserts.assertAppliactionUrl;
import static org.jboss.ide.eclipse.as.openshift.test.internal.core.utils.ApplicationAsserts.assertGitUri;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.jboss.ide.eclipse.as.openshift.core.IApplication;
import org.jboss.ide.eclipse.as.openshift.core.ICartridge;
import org.jboss.ide.eclipse.as.openshift.core.InvalidCredentialsOpenshiftException;
import org.jboss.ide.eclipse.as.openshift.core.OpenshiftException;
import org.jboss.ide.eclipse.as.openshift.core.internal.Application;
import org.jboss.ide.eclipse.as.openshift.core.internal.IOpenshiftService;
import org.jboss.ide.eclipse.as.openshift.core.internal.OpenshiftService;
import org.jboss.ide.eclipse.as.openshift.core.internal.InternalUser;
import org.jboss.ide.eclipse.as.openshift.test.internal.core.fakes.TestUser;
import org.jboss.ide.eclipse.as.openshift.test.internal.core.utils.ApplicationUtils;
import org.junit.Before;
import org.junit.Test;

/**
 * @author André Dietisheim
 */
public class ApplicationIntegrationTest {

	private IOpenshiftService service;

	private InternalUser internalUser;
	private InternalUser invalidUser;

	@Before
	public void setUp() {
		this.service = new OpenshiftService();
		this.internalUser = new TestUser();
		this.invalidUser = new TestUser("bogusPassword");
	}

	@Test(expected = InvalidCredentialsOpenshiftException.class)
	public void createApplicationWithInvalidCredentialsThrowsException() throws Exception {
		service.createApplication(ApplicationUtils.createRandomApplicationName(), ICartridge.JBOSSAS_7, invalidUser);
	}

	@Test
	public void canCreateApplication() throws Exception {
		String applicationName = ApplicationUtils.createRandomApplicationName();
		try {
			ICartridge cartridge = ICartridge.JBOSSAS_7;
			Application application = service.createApplication(applicationName, cartridge, internalUser);
			assertNotNull(application);
			assertEquals(applicationName, application.getName());
			assertEquals(cartridge, application.getCartridge());
		} finally {
			ApplicationUtils.silentlyDestroyAS7Application(applicationName, internalUser, service);
		}
	}

	@Test
	public void canDestroyApplication() throws Exception {
		String applicationName = ApplicationUtils.createRandomApplicationName();
		service.createApplication(applicationName, ICartridge.JBOSSAS_7, internalUser);
		service.destroyApplication(applicationName, ICartridge.JBOSSAS_7, internalUser);
	}

	@Test(expected = OpenshiftException.class)
	public void createDuplicateApplicationThrowsException() throws Exception {
		String applicationName = ApplicationUtils.createRandomApplicationName();
		try {
			service.createApplication(applicationName, ICartridge.JBOSSAS_7, internalUser);
			service.createApplication(applicationName, ICartridge.JBOSSAS_7, internalUser);
		} finally {
			ApplicationUtils.silentlyDestroyAS7Application(applicationName, internalUser, service);
		}
	}

	@Test
	public void canStopApplication() throws Exception {
		String applicationName = ApplicationUtils.createRandomApplicationName();
		try {
			service.createApplication(applicationName, ICartridge.JBOSSAS_7, internalUser);
			service.stopApplication(applicationName, ICartridge.JBOSSAS_7, internalUser);
		} finally {
			ApplicationUtils.silentlyDestroyAS7Application(applicationName, internalUser, service);
		}
	}

	@Test
	public void canStartStoppedApplication() throws Exception {
		String applicationName = ApplicationUtils.createRandomApplicationName();
		try {
			service.createApplication(applicationName, ICartridge.JBOSSAS_7, internalUser);
			service.stopApplication(applicationName, ICartridge.JBOSSAS_7, internalUser);
			service.startApplication(applicationName, ICartridge.JBOSSAS_7, internalUser);
		} finally {
			ApplicationUtils.silentlyDestroyAS7Application(applicationName, internalUser, service);
		}
	}

	@Test
	public void canStartStartedApplication() throws Exception {
		String applicationName = ApplicationUtils.createRandomApplicationName();
		try {
			/**
			 * freshly created apps are started
			 * 
			 * @link 
			 *       https://github.com/openshift/os-client-tools/blob/master/express
			 *       /doc/API
			 */
			service.createApplication(applicationName, ICartridge.JBOSSAS_7, internalUser);
			service.startApplication(applicationName, ICartridge.JBOSSAS_7, internalUser);
		} finally {
			ApplicationUtils.silentlyDestroyAS7Application(applicationName, internalUser, service);
		}
	}

	@Test
	public void canStopStoppedApplication() throws Exception {
		String applicationName = ApplicationUtils.createRandomApplicationName();
		try {
			/**
			 * freshly created apps are started
			 * 
			 * @link 
			 *       https://github.com/openshift/os-client-tools/blob/master/express
			 *       /doc/API
			 */
			service.createApplication(applicationName, ICartridge.JBOSSAS_7, internalUser);
			service.stopApplication(applicationName, ICartridge.JBOSSAS_7, internalUser);
			service.stopApplication(applicationName, ICartridge.JBOSSAS_7, internalUser);
		} finally {
			ApplicationUtils.silentlyDestroyAS7Application(applicationName, internalUser, service);
		}
	}

	@Test
	public void canRestartApplication() throws Exception {
		String applicationName = ApplicationUtils.createRandomApplicationName();
		try {
			/**
			 * freshly created apps are started
			 * 
			 * @link 
			 *       https://github.com/openshift/os-client-tools/blob/master/express
			 *       /doc/API
			 */
			service.createApplication(applicationName, ICartridge.JBOSSAS_7, internalUser);
			service.restartApplication(applicationName, ICartridge.JBOSSAS_7, internalUser);
		} finally {
			ApplicationUtils.silentlyDestroyAS7Application(applicationName, internalUser, service);
		}
	}

	@Test
	public void canGetStatus() throws Exception {
		String applicationName = ApplicationUtils.createRandomApplicationName();
		try {
			Application application = service.createApplication(applicationName, ICartridge.JBOSSAS_7, internalUser);
			String applicationStatus = service.getStatus(application.getName(), application.getCartridge(), internalUser);
			assertNotNull(applicationStatus);
		} finally {
			ApplicationUtils.silentlyDestroyAS7Application(applicationName, internalUser, service);
		}
	}

	@Test
	public void returnsValidGitUri() throws Exception {
		String applicationName = ApplicationUtils.createRandomApplicationName();
		try {
			IApplication application = service.createApplication(applicationName, ICartridge.JBOSSAS_7, internalUser);
			String gitUri = application.getGitUri();
			assertNotNull(gitUri);
			assertGitUri(applicationName, gitUri);
		} finally {
			ApplicationUtils.silentlyDestroyAS7Application(applicationName, internalUser, service);
		}
	}

	@Test
	public void returnsValidApplicationUrl() throws Exception {
		String applicationName = ApplicationUtils.createRandomApplicationName();
		try {
			IApplication application = service.createApplication(applicationName, ICartridge.JBOSSAS_7, internalUser);
			String applicationUrl = application.getApplicationUrl();
			assertNotNull(applicationUrl);
			assertAppliactionUrl(applicationName, applicationUrl);
		} finally {
			ApplicationUtils.silentlyDestroyAS7Application(applicationName, internalUser, service);
		}
	}
}
