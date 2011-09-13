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

import org.jboss.ide.eclipse.as.openshift.core.Application;
import org.jboss.ide.eclipse.as.openshift.core.Cartridge;
import org.jboss.ide.eclipse.as.openshift.core.IOpenshiftService;
import org.jboss.ide.eclipse.as.openshift.core.InvalidCredentialsOpenshiftException;
import org.jboss.ide.eclipse.as.openshift.core.OpenshiftException;
import org.jboss.ide.eclipse.as.openshift.core.Status;
import org.jboss.ide.eclipse.as.openshift.core.internal.OpenshiftService;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author André Dietisheim
 */
public class ApplicationIntegrationTest {

	private IOpenshiftService openshiftService;
	private IOpenshiftService invalidCredentialsOpenshiftService;

	private static final String USERNAME = "toolsjboss@gmail.com";
	private static final String PASSWORD = "1q2w3e";

	@Before
	public void setUp() {
		this.openshiftService = new OpenshiftService(USERNAME, PASSWORD);
		this.invalidCredentialsOpenshiftService = new OpenshiftService(USERNAME, "bogus");
	}

	@Ignore
	@Test(expected = InvalidCredentialsOpenshiftException.class)
	public void createApplicationWithInvalidCredentialsThrowsException() throws Exception {
		invalidCredentialsOpenshiftService.createApplication(createRandomApplicationName(), Cartridge.JBOSSAS_7);
	}

	@Ignore
	@Test
	public void canCreateApplication() throws Exception {
		String applicationName = createRandomApplicationName();
		try {
			Cartridge cartridge = Cartridge.JBOSSAS_7;
			Application application = openshiftService.createApplication(applicationName, cartridge);
			assertNotNull(application);
			assertEquals(applicationName, application.getName());
			assertEquals(cartridge, application.getCartridge());
		} finally {
			silentlyDestroyApplication(applicationName, openshiftService);
		}
	}

	@Ignore
	@Test
	public void canDestroyApplication() throws Exception {
		String applicationName = createRandomApplicationName();
		openshiftService.createApplication(applicationName, Cartridge.JBOSSAS_7);
		openshiftService.destroyApplication(applicationName, Cartridge.JBOSSAS_7);
	}

	@Ignore
	@Test(expected = OpenshiftException.class)
	public void createDuplicateApplicationThrowsException() throws Exception {
		String applicationName = createRandomApplicationName();
		try {
			openshiftService.createApplication(applicationName, Cartridge.JBOSSAS_7);
			openshiftService.createApplication(applicationName, Cartridge.JBOSSAS_7);
		} finally {
			silentlyDestroyApplication(applicationName, openshiftService);
		}
	}

	@Ignore
	@Test
	public void canStopApplication() throws Exception {
		String applicationName = createRandomApplicationName();
		try {
			openshiftService.createApplication(applicationName, Cartridge.JBOSSAS_7);
			openshiftService.stopApplication(applicationName, Cartridge.JBOSSAS_7);
		} finally {
			silentlyDestroyApplication(applicationName, openshiftService);
		}
	}

	@Ignore
	@Test
	public void canStartStoppedApplication() throws Exception {
		String applicationName = createRandomApplicationName();
		try {
			openshiftService.createApplication(applicationName, Cartridge.JBOSSAS_7);
			openshiftService.stopApplication(applicationName, Cartridge.JBOSSAS_7);
			openshiftService.startApplication(applicationName, Cartridge.JBOSSAS_7);
		} finally {
			silentlyDestroyApplication(applicationName, openshiftService);
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
			 *       https://github.com/openshift/os-client-tools/blob/master/express/doc/API
			 */
			openshiftService.createApplication(applicationName, Cartridge.JBOSSAS_7);
			openshiftService.startApplication(applicationName, Cartridge.JBOSSAS_7);
		} finally {
			silentlyDestroyApplication(applicationName, openshiftService);
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
			 *       https://github.com/openshift/os-client-tools/blob/master/express/doc/API
			 */
			openshiftService.createApplication(applicationName, Cartridge.JBOSSAS_7);
			openshiftService.stopApplication(applicationName, Cartridge.JBOSSAS_7);
			openshiftService.stopApplication(applicationName, Cartridge.JBOSSAS_7);
		} finally {
			silentlyDestroyApplication(applicationName, openshiftService);
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
			 *       https://github.com/openshift/os-client-tools/blob/master/express/doc/API
			 */
			openshiftService.createApplication(applicationName, Cartridge.JBOSSAS_7);
			openshiftService.restartApplication(applicationName, Cartridge.JBOSSAS_7);
		} finally {
			silentlyDestroyApplication(applicationName, openshiftService);
		}
	}

	@Test
	public void canGetStatus() throws Exception {
		String applicationName = createRandomApplicationName();
		try {
			Application application = openshiftService.createApplication(applicationName, Cartridge.JBOSSAS_7);
			Status status = openshiftService.getStatus(application);
			assertNotNull(status);
		} finally {
			silentlyDestroyApplication(applicationName, openshiftService);
		}
	}
	
	private String createRandomApplicationName() {
		return String.valueOf(System.currentTimeMillis());
	}

	private void silentlyDestroyApplication(String name, IOpenshiftService service) {
		try {
			service.destroyApplication(name, Cartridge.JBOSSAS_7);
		} catch (OpenshiftException e) {
			e.printStackTrace();
		}
	}
}
