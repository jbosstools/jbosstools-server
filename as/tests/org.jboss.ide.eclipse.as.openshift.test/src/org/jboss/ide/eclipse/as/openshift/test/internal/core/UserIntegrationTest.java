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

import static org.jboss.ide.eclipse.as.openshift.test.internal.core.utils.ApplicationAsserts.assertApplication;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;

import javax.xml.datatype.DatatypeConfigurationException;

import org.jboss.ide.eclipse.as.openshift.core.IApplication;
import org.jboss.ide.eclipse.as.openshift.core.ICartridge;
import org.jboss.ide.eclipse.as.openshift.core.IDomain;
import org.jboss.ide.eclipse.as.openshift.core.ISSHPublicKey;
import org.jboss.ide.eclipse.as.openshift.core.OpenshiftEndpointException;
import org.jboss.ide.eclipse.as.openshift.core.OpenshiftException;
import org.jboss.ide.eclipse.as.openshift.core.User;
import org.jboss.ide.eclipse.as.openshift.core.internal.OpenshiftService;
import org.jboss.ide.eclipse.as.openshift.test.internal.core.fakes.TestUser;
import org.jboss.ide.eclipse.as.openshift.test.internal.core.utils.ApplicationUtils;
import org.junit.Before;
import org.junit.Test;

/**
 * @author André Dietisheim
 */
public class UserIntegrationTest {

	private User user;

	@Before
	public void setUp() throws OpenshiftException, DatatypeConfigurationException {
		this.user = new TestUser();
	}

	@Test
	public void canGetUserUUID() throws OpenshiftException {
		String uuid = user.getUUID();
		assertNotNull(uuid);
		assertTrue(uuid.length() > 0);
	}

	@Test
	public void canGetPublicKey() throws OpenshiftException {
		ISSHPublicKey key = user.getSshKey();
		assertNotNull(key);
		assertNotNull(key.getPublicKey());
		assertTrue(key.getPublicKey().length() > 0);
	}

	@Test
	public void canGetDomain() throws OpenshiftException {
		IDomain domain = user.getDomain();
		assertNotNull(domain);
		assertNotNull(domain.getRhcDomain());
		assertTrue(domain.getRhcDomain().length() > 0);
		assertNotNull(domain.getNamespace());
		assertTrue(domain.getNamespace().length() > 0);
	}

	@Test(expected=OpenshiftEndpointException.class)
	public void cannotCreateDomainIfAlreadyExists() throws OpenshiftException {
		IDomain domain = user.getDomain();
		assertNotNull(domain);
		ISSHPublicKey key = user.getSshKey();
		assertNotNull(key);
		user.createDomain("newDomain", key);
	}

	@Test
	public void canGetCartridges() throws OpenshiftException {
		Collection<ICartridge> cartridges = user.getCartridges();
		assertNotNull(cartridges);
		assertTrue(cartridges.size() >= 5);
	}

	@Test
	public void canGetApplications() throws OpenshiftException {
		Collection<IApplication> applications = user.getApplications();
		assertNotNull(applications);
	}

	@Test
	public void canCreateApplication() throws OpenshiftException {
		String applicationName = ApplicationUtils.createRandomApplicationName();
		try {
			Collection<IApplication> applications = user.getApplications();
			assertNotNull(applications);
			int numOfApplications = applications.size();
			IApplication application = user.createApplication(applicationName, ICartridge.JBOSSAS_7);
			assertEquals(numOfApplications + 1, applications.size());
			assertApplication(applicationName, ICartridge.JBOSSAS_7.getName(), application);
		} finally {
			ApplicationUtils.silentlyDestroyAS7Application(applicationName, user, new OpenshiftService());
		}
	}

	@Test
	public void canGetApplicationByName() throws OpenshiftException, DatatypeConfigurationException {
		String applicationName = ApplicationUtils.createRandomApplicationName();
		try {
			IApplication application = user.createApplication(applicationName, ICartridge.JBOSSAS_7);
			IApplication applicationFound = user.getApplicationByName(applicationName);
			assertNotNull(applicationFound);
			assertEquals(application, applicationFound);
		} finally {
			ApplicationUtils.silentlyDestroyAS7Application(applicationName, user, new OpenshiftService());
		}
	}
}
